package pl.karoldronia.artefacts.artefact.impl.ice.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.ice.IceArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class IceArtefactAbility implements Ability {

    private final IceArtefactConfig artefactConfig;
    private final Plugin plugin;
    private final NoticeService noticeService;

    public IceArtefactAbility(IceArtefactConfig artefactConfig, Plugin plugin, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.plugin = plugin;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "ice";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 50, 2, 2, 2, 0.1);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 30, 2, 1, 2, 0.05);

        List<Player> targets = this.getTargets(player, profile, this.artefactConfig.abilityRadius);

        this.launchIceSpikes(player, targets);
        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.iceAbility)
                .placeholder("{affected}", String.valueOf(targets.size()))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

    private void launchIceSpikes(Player player, List<Player> targets) {
        List<Player> targetsCopy = new ArrayList<>(targets);

        int spikeCount = this.artefactConfig.spikeCount;
        for (int i = 0; i < Math.min(spikeCount, targetsCopy.size() + 4); i++) {
            int finalI = i;
            new BukkitRunnable() {
                private int ticks = 0;
                private final Location startLoc = player.getLocation().clone();
                private final Vector direction;

                {
                    if (finalI < targetsCopy.size()) {
                        // Target specific players
                        direction = targetsCopy.get(finalI).getLocation().toVector().subtract(startLoc.toVector()).normalize();
                    } else {
                        // Random directions for extra spikes
                        double angle = (2 * Math.PI * finalI) / spikeCount;
                        direction = new Vector(Math.cos(angle), 0.1, Math.sin(angle)).normalize();
                    }
                }

                @Override
                public void run() {
                    ticks++;

                    Location currentLoc = startLoc.clone().add(direction.clone().multiply(ticks * 0.8));
                    createIceSpike(currentLoc);

                    for (Player target : targetsCopy) {
                        if (target.getLocation().distance(currentLoc) < 1.5) {
                            // Launch player 15 blocks up with strong velocity
                            Vector launchVector = new Vector(0, 1.5, 0); // Strong upward velocity for 15 blocks
                            target.setVelocity(launchVector);

                            // Deal 3 hearts (6 HP) damage
                            double newHealth = Math.max(0, target.getHealth() - artefactConfig.spikeDamage);
                            target.setHealth(newHealth);

                            // Apply slowness effect
                            target.addPotionEffect(
                                    new PotionEffect(PotionEffectType.SLOWNESS,
                                            60, 4, true, false)
                            );

                            // Sound and particle effects
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);
                            target.getWorld().spawnParticle(
                                    Particle.BLOCK,
                                    target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1,
                                    Material.ICE.createBlockData()
                            );

                            // Create ice pillar effect at launch point
                            createIcePillar(target.getLocation().clone());

                            targetsCopy.remove(target);
                            this.cancel();
                            return;
                        }
                    }

                    // Stop after 10 blocks or if hit something
                    if (ticks > 12 || currentLoc.getBlock().getType().isSolid()) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(this.plugin, i * 2L, 2L);
        }
    }

    private void createIceSpike(Location location) {
        location.getWorld().spawnParticle(
                Particle.BLOCK,
                location,
                8, 0.3, 0.3, 0.3, 0.1,
                Material.PACKED_ICE.createBlockData()
        );
        location.getWorld().spawnParticle(Particle.SNOWFLAKE, location, 3, 0.2, 0.2, 0.2, 0.05);

        Block block = location.getBlock();
        if (block.getType() == Material.AIR) {
            block.setType(Material.ICE);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getType() == Material.ICE) {
                        block.setType(Material.AIR);
                        block.getWorld().spawnParticle(
                                Particle.BLOCK,
                                block.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1,
                                Material.ICE.createBlockData()
                        );
                    }
                }
            }.runTaskLater(this.plugin, 40L); // Remove after 2 seconds
        }
    }

    private void createIcePillar(Location location) {
        // Create a temporary ice pillar for visual effect
        for (int y = 0; y < 3; y++) {
            Location pillarLoc = location.clone().add(0, y, 0);
            Block block = pillarLoc.getBlock();

            if (block.getType() == Material.AIR) {
                block.setType(Material.PACKED_ICE);

                // Spawn particles around the pillar
                pillarLoc.getWorld().spawnParticle(
                        Particle.SNOWFLAKE,
                        pillarLoc.add(0.5, 0.5, 0.5),
                        5, 0.3, 0.3, 0.3, 0.05
                );

                // Remove the pillar after 1.5 seconds
                final Block finalBlock = block;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (finalBlock.getType() == Material.PACKED_ICE) {
                            finalBlock.setType(Material.AIR);
                            finalBlock.getWorld().spawnParticle(
                                    Particle.BLOCK,
                                    finalBlock.getLocation().add(0.5, 0.5, 0.5),
                                    8, 0.3, 0.3, 0.3, 0.1,
                                    Material.PACKED_ICE.createBlockData()
                            );
                        }
                    }
                }.runTaskLater(this.plugin, 30L);
            }
        }
    }
}