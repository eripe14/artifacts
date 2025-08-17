package pl.karoldronia.artefacts.artefact.impl.air.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.air.AirArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public class AirArtefactAbility implements Ability {

    private final AirArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public AirArtefactAbility(AirArtefactConfig artefactConfig, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Vector direction = player.getLocation().getDirection().normalize();
        Location startLocation = player.getLocation();
        Location endLocation = this.findSafeLocation(startLocation.clone().add(direction.multiply(this.artefactConfig.dashDistance)));

        List<Player> targets = this.getTargetsInPath(player, profile, startLocation, endLocation);
        for (Player target : targets) {
            double newHealth = Math.max(0, target.getHealth() - this.artefactConfig.dashDamage);
            target.setHealth(newHealth);

            this.noticeService.create()
                    .notice(messages -> messages.playerDashedThrough)
                    .player(target.getUniqueId())
                    .placeholder("{player}", player.getName())
                    .send();
        }

        player.teleport(endLocation);
        this.createDashEffects(startLocation, endLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);

        this.noticeService.create()
                .notice(messages -> messages.airAbility)
                .player(player.getUniqueId())
                .placeholder("{targets}", String.valueOf(targets.size()))
                .placeholder("{distance}", String.valueOf(this.artefactConfig.dashDistance))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

    private Location findSafeLocation(Location location) {
        while (location.getBlock().getType().isSolid() && location.getY() < 256) {
            location.add(0, 1, 0);
        }
        return location;
    }

    private List<Player> getTargetsInPath(Player player, Profile profile, Location start, Location end) {
        double radius = 3.0; // 3 block radius around the path

        return player.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(player))
                .filter(p -> !profile.getTrustedPlayers().contains(p.getUniqueId()))
                .filter(p -> isInDashPath(start, end, p.getLocation(), radius))
                .toList();
    }

    private boolean isInDashPath(Location start, Location end, Location point, double radius) {
        // Check if point is within radius of the line from start to end
        Vector startToEnd = end.toVector().subtract(start.toVector());
        Vector startToPoint = point.toVector().subtract(start.toVector());

        double projectionLength = startToPoint.dot(startToEnd) / startToEnd.lengthSquared();

        // Clamp projection to line segment
        projectionLength = Math.max(0, Math.min(1, projectionLength));

        Vector closestPoint = start.toVector().add(startToEnd.multiply(projectionLength));
        double distance = point.toVector().distance(closestPoint);

        return distance <= radius;
    }

    private void createDashEffects(Location start, Location end) {
        // Create particle trail
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);

        for (double i = 0; i < distance; i += 0.5) {
            Location particleLocation = start.clone().add(direction.clone().multiply(i));
            start.getWorld().spawnParticle(Particle.CLOUD, particleLocation, 3, 0.2, 0.2, 0.2, 0.1);
        }
    }
}