package pl.karoldronia.artefacts;

import com.eternalcode.multification.notice.Notice;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import eu.okaeri.persistence.PersistenceCollection;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.repository.RepositoryDeclaration;
import dev.rollczi.litecommands.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;
import pl.karoldronia.artefacts.artefact.*;
import pl.karoldronia.artefacts.artefact.ability.AbilityController;
import pl.karoldronia.artefacts.artefact.impl.air.AirArtefact;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.artefact.impl.earth.EarthArtefact;
import pl.karoldronia.artefacts.artefact.impl.fire.FireArtefact;
import pl.karoldronia.artefacts.artefact.impl.ice.IceArtefact;
import pl.karoldronia.artefacts.artefact.impl.life.LifeArtefact;
import pl.karoldronia.artefacts.artefact.impl.luck.LuckArtefact;
import pl.karoldronia.artefacts.artefact.impl.ocean.OceanArtefact;
import pl.karoldronia.artefacts.artefact.impl.sculk.SculkArtefact;
import pl.karoldronia.artefacts.artefact.impl.strength.StrengthArtefact;
import pl.karoldronia.artefacts.artefact.impl.thunder.ThunderArtefact;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemController;
import pl.karoldronia.artefacts.artefact.passive.ArtefactPassiveController;
import pl.karoldronia.artefacts.artefact.passive.ArtefactPassiveService;
import pl.karoldronia.artefacts.artefact.protect.ArtefactProtectRepository;
import pl.karoldronia.artefacts.artefact.protect.ArtefactProtectService;
import pl.karoldronia.artefacts.command.InvalidUsageHandlerImpl;
import pl.karoldronia.artefacts.command.MissingPermissionHandlerImpl;
import pl.karoldronia.artefacts.config.ConfigService;
import pl.karoldronia.artefacts.config.impl.MessageConfig;
import pl.karoldronia.artefacts.config.impl.PluginConfig;
import pl.karoldronia.artefacts.artefact.item.crafting.CraftingService;
import pl.karoldronia.artefacts.notice.NoticeHandler;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.persistance.DatabaseManager;
import pl.karoldronia.artefacts.profile.ProfileRepository;
import pl.karoldronia.artefacts.profile.TrustCommand;
import pl.karoldronia.artefacts.scheduler.BukkitSchedulerImpl;
import pl.karoldronia.artefacts.scheduler.Scheduler;

import java.io.IOException;

public class ArtefactsPlugin extends JavaPlugin {

    public static final NamespacedKey ARTEFACT_ITEM_KEY;

    private Scheduler scheduler;

    private NoticeService noticeService;

    private ConfigService configService;
    private PluginConfig pluginConfig;
    private MessageConfig messageConfig;

    private DatabaseManager databaseManager;
    private DocumentPersistence documentPersistence;

    private ProfileRepository profileRepository;

    private ArtefactService artefactService;
    private ArtefactPassiveService artefactPassiveService;

    private ArtefactProtectRepository artefactProtectRepository;
    private ArtefactProtectService artefactProtectService;

    private CraftingService craftingService;

    private LiteCommands<CommandSender> liteCommands;

    static {
        ARTEFACT_ITEM_KEY = new NamespacedKey("artefacts", "artefact_item");
    }

