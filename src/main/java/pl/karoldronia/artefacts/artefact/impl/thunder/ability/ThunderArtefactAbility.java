package pl.karoldronia.artefacts.artefact.impl.thunder.ability;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.thunder.ThunderArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.scheduler.Scheduler;

import java.time.Duration;
import java.util.List;

public class ThunderArtefactAbility implements Ability {

    private final Scheduler scheduler;
    private final NoticeService noticeService;
    private final ThunderArtefactConfig artefactConfig;

    public ThunderArtefactAbility(
            Scheduler scheduler,
            NoticeService noticeService,
            ThunderArtefactConfig artefactConfig
    ) {
        this.scheduler = scheduler;
        this.noticeService = noticeService;
        this.artefactConfig = artefactConfig;
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        double abilityRadius = this.artefactConfig.abilityRadius;
        List<Player> nearbyTargets = this.getTargets(player, profile, abilityRadius);

        this.scheduler.repeatSync(() -> {
            for (Player nearbyTarget : nearbyTargets) {
                Location location = nearbyTarget.getLocation();
                location.getWorld().strikeLightning(location);
            }
        }, this.artefactConfig.abilityDelayBetween, this.artefactConfig.abilityTimes);

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.thunderAbility)
                .placeholder("{affected}", String.valueOf(nearbyTargets.size()))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

}