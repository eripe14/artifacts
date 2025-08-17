package pl.karoldronia.artefacts.artefact.impl.air;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.air.ability.AirArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.air.ability.AirArtefactUpgradedAbility;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.List;

public class AirArtefact implements Artefact {

    public static final String ID = "air";

    private final Plugin plugin;
    private final AirArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public AirArtefact(Plugin plugin, AirArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void givePassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                0,
                false,
                true,
                true
        ));
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new AirArtefactAbility(this.artefactConfig, this.noticeService),
                new AirArtefactUpgradedAbility(this.plugin, this.artefactConfig, this.noticeService)
        );
    }

    @Override
    public void registerControllers(Plugin plugin, Server server, ProfileRepository profileRepository) {
        server.getPluginManager().registerEvents(
                new AirArtefactController(profileRepository),
                plugin
        );
    }
}