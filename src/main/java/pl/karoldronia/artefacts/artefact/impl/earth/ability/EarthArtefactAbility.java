package pl.karoldronia.artefacts.artefact.impl.earth.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.earth.EarthArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public class EarthArtefactAbility implements Ability {

    private final NoticeService noticeService;
    private final EarthArtefactConfig artefactConfig;

    public EarthArtefactAbility(NoticeService noticeService, EarthArtefactConfig artefactConfig) {
        this.noticeService = noticeService;
        this.artefactConfig = artefactConfig;
    }

    @Override
    public String getId() {
        return "earth";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        List<Player> targets = this.getTargets(player, profile, this.artefactConfig.abilityRadius);
        for (Player target : targets) {
            target.setVelocity(new Vector(0, 2.0, 0));
            target.playSound(target.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.2f, 1f);
            target.spawnParticle(Particle.CLOUD, target.getLocation(), 30, 0.5, 0.1, 0.5, 0.02);
        }

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.earthAbility)
                .placeholder("{affected}", String.valueOf(targets.size()))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

}