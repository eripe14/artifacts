package pl.karoldronia.artefacts.artefact.impl.dragon;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.dragon.ability.FirstDragonArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.dragon.ability.SecondDragonArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.dragon.ability.ThirdDragonArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.dragon.controller.DragonArtefactAbilityController;
import pl.karoldronia.artefacts.artefact.impl.dragon.controller.DragonArtefactCraftController;
import pl.karoldronia.artefacts.artefact.impl.dragon.controller.DragonArtefactItemController;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;
import pl.karoldronia.artefacts.config.impl.PluginConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.List;

public class DragonArtefact implements Artefact {

    public static final String ID = "dragon";

    private final Plugin plugin;
    private final PluginConfig pluginConfig;
    private final DragonArtefactConfig artefactConfig;
    private final ArtefactService artefactService;
    private final NoticeService noticeService;

    public DragonArtefact(
            Plugin plugin,
            PluginConfig pluginConfig,
            DragonArtefactConfig artefactConfig,
            ArtefactService artefactService,
            NoticeService noticeService
    ) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.artefactConfig = artefactConfig;
        this.artefactService = artefactService;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ArtefactItem getArtefactItem() {
        return this.pluginConfig.dragonArtefactItem;
    }

    @Override
    public List<PotionEffectType> givePassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                1,
                false,
                true,
                true
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                1,
                false,
                true,
                true
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HEALTH_BOOST,
                Integer.MAX_VALUE,
                4,
                false,
                true,
                true
        ));

        return List.of(PotionEffectType.STRENGTH, PotionEffectType.SPEED, PotionEffectType.HEALTH_BOOST);
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new FirstDragonArtefactAbility(this.plugin, this.artefactConfig, this.noticeService),
                new SecondDragonArtefactAbility(this.plugin, this.artefactConfig, this.noticeService),
                new ThirdDragonArtefactAbility(this.artefactConfig, this.noticeService, this.plugin)
        );
    }

    @Override
    public void registerControllers(Plugin plugin, Server server, ProfileRepository profileRepository) {
        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(
                new DragonArtefactCraftController(profileRepository, this.noticeService, this.pluginConfig),
                plugin
        );
        pluginManager.registerEvents(
                new DragonArtefactAbilityController(profileRepository, this.noticeService, this.artefactService),
                plugin
        );
        pluginManager.registerEvents(
                new DragonArtefactItemController(profileRepository, this.artefactService, this.noticeService),
                plugin
        );
    }
}