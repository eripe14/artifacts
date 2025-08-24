package pl.karoldronia.artefacts.artefact.impl.strength;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.strength.ability.StrengthArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.strength.ability.StrengthArtefactUpgradedAbility;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.List;

@RequiredArgsConstructor
public class StrengthArtefact implements Artefact {

    public static final String ID = "strength";
    private final Plugin plugin;
    private final ArtefactService artefactService;
    private final StrengthArtefactConfig artefactConfig;
    private final NoticeService noticeService;

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
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                1,
                false,
                true,
                true
        ));
        return List.of(PotionEffectType.STRENGTH);
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new StrengthArtefactAbility(this.artefactConfig, this.noticeService),
                new StrengthArtefactUpgradedAbility(this.plugin, this.artefactService, this.artefactConfig, this.noticeService)
        );
    }
}