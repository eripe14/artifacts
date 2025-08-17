package pl.karoldronia.artefacts.artefact.impl.air.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.air.AirArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AirArtefactUpgradedAbility implements Ability {

    private final Plugin plugin;
    private final AirArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public AirArtefactUpgradedAbility(
            Plugin plugin,
            AirArtefactConfig artefactConfig,
            NoticeService noticeService
    ) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Location originalLocation = player.getLocation().clone();
        this.performMultipleDashes(player, profile, originalLocation);

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.upgradedAbilityCooldown;
    }

    @Override
    public boolean requireUpgrade() {
        return true;
    }

    private void performMultipleDashes(Player player, Profile profile, Location centerLocation) {
        List<Player> nearbyTargets = this.getTargets(player, profile, this.artefactConfig.upgradedAbilityRadius);

        if (nearbyTargets.isEmpty()) {
            this.noticeService.create()
                    .notice(messages -> messages.noTargetsForUpgradedAirAbility)
                    .player(player.getUniqueId())
                    .send();
        }

        performDashSequence(player, nearbyTargets, centerLocation);
    }

    private void performDashSequence(Player player, List<Player> targets, Location centerLocation) {
        if (targets.isEmpty()) return;

        Player target = targets.getFirst(); // Dashać wokół pierwszego celu
        Location targetLoc = target.getLocation();

        // Lokacje wokół celu: 4 punkty kardynalne
        Location[] positions = new Location[]{
                targetLoc.clone().add(3, 1, 0),  // East
                targetLoc.clone().add(-3, 1, 0), // West
                targetLoc.clone().add(0, 1, 3),  // South
                targetLoc.clone().add(0, 1, -3)  // North
        };

        new BukkitRunnable() {
            private int dashIndex = 0;

            @Override
            public void run() {
                if (dashIndex >= positions.length) {
                    dashUpward(player);
                    cancel();
                    return;
                }

                Location dashLocation = findSafeLocation(positions[dashIndex]);
                createDashEffect(player.getLocation(), dashLocation);

                Vector direction = dashLocation.toVector().subtract(player.getLocation().toVector()).normalize();
                Vector velocity = direction.multiply(artefactConfig.upgradedDashDistance);
                velocity.setY(0.3);
                player.setVelocity(velocity);

                // Deal 1 heart = 2.0 HP
                double newHealth = Math.max(0, target.getHealth() - artefactConfig.singleDashDamage);
                target.setHealth(newHealth);

                dashIndex++;
            }
        }.runTaskTimer(this.plugin, 0L, 10L); // 0.5s między dashami
    }

    private void dashUpward(Player player) {
        player.setVelocity(new Vector(0, 1.8, 0)); // Wystrzał w górę

        new BukkitRunnable() {
            @Override
            public void run() {
                performGroundPound(player);
            }
        }.runTaskLater(this.plugin, 40L);
    }

    private void performGroundPound(Player player) {
        Location impactLocation = player.getLocation(); // gdzie gracz wyląduje

        List<Player> targets = player.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(player))
                .filter(p -> p.getLocation().distance(impactLocation) <= artefactConfig.groundPoundRadius)
                .toList();

        for (Player target : targets) {
            double newHealth = Math.max(0, target.getHealth() - this.artefactConfig.groundPoundDamage);
            target.setHealth(newHealth);

            Vector knockback = target.getLocation().toVector()
                    .subtract(impactLocation.toVector())
                    .normalize()
                    .multiply(2.0)
                    .setY(0.6);
            target.setVelocity(knockback);

            noticeService.create()
                    .notice(messages -> messages.airAbilitySlamDown)
                    .player(target.getUniqueId())
                    .placeholder("{player}", player.getName())
                    .send();
        }

        // Particle & sound effects
        for (int i = 0; i < 360; i += 10) {
            double radians = Math.toRadians(i);
            Location particleLocation = impactLocation.clone().add(
                    Math.cos(radians) * artefactConfig.groundPoundRadius,
                    0.5,
                    Math.sin(radians) * artefactConfig.groundPoundRadius
            );
            impactLocation.getWorld().spawnParticle(Particle.EXPLOSION, particleLocation, 1);
        }

        impactLocation.getWorld().playSound(impactLocation, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        impactLocation.getWorld().playSound(impactLocation, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5f, 1.0f);
    }


    private Location findSafeLocation(Location location) {
        while (location.getBlock().getType().isSolid() && location.getY() < 256) {
            location.add(0, 1, 0);
        }
        return location;
    }

    private void createDashEffect(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);

        for (double i = 0; i < distance; i += 0.3) {
            Location particleLocation = start.clone().add(direction.clone().multiply(i));
            start.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLocation, 2, 0.1, 0.1, 0.1, 0.1);
        }
    }

}