package pl.karoldronia.artefacts.artefact.impl.luck.ability;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.luck.LuckArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LuckArtefactAbility implements Ability {

    private final LuckArtefactConfig artefactConfig;
    private final NoticeService noticeService;
    private final List<PotionEffectType> EFFECTS = List.of(
            PotionEffectType.STRENGTH,
            PotionEffectType.SPEED,
            PotionEffectType.HASTE,
            PotionEffectType.RESISTANCE,
            PotionEffectType.REGENERATION,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.HUNGER
    );

    public LuckArtefactAbility(LuckArtefactConfig artefactConfig, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "luck";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        PotionEffectType randomEffect = this.getRandomEffect();
        player.addPotionEffect(new PotionEffect(
                randomEffect,
                30 * 20,
                2,
                false,
                true,
                true
        ));

        this.noticeService.create()
                .notice(messages -> messages.luckAbility)
                .player(player.getUniqueId())
                .placeholder("{effect}", randomEffect.getName().toLowerCase().replace("_", " "))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

    private PotionEffectType getRandomEffect() {
        int index = ThreadLocalRandom.current().nextInt(EFFECTS.size());
        return EFFECTS.get(index);
    }
}