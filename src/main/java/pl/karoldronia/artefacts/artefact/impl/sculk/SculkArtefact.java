package pl.karoldronia.artefacts.artefact.impl.sculk;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.sculk.ability.SculkArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.sculk.ability.SculkArtefactUpgradedAbility;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.List;

public class SculkArtefact implements Artefact {

    public static final String ID = "sculk";
    private final Plugin plugin;
    private final SculkArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public SculkArtefact(Plugin plugin, SculkArtefactConfig artefactConfig, NoticeService noticeService) {
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
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                0,
                false,
                true,
                true
        ));
        return List.of(PotionEffectType.STRENGTH);
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new SculkArtefactAbility(this.plugin, this.artefactConfig, this.noticeService),
                new SculkArtefactUpgradedAbility(this.artefactConfig, this.plugin, this.noticeService)
        );
    }
}