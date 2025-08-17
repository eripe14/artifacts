package pl.karoldronia.artefacts.artefact.impl.ice;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.ice.ability.IceArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.ice.ability.IceArtefactUpgradedAbility;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.List;

public class IceArtefact implements Artefact {

    public static final String ID = "ice";

    private final IceArtefactConfig artefactConfig;
    private final Plugin plugin;
    private final NoticeService noticeService;

    public IceArtefact(IceArtefactConfig artefactConfig, Plugin plugin, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.plugin = plugin;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void givePassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE,
                Integer.MAX_VALUE,
                0,
                false,
                true,
                true
        ));
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
                new IceArtefactAbility(this.artefactConfig, this.plugin, this.noticeService),
                new IceArtefactUpgradedAbility(this.artefactConfig, this.plugin, this.noticeService)
        );
    }

    @Override
    public void registerControllers(Plugin plugin, Server server, ProfileRepository profileRepository) {
        server.getPluginManager().registerEvents(
                new IceArtefactController(profileRepository),
                this.plugin
        );
    }
}