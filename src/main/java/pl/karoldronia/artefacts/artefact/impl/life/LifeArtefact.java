package pl.karoldronia.artefacts.artefact.impl.life;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.life.ability.LifeArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.life.ability.LifeArtefactUpgradedAbility;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.List;

public class LifeArtefact implements Artefact {

    public static final String ID = "life";
    private final Plugin plugin;
    private final LifeArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public LifeArtefact(Plugin plugin, LifeArtefactConfig artefactConfig, NoticeService noticeService) {
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
        double baseHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth + 8);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HEALTH_BOOST,
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
                new LifeArtefactAbility(this.artefactConfig, this.noticeService),
                new LifeArtefactUpgradedAbility(this.plugin, this.artefactConfig, this.noticeService)
        );
    }
}