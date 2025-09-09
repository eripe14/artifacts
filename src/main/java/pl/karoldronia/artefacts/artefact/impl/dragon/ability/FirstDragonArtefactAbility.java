package pl.karoldronia.artefacts.artefact.impl.dragon.ability;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.ability.dragon.DragonAbility;
import pl.karoldronia.artefacts.artefact.ability.dragon.DragonAbilityTrigger;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonBreathArea;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class FirstDragonArtefactAbility implements DragonAbility {

    private final Plugin plugin;
    private final DragonArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    private final Map<UUID, DragonBreathArea> activeBreathAreas = new ConcurrentHashMap<>();

    @Override
    public String getId() {
        return "dragon_first";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Location breathLocation = this.getBreathLocation(player);

        DragonBreathArea breathArea = new DragonBreathArea(
                player.getUniqueId(),
                (int) this.artefactConfig.dragonBreathDuration.toSeconds(),
                this.artefactConfig.dragonBreathDamage
        );

        this.activeBreathAreas.put(player.getUniqueId(), breathArea);
        this.startFollowingDragonBreath(breathArea);

        breathLocation.getWorld().playSound(breathLocation, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.firstDragonAbility)
                .send();

        return AbilityResult.SUCCESS;
    }

    private void startFollowingDragonBreath(DragonBreathArea breathFollower) {
        BukkitTask breathTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player player = Bukkit.getPlayer(breathFollower.getPlayerId());

            if (player == null || !player.isOnline()) {
                return;
            }

            if (!breathFollower.isActive()) {
                return;
            }

            Location playerLocation = player.getLocation();
            breathFollower.updateLocation(playerLocation);

            damageEntitiesAroundPlayer(player, breathFollower);
            spawnFollowingBreathParticles(playerLocation);

            if (breathFollower.getCurrentTick() % 3 == 0) {
                player.getWorld().playSound(
                        playerLocation,
                        Sound.ITEM_BOTTLE_FILL_DRAGONBREATH,
                        1.5f,
                        1.0f
                );
            }

            breathFollower.tick();
        }, 0L, 20L); // Co sekundę (20 ticków)

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            breathTask.cancel();
            this.activeBreathAreas.remove(breathFollower.getPlayerId());

            Player player = Bukkit.getPlayer(breathFollower.getPlayerId());
            if (player != null && player.isOnline()) {
                spawnEndParticles(player.getLocation());
            }

        }, this.artefactConfig.dragonBreathDuration.toSeconds() * 20L);
    }

    private void damageEntitiesAroundPlayer(Player player, DragonBreathArea breathFollower) {
        Location center = player.getLocation();
        double radius = 2.5; // 5x5 area = radius 2.5

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (entity.getUniqueId().equals(breathFollower.getPlayerId())) {
                continue;
            }

            if (isInBreathArea(center, entity.getLocation(), radius)) {
                double newHealth = Math.max(0, livingEntity.getHealth() - breathFollower.getDamagePerTick());
                livingEntity.setHealth(newHealth);
                spawnDamageParticles(entity.getLocation());
            }
        }
    }

    private boolean isInBreathArea(Location center, Location entityLoc, double radius) {
        double dx = Math.abs(center.getX() - entityLoc.getX());
        double dz = Math.abs(center.getZ() - entityLoc.getZ());
        double dy = Math.abs(center.getY() - entityLoc.getY());

        return dx <= radius && dz <= radius && dy <= 1.5;
    }

    private void spawnFollowingBreathParticles(Location playerLocation) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location particleLoc = playerLocation.clone().add(x, 0.1, z);
                playerLocation.getWorld().spawnParticle(
                        Particle.DRAGON_BREATH,
                        particleLoc,
                        4, // Count
                        0.4, 0.2, 0.4, // Offset
                        0.03 // Speed
                );

                if (Math.random() < 0.4) {
                    playerLocation.getWorld().spawnParticle(
                            Particle.SMOKE,
                            particleLoc,
                            2,
                            0.3, 0.1, 0.3,
                            0.02
                    );
                }

                if (Math.random() < 0.2) {
                    playerLocation.getWorld().spawnParticle(
                            Particle.FLAME,
                            particleLoc.add(0, 0.5, 0),
                            1,
                            0.2, 0.2, 0.2,
                            0.01
                    );
                }
            }
        }

        double time = System.currentTimeMillis() / 500.0;
        for (int i = 0; i < 8; i++) {
            double angle = (time + i * Math.PI / 4) % (2 * Math.PI);
            double x = Math.cos(angle) * 2.5;
            double z = Math.sin(angle) * 2.5;

            Location spiralLoc = playerLocation.clone().add(x, 1, z);
            playerLocation.getWorld().spawnParticle(
                    Particle.DRAGON_BREATH,
                    spiralLoc,
                    2,
                    0.1, 0.1, 0.1,
                    0.02
            );
        }
    }

    private void spawnDamageParticles(Location location) {
        location.getWorld().spawnParticle(
                Particle.DAMAGE_INDICATOR,
                location.add(0, 1, 0),
                4,
                0.4, 0.4, 0.4,
                0.1
        );
    }

    private void spawnEndParticles(Location location) {
        location.getWorld().spawnParticle(
                Particle.DRAGON_BREATH,
                location,
                50,
                2.5, 1, 2.5,
                0.1
        );

        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 1.2f);
    }

    private Location getBreathLocation(Player player) {
        Location eyeLocation = player.getEyeLocation();
        return eyeLocation.add(eyeLocation.getDirection().multiply(this.artefactConfig.dragonBreathLocationMultiplier));
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

    @Override
    public DragonAbilityTrigger getDragonTrigger() {
        return DragonAbilityTrigger.RPM;
    }

    @Override
    public void onDisable() {
        for (DragonBreathArea area : this.activeBreathAreas.values()) {
            area.setActive(false);
        }
        this.activeBreathAreas.clear();
    }
}