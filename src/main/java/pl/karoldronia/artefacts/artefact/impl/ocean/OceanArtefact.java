package pl.karoldronia.artefacts.artefact.impl.ocean;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.ocean.ability.OceanArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.ocean.ability.OceanArtefactUpgradedAbility;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.List;

public class OceanArtefact implements Artefact {

    public static final String ID = "ocean";
    private final Plugin plugin;
    private final OceanArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public OceanArtefact(Plugin plugin, OceanArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
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
                PotionEffectType.DOLPHINS_GRACE,
                Integer.MAX_VALUE,
                0,
                false,
                true,
                true
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.CONDUIT_POWER,
                Integer.MAX_VALUE,
                0,
                false,
                true,
                true
        ));
        return List.of(PotionEffectType.DOLPHINS_GRACE, PotionEffectType.CONDUIT_POWER);
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new OceanArtefactAbility(this.artefactConfig, this.noticeService),
                new OceanArtefactUpgradedAbility(this.plugin, this.artefactConfig)
        );
    }

}