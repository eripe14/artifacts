package pl.karoldronia.artefacts.artefact.impl.earth;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.earth.ability.EarthArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.earth.ability.EarthArtefactUpgradedAbility;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.List;

public class EarthArtefact implements Artefact {

    public static final String ID = "earth";
    private final Plugin plugin;
    private final NoticeService noticeService;
    private final EarthArtefactConfig artefactConfig;

    public EarthArtefact(Plugin plugin, NoticeService noticeService, EarthArtefactConfig artefactConfig) {
        this.plugin = plugin;
        this.noticeService = noticeService;
        this.artefactConfig = artefactConfig;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void givePassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
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
                new EarthArtefactAbility(this.noticeService, this.artefactConfig),
                new EarthArtefactUpgradedAbility(this.plugin, this.artefactConfig, this.noticeService)
        );
    }

}