package pl.karoldronia.artefacts.artefact.impl.air.ability;

import org.bukkit.Location;
import org.bukkit.Material;
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
    public String getId() {
        return "air";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Vector direction = player.getLocation().getDirection().normalize();
        Location startLocation = player.getLocation().add(0, 0.5, 0); // Dodajemy trochę wysokości żeby nie sprawdzać bloku pod nogami

        Location endLocation = this.findMaximumSafeDashLocation(startLocation, direction);

        if (startLocation.distance(endLocation) < 1.0) {
            return AbilityResult.SUCCESS;
        }

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
                .placeholder("{distance}", String.valueOf((int) startLocation.distance(endLocation)))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }


    private Location findMaximumSafeDashLocation(Location start, Vector direction) {
        Location current = start.clone();
        Location lastSafe = start.clone();
        double stepSize = 0.25;
        double maxDistance = this.artefactConfig.dashDistance;

        for (double distance = stepSize; distance <= maxDistance; distance += stepSize) {
            current = start.clone().add(direction.clone().multiply(distance));
            if (isLocationSafe(current)) {
                lastSafe = current.clone();
            } else {
                break;
            }
        }

        return lastSafe;
    }

    private boolean isLocationSafe(Location location) {
        if (location.getBlock().getType().isSolid()) {
            return false;
        }

        Location headLocation = location.clone().add(0, 1, 0);
        if (headLocation.getBlock().getType().isSolid()) {
            return false;
        }

        Location groundLocation = location.clone().add(0, -1, 0);
        Material groundType = groundLocation.getBlock().getType();

        if (groundType == Material.AIR || groundType == Material.VOID_AIR) {
            return !(location.getY() < 5);
        }

        return true;
    }

    private List<Player> getTargetsInPath(Player player, Profile profile, Location start, Location end) {
        double radius = 2.0;

        return player.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(player))
                .filter(p -> !profile.getTrustedPlayers().contains(p.getUniqueId()))
                .filter(p -> isInDashPath(start, end, p.getLocation(), radius))
                .toList();
    }

    private boolean isInDashPath(Location start, Location end, Location point, double radius) {
        Vector startToEnd = end.toVector().subtract(start.toVector());
        Vector startToPoint = point.toVector().subtract(start.toVector());

        double projectionLength = startToPoint.dot(startToEnd) / startToEnd.lengthSquared();

        projectionLength = Math.max(0, Math.min(1, projectionLength));

        Vector closestPoint = start.toVector().add(startToEnd.multiply(projectionLength));
        double distance = point.toVector().distance(closestPoint);

        return distance <= radius;
    }

    private void createDashEffects(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);

        for (double i = 0; i < distance; i += 0.5) {
            Location particleLocation = start.clone().add(direction.clone().multiply(i));
            start.getWorld().spawnParticle(Particle.CLOUD, particleLocation, 3, 0.2, 0.2, 0.2, 0.1);
        }
    }
}