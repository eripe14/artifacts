package pl.karoldronia.artefacts.artefact.impl.thunder;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.thunder.ability.ThunderArtefactAbility;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.ProfileRepository;
import pl.karoldronia.artefacts.scheduler.Scheduler;

import java.util.List;

public class ThunderArtefact implements Artefact {

    public static final String ID = "thunder";

    private final Scheduler scheduler;
    private final NoticeService noticeService;
    private final ThunderArtefactConfig artefactConfig;

    public ThunderArtefact(Scheduler scheduler, NoticeService noticeService, ThunderArtefactConfig artefactConfig) {
        this.scheduler = scheduler;
        this.noticeService = noticeService;
        this.artefactConfig = artefactConfig;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ArtefactItem getArtefactItem() {
        return this.artefactConfig.item;
    }

    @Override
    public List<PotionEffectType> givePassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                0,
                false,
                true,
                true
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE,
                Integer.MAX_VALUE,
                0,
                false,
                true,
                true
        ));
        return List.of(PotionEffectType.SPEED, PotionEffectType.FIRE_RESISTANCE);
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new ThunderArtefactAbility(this.scheduler, this.noticeService, this.artefactConfig)
        );
    }

    @Override
    public void registerControllers(Plugin plugin, Server server, ProfileRepository profileRepository) {
        server.getPluginManager().registerEvents(
                new ThunderArtefactController(profileRepository),
                plugin
        );
    }
}