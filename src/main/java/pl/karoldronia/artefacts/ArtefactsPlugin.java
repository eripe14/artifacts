package pl.karoldronia.artefacts;

import com.eternalcode.multification.notice.Notice;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import eu.okaeri.persistence.PersistenceCollection;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.repository.RepositoryDeclaration;
import dev.rollczi.litecommands.*;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ArtefactArgumentResolver;
import pl.karoldronia.artefacts.artefact.ArtefactCommand;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.ability.AbilityController;
import pl.karoldronia.artefacts.artefact.impl.air.AirArtefact;
import pl.karoldronia.artefacts.artefact.impl.earth.EarthArtefact;
import pl.karoldronia.artefacts.artefact.impl.fire.FireArtefact;
import pl.karoldronia.artefacts.artefact.impl.ice.IceArtefact;
import pl.karoldronia.artefacts.artefact.impl.luck.LuckArtefact;
import pl.karoldronia.artefacts.artefact.impl.ocean.OceanArtefact;
import pl.karoldronia.artefacts.artefact.impl.sculk.SculkArtefact;
import pl.karoldronia.artefacts.artefact.impl.strength.StrengthArtefact;
import pl.karoldronia.artefacts.artefact.impl.thunder.ThunderArtefact;
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
import pl.karoldronia.artefacts.scheduler.BukkitSchedulerImpl;
import pl.karoldronia.artefacts.scheduler.Scheduler;

public class ArtefactsPlugin extends JavaPlugin {

    private Scheduler scheduler;

    private NoticeService noticeService;

    private ConfigService configService;
    private PluginConfig pluginConfig;
    private MessageConfig messageConfig;

    private DatabaseManager databaseManager;
    private DocumentPersistence documentPersistence;

    private ProfileRepository profileRepository;

    private ArtefactService artefactService;

    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        Server server = this.getServer();

        this.scheduler = new BukkitSchedulerImpl(this);

        this.messageConfig = new MessageConfig();
        this.noticeService = new NoticeService(this.messageConfig);

        this.configService = new ConfigService(this.noticeService.getNoticeRegistry());
        this.pluginConfig  = this.configService.load(PluginConfig.class, this.getDataFolder(), "config.yml");
        this.messageConfig = this.configService.load(MessageConfig.class, this.getDataFolder(), "messages.yml");

        this.databaseManager = new DatabaseManager(this, this.pluginConfig);
        this.documentPersistence = this.databaseManager.connect();

        PersistenceCollection profilesCollection = PersistenceCollection.of(ProfileRepository.class);
        this.documentPersistence.registerCollection(profilesCollection);
        this.profileRepository = RepositoryDeclaration.of(ProfileRepository.class)
                .newProxy(this.documentPersistence, profilesCollection, this.getClass().getClassLoader());

        this.artefactService = new ArtefactService(this, this.profileRepository);
        this.setupArtefacts();

        server.getPluginManager().registerEvents(
                new AbilityController(this, this.profileRepository, this.artefactService, this.noticeService),
                this
        );

        CraftingService craftingService = new CraftingService(this, this.pluginConfig);
        craftingService.register();

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
    }

    void setupArtefacts() {
        this.artefactService.addArtefact(new OceanArtefact(this, this.pluginConfig.oceanArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new FireArtefact(this, this.pluginConfig.fireArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new ThunderArtefact(this.scheduler, this.noticeService, this.pluginConfig.thunderArtefactConfig));
        this.artefactService.addArtefact(new EarthArtefact(this, this.noticeService, this.pluginConfig.earthArtefactConfig));
        this.artefactService.addArtefact(new AirArtefact(this, this.pluginConfig.airArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new IceArtefact(this.pluginConfig.iceArtefactConfig, this, this.noticeService));
        this.artefactService.addArtefact(new SculkArtefact(this, this.pluginConfig.sculkArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new StrengthArtefact(this, this.pluginConfig.strengthArtefactConfig, this.noticeService));
        this.artefactService.addArtefact(new LuckArtefact(this, this.pluginConfig.luckArtefactConfig, this.noticeService));
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
                    new ArtefactCommand(this.profileRepository, this.noticeService)
                )
                .build();
    }

}