    @Override
    public void onEnable() {
        Server server = this.getServer();

        this.scheduler = new BukkitSchedulerImpl(this);

        this.messageConfig = new MessageConfig();
        this.noticeService = new NoticeService(this.messageConfig);

        this.configService = new ConfigService(this.noticeService.getNoticeRegistry());
        this.pluginConfig = this.configService.load(PluginConfig.class, this.getDataFolder(), "config.yml");
        this.messageConfig = this.configService.load(MessageConfig.class, this.getDataFolder(), "messages.yml");

        this.databaseManager = new DatabaseManager(this, this.pluginConfig);
        this.documentPersistence = this.databaseManager.connect();

        PersistenceCollection profilesCollection = PersistenceCollection.of(ProfileRepository.class);
        this.documentPersistence.registerCollection(profilesCollection);
        this.profileRepository = RepositoryDeclaration.of(ProfileRepository.class)
                .newProxy(this.documentPersistence, profilesCollection, this.getClass().getClassLoader());

        this.artefactService = new ArtefactService(this, this.profileRepository);
        this.artefactPassiveService = new ArtefactPassiveService(this.scheduler, this.artefactService, this.profileRepository);

        PersistenceCollection protectsCollection = PersistenceCollection.of(ArtefactProtectRepository.class);
        this.documentPersistence.registerCollection(protectsCollection);
        this.artefactProtectRepository = RepositoryDeclaration.of(ArtefactProtectRepository.class)
                .newProxy(this.documentPersistence, protectsCollection, this.getClass().getClassLoader());

        this.artefactProtectService = new ArtefactProtectService(this.artefactProtectRepository);

        this.setupArtefacts();
        this.artefactPassiveService.passiveEffects();

        server.getPluginManager().registerEvents(
                new AbilityController(this, this.profileRepository, this.artefactService, this.noticeService, this.artefactProtectService),
                this
        );
        server.getPluginManager().registerEvents(
                new ArtefactItemController(this.profileRepository, this.artefactService, this.noticeService),
                this
        );
        server.getPluginManager().registerEvents(
                new ArtefactController(this.scheduler, this.profileRepository, this.artefactService, this.noticeService),
                this
        );
        server.getPluginManager().registerEvents(
                new ArtefactPassiveController(
                        this.artefactService,
                        this.artefactPassiveService,
                        this.profileRepository,
                        this.scheduler
                ),
                this
        );

        this.craftingService = new CraftingService(this, this.pluginConfig);
        this.craftingService.register();

        this.initializeCommands();
    }

    @Override
    public void onDisable() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }

        if (this.artefactService != null) {
            this.artefactService.shutdown();
        }

        if (this.artefactPassiveService != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.artefactPassiveService.onPlayerQuit(player.getUniqueId());
            }
        }

        if (this.craftingService != null) {
            this.craftingService.unregister();
        }

        if (this.documentPersistence != null) {
            try {
                this.documentPersistence.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void setupArtefacts() {
        this.artefactService.addArtefact(new OceanArtefact(this, this.pluginConfig.oceanArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new FireArtefact(this, this.pluginConfig.fireArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new ThunderArtefact(this.scheduler, this.noticeService, this.pluginConfig.thunderArtefactConfig));

        this.artefactService.addArtefact(new EarthArtefact(this, this.noticeService, this.pluginConfig.earthArtefactConfig));
        this.artefactService.addArtefact(new AirArtefact(this, this.pluginConfig.airArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new IceArtefact(this.pluginConfig.iceArtefactConfig, this, this.noticeService));
        this.artefactService.addArtefact(new LifeArtefact(this, this.pluginConfig.lifeArtefactConfig, this.noticeService));

        this.artefactService.addArtefact(new SculkArtefact(this, this.pluginConfig.sculkArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new StrengthArtefact(
                this,
                this.artefactService,
                this.pluginConfig.strengthArtefactConfig,
                this.noticeService
        ));
        this.artefactService.addArtefact(new LuckArtefact(this, this.pluginConfig.luckArtefactConfig, this.noticeService));

        this.artefactService.addArtefact(new DragonArtefact(
                this,
                this.pluginConfig,
                this.pluginConfig.dragonArtefactConfig,
                this.artefactService,
                this.noticeService
        ));
    }

    void initializeCommands() {
        this.liteCommands = LiteBukkitFactory.builder("artefacts", this)
                .settings(settings -> settings.fallbackPrefix("[FireballArtefacts]").nativePermissions(false))
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, this.messageConfig.cantFindPlayer)
                .message(LiteBukkitMessages.PLAYER_ONLY, input -> this.messageConfig.onlyForPlayer)
                .missingPermission(new MissingPermissionHandlerImpl(this.noticeService))
                .invalidUsage(new InvalidUsageHandlerImpl(this.noticeService))
                .result(Notice.class, new NoticeHandler(this.noticeService))
                .argument(Artefact.class, new ArtefactArgumentResolver(this.artefactService, this.messageConfig))
                .commands(
                        new ArtefactCommand(
                                this.profileRepository,
                                this.noticeService,
                                this.messageConfig,
                                this.pluginConfig,
                                this.craftingService,
                                this.artefactProtectService
                        ),
                        new TrustCommand(this.profileRepository, this.noticeService)
                )
                .build();
    }

}