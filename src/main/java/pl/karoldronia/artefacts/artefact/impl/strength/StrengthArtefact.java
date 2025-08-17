package pl.karoldronia.artefacts.artefact.impl.strength;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.strength.ability.StrengthArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.strength.ability.StrengthArtefactUpgradedAbility;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.List;

public class StrengthArtefact implements Artefact {

    public static final String ID = "strength";
    private final Plugin plugin;
    private final StrengthArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public StrengthArtefact(Plugin plugin, StrengthArtefactConfig artefactConfig, NoticeService noticeService) {
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
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                1,
                false,
                true,
                true
        ));
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new StrengthArtefactAbility(this.artefactConfig, this.noticeService),
                new StrengthArtefactUpgradedAbility(this.plugin, this.artefactConfig, this.noticeService)
        );
    }
}