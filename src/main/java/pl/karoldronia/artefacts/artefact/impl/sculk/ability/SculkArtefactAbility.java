package pl.karoldronia.artefacts.artefact.impl.sculk.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.sculk.SculkArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public class SculkArtefactAbility implements Ability {

    private final Plugin plugin;
    private final SculkArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public SculkArtefactAbility(Plugin plugin, SculkArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "sculk";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        List<Player> targets = this.getTargets(player, profile, this.artefactConfig.abilityRadius);

        this.executeWardenBlast(player, targets);
        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.sculkAbility)
                .placeholder("{affected}", String.valueOf(targets.size()))
                .send();

        return AbilityResult.SUCCESS;
    }

    private void executeWardenBlast(Player player, List<Player> targets) {
        Location center = player.getLocation();

        player.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, center.add(0, 1, 0), 3, 1, 1, 1, 0.1);
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, center, 2, 3, 2, 3);

        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 30, 2, 2, 2, 0.3);

        for (Player target : targets) {
            double newHealth = Math.max(0, target.getHealth() - this.artefactConfig.abilityDamage);
            target.setHealth(newHealth);

            Vector knockback = target.getLocation().toVector().subtract(center.toVector()).normalize();
            knockback.setY(0.3); // Slight upward component
            knockback.multiply(1.2); // Moderate knockback strength
            target.setVelocity(knockback);

            // Sound and particle effects on target
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WARDEN_HURT, 1.0f, 1.0f);
            target.getWorld().spawnParticle(
                    Particle.BLOCK,
                    target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1,
                    Material.SCULK.createBlockData()
            );

            target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, true, false));
        }

        createSculkWave(center);
    }

    private void createSculkWave(Location center) {
        new BukkitRunnable() {
            private double radius = 1.0;
            private final double maxRadius = artefactConfig.abilityRadius;

            @Override
            public void run() {
                if (this.radius > this.maxRadius) {
                    this.cancel();
                    return;
                }

                for (int angle = 0; angle < 360; angle += 15) {
                    double radians = Math.toRadians(angle);
                    double x = center.getX() + this.radius * Math.cos(radians);
                    double z = center.getZ() + this.radius * Math.sin(radians);
                    Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);

                    center.getWorld().spawnParticle(
                            Particle.SONIC_BOOM,
                            particleLoc, 2, 0.1, 0.1, 0.1, 0.05
                    );
                }

                this.radius++;
            }
        }.runTaskTimer(this.plugin, 0L, 2L);
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }
}