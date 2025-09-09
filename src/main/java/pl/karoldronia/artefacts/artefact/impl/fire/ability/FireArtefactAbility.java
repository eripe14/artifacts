package pl.karoldronia.artefacts.artefact.impl.fire.ability;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.fire.FireArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public class FireArtefactAbility implements Ability {

    private final FireArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public FireArtefactAbility(FireArtefactConfig artefactConfig, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "fire";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        double abilityRadius = this.artefactConfig.abilityRadius;
        List<Player> nearbyTargets = this.getTargets(player, profile, abilityRadius);

        for (Player nearbyTarget : nearbyTargets) {
            double fireDamage = this.artefactConfig.abilityFireDamage;
            nearbyTarget.damage(fireDamage, player);
            nearbyTarget.setFireTicks(this.artefactConfig.abilityFireTicks);

            nearbyTarget.getWorld().playSound(
                    nearbyTarget.getLocation(),
                    Sound.ENTITY_PLAYER_HURT_ON_FIRE,
                    1.0f,
                    1.0f
            );
        }

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.fireAbility)
                .placeholder("{affected}", String.valueOf(nearbyTargets.size()))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

}