package pl.karoldronia.artefacts.artefact.impl.luck;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.luck.ability.LuckArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.luck.ability.LuckArtefactUpgradedAbility;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.List;

public class LuckArtefact implements Artefact {

    public static final String ID = "luck";
    private final Plugin plugin;
    private final LuckArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public LuckArtefact(Plugin plugin, LuckArtefactConfig artefactConfig, NoticeService noticeService) {
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
                PotionEffectType.HERO_OF_THE_VILLAGE,
                Integer.MAX_VALUE,
                2,
                false,
                true,
                true
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.LUCK,
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
                new LuckArtefactAbility(this.artefactConfig, this.noticeService),
                new LuckArtefactUpgradedAbility(this.plugin, this.artefactConfig, this.noticeService)
        );
    }
}