package pl.karoldronia.artefacts.artefact.impl.ocean.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.ocean.OceanArtefactConfig;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public class OceanArtefactUpgradedAbility implements Ability {

    private final Plugin plugin;
    private final OceanArtefactConfig artefactConfig;

    public OceanArtefactUpgradedAbility(Plugin plugin, OceanArtefactConfig artefactConfig) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
    }

    @Override
    public String getId() {
        return "ocean-upgraded";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Location playerLocation = player.getLocation();
        boolean inWater = this.isInWater(playerLocation);

        double damage = inWater ? this.artefactConfig.waterDamage : this.artefactConfig.landDamage;

        // Perform the dive sequence
        this.performDolphinJump(player, profile, playerLocation, damage, inWater);

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

    private boolean isInWater(Location location) {
        Material blockType = location.getBlock().getType();
        Material belowType = location.clone().subtract(0, 1, 0).getBlock().getType();

        return blockType == Material.WATER ||
                belowType == Material.WATER ||
                blockType == Material.KELP ||
                blockType == Material.KELP_PLANT ||
                blockType == Material.SEAGRASS ||
                blockType == Material.TALL_SEAGRASS;
    }

    private void performDolphinJump(Player player, Profile profile, Location centerLocation, double damage, boolean inWater) {
        // Spawn dolphin at player location
        Location spawnLocation = centerLocation.clone().add(0, -1, 0); // Slightly below ground/water
        spawnLocation.setPitch(-90);

        Dolphin dolphin = (Dolphin) centerLocation.getWorld().spawnEntity(spawnLocation, EntityType.DOLPHIN);

        // Configure dolphin
        dolphin.setInvulnerable(true);
        dolphin.setSilent(false);
        dolphin.setAI(false);
        dolphin.setGravity(false);

        // Try to make dolphin bigger (if possible)
        try {
            dolphin.getAttribute(org.bukkit.attribute.Attribute.GENERIC_SCALE).setBaseValue(3.5);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Location rotatedLocation = spawnLocation.clone();
        rotatedLocation.setPitch(-90); // Head pointing straight up
        rotatedLocation.setYaw(centerLocation.getYaw()); // Face same direction as player
        dolphin.teleport(rotatedLocation);

        // Get targets before jump
        List<Player> targets = getTargets(player, profile, this.artefactConfig.upgradedEffectRadius);
        List<Player> hitTargets = targets.stream()
                .filter(target -> target.getLocation().distance(centerLocation) <= this.artefactConfig.upgradedEffectRadius)
                .toList();

        // Damage targets immediately
        for (Player target : hitTargets) {
            target.damage(damage, player);

            // Knockback effect
            Vector knockback = target.getLocation().toVector()
                    .subtract(centerLocation.toVector())
                    .normalize()
                    .multiply(1.5)
                    .setY(0.8);
            target.setVelocity(knockback);

            // Visual effect on target
            target.getWorld().spawnParticle(Particle.CRIT,
                    target.getLocation().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.1);

            String damageHearts = inWater ? "7 hearts" : "4 hearts";
            target.sendMessage("§c🐬 " + player.getName() + "'s dolphin attack hits you for " + damageHearts + "!");
        }

        // Perform the jump animation
        animateDolphinJump(dolphin, centerLocation, inWater);

        // Success message
        String hitMessage = hitTargets.isEmpty() ?
                "§bDolphin jump complete, but no enemies were hit!" :
                "§bDolphin jump hit " + hitTargets.size() + " enemies for " +
                        (inWater ? "7 hearts" : "4 hearts") + " each!";
        player.sendMessage(hitMessage);
    }

    private void animateDolphinJump(Dolphin dolphin, Location centerLocation, boolean inWater) {
        Location startLocation = dolphin.getLocation();

        // Play initial sound
        centerLocation.getWorld().playSound(centerLocation,
                inWater ? Sound.ENTITY_DOLPHIN_JUMP : Sound.ENTITY_DOLPHIN_SPLASH,
                2.0f, 1.0f);

        new BukkitRunnable() {
            private double progress = 0;
            private final double increment = 0.05; // Slower for more dramatic effect

            @Override
            public void run() {
                if (progress >= 1.0) {
                    // Jump complete - create final effects and remove dolphin
                    createFinalEffects(centerLocation, inWater);
                    dolphin.remove();
                    cancel();
                    return;
                }

                // Calculate jump position (parabolic arc)
                double height = Math.sin(progress * Math.PI) * 6.0;
                Location jumpLocation = startLocation.clone().add(0, height, 0);

                // Teleport dolphin to new position
                dolphin.teleport(jumpLocation);

                // Create trail particles
                if (inWater) {
                    jumpLocation.getWorld().spawnParticle(Particle.FALLING_WATER, jumpLocation, 3, 0.3, 0.3, 0.3, 0.1);
                    jumpLocation.getWorld().spawnParticle(Particle.DOLPHIN, jumpLocation, 2, 0.2, 0.2, 0.2, 0.05);

                    // Extra water splash at peak
                    if (progress > 0.4 && progress < 0.6) {
                        jumpLocation.getWorld().spawnParticle(Particle.SPLASH, jumpLocation, 5, 0.5, 0.5, 0.5, 0.2);
                    }
                } else {
                    jumpLocation.getWorld().spawnParticle(Particle.CLOUD, jumpLocation, 2, 0.2, 0.2, 0.2, 0.05);
                    jumpLocation.getWorld().spawnParticle(Particle.SWEEP_ATTACK, jumpLocation, 1, 0.1, 0.1, 0.1, 0.1);

                    // Dust particles when jumping from land
                    if (progress < 0.3) {
                        Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromRGB(139, 69, 19), 1.0f);
                        jumpLocation.getWorld().spawnParticle(Particle.DUST, jumpLocation, 3, 0.3, 0.1, 0.3, 0.0, dustOptions);
                    }
                }

                // Play dolphin sounds during jump
                if (progress == 0.3 || progress == 0.7) {
                    centerLocation.getWorld().playSound(jumpLocation, Sound.ENTITY_DOLPHIN_AMBIENT, 1.0f, 1.2f);
                }

                progress += increment;
            }
        }.runTaskTimer(this.plugin, 0L, 2L); // Every 2 ticks for smooth animation
    }

    private void createFinalEffects(Location location, boolean inWater) {
        if (inWater) {
            // Water effects
            location.getWorld().spawnParticle(Particle.SPLASH, location, 25, 2, 1, 2, 0.3);
            location.getWorld().spawnParticle(Particle.BUBBLE, location, 15, 1.5, 1, 1.5, 0.2);
            location.getWorld().spawnParticle(Particle.DOLPHIN, location, 10, 1, 1, 1, 0.1);

            // Water sounds
            location.getWorld().playSound(location, Sound.ENTITY_DOLPHIN_SPLASH, 1.5f, 1.0f);
            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.8f);
        } else {
            // Land effects
            Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromRGB(139, 69, 19), 1.0f);
            location.getWorld().spawnParticle(Particle.DUST, location, 20, 1.5, 0.5, 1.5, 0.0, dustOptions);
            location.getWorld().spawnParticle(Particle.EXPLOSION, location, 10, 1, 0.5, 1, 0.1);

            // Land sounds
            location.getWorld().playSound(location, Sound.ENTITY_DOLPHIN_HURT, 1.0f, 0.8f);
            location.getWorld().playSound(location, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.9f);
        }

        // Universal effects
        location.getWorld().spawnParticle(Particle.CRIT, location, 15, 1.5, 1, 1.5, 0.1);

        // Create expanding ring effect
        new BukkitRunnable() {
            private double radius = 0;
            private final double maxRadius = artefactConfig.upgradedEffectRadius;

            @Override
            public void run() {
                if (radius > maxRadius) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 360; i += 20) {
                    double radians = Math.toRadians(i);
                    Location particleLocation = location.clone().add(
                            Math.cos(radians) * radius,
                            0.1,
                            Math.sin(radians) * radius
                    );

                    Particle particle = inWater ? Particle.FALLING_WATER : Particle.CLOUD;
                    location.getWorld().spawnParticle(particle, particleLocation, 1, 0.1, 0.1, 0.1, 0.05);
                }

                radius += 0.3;
            }
        }.runTaskTimer(this.plugin, 5L, 3L); // Start after 5 ticks, every 3 ticks
    }
}