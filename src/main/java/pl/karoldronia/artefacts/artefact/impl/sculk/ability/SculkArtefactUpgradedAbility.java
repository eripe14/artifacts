package pl.karoldronia.artefacts.artefact.impl.sculk.ability;

import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.visibility.VisibilityChanger;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.sculk.SculkArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SculkArtefactUpgradedAbility implements Ability {

    private final SculkArtefactConfig artefactConfig;
    private final Plugin plugin;
    private final NoticeService noticeService;
    private final List<SculkDomain> activeDomains = new ArrayList<>();

    public SculkArtefactUpgradedAbility(SculkArtefactConfig artefactConfig, Plugin plugin, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.plugin = plugin;
        this.noticeService = noticeService;
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Location center = player.getLocation();

        player.getWorld().playSound(center, Sound.ENTITY_WARDEN_EMERGE, 2.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, center.add(0, 1, 0), 100, 5, 2, 5, 0.3);

        SculkDomain domain = new SculkDomain(center, player);
        this.activeDomains.add(domain);
        domain.activate();

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.sculkDomainCreated)
                .send();

        new BukkitRunnable() {
            @Override
            public void run() {
                domain.deactivate();
                activeDomains.remove(domain);
            }
        }.runTaskLater(this.plugin, 600L);

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.upgradedAbilityCooldown;
    }

    private class SculkDomain {
        private final Location center;
        private final Player owner;
        private final int domainSize = 5; // 10x10 area (radius 5)
        private BukkitTask domainTask;
        private boolean isActive = false;

        public SculkDomain(Location center, Player owner) {
            this.center = center.clone();
            this.owner = owner;
        }

        public void activate() {
            this.isActive = true;
            createDomainBoundaries();
            startDomainEffects();
        }

        public void deactivate() {
            this.isActive = false;
            if (domainTask != null) {
                domainTask.cancel();
            }

            center.getWorld().playSound(center, Sound.ENTITY_WARDEN_DEATH, 1.5f, 0.8f);
            center.getWorld().spawnParticle(Particle.SONIC_BOOM, center, 50, 5, 2, 5, 0.2);
        }

        private void createDomainBoundaries() {
            new BukkitRunnable() {
                private int tick = 0;

                @Override
                public void run() {
                    if (!isActive || tick > 600) { // Stop after 30 seconds
                        this.cancel();
                        return;
                    }

                    if (tick % 10 == 0) {
                        createBoundaryParticles();
                    }

                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        private void createBoundaryParticles() {
            for (int x = -domainSize; x <= domainSize; x++) {
                for (int z = -domainSize; z <= domainSize; z++) {
                    if (Math.abs(x) == domainSize || Math.abs(z) == domainSize) {
                        Location wallLoc = center.clone().add(x, 0, z);
                        for (int y = 0; y < 4; y++) {
                            wallLoc.add(0, y, 0);
                            center.getWorld().spawnParticle(
                                    Particle.SONIC_BOOM,
                                    wallLoc, 1, 0.1, 0.1, 0.1, 0.02
                            );
                            wallLoc.subtract(0, y, 0);
                        }
                    }
                }
            }
        }

        private void startDomainEffects() {
            BukkitRunnable task = new BukkitRunnable() {
                private int invisTick = 0;

                @Override
                public void run() {
                    if (!isActive) {
                        this.cancel();
                        return;
                    }

                    List<Player> playersInDomain = getPlayersInDomain();

                    for (Player player : playersInDomain) {
                        // Prevent escape - push back if trying to leave
                        if (isAtBoundary(player.getLocation())) {
                            Vector pushBack = center.toVector().subtract(player.getLocation().toVector()).normalize();
                            pushBack.setY(0);
                            pushBack.multiply(0.5);
                            player.setVelocity(pushBack);

                            player.getWorld().spawnParticle(
                                    Particle.BLOCK_MARKER,
                                    player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1,
                                    Bukkit.createBlockData(Material.BARRIER)
                            );
                        }

                        // Owner invisibility cycle
                        VisibilityChanger visibilityChanger = VanishAPI.getPlugin().getVisibilityChanger();
                        if (player.equals(owner)) {
                            if (invisTick % 60 < 40) { // 2 seconds invisible (40 ticks)
                                if (!VanishAPI.isInvisible(owner)) {
                                    VanishAPI.hidePlayer(owner);
                                }
                                return;
                            }

                            if (VanishAPI.isInvisible(owner)) {
                                VanishAPI.showPlayer(owner);
                            }
                        }
                    }

                    invisTick++;
                }
            };

            domainTask = task.runTaskTimer(plugin, 0L, 1L);
        }

        private List<Player> getPlayersInDomain() {
            List<Player> players = new ArrayList<>();
            for (Player player : center.getWorld().getPlayers()) {
                if (isInDomain(player.getLocation())) {
                    players.add(player);
                }
            }
            return players;
        }

        private boolean isInDomain(Location loc) {
            return Math.abs(loc.getX() - center.getX()) <= domainSize &&
                    Math.abs(loc.getZ() - center.getZ()) <= domainSize &&
                    Math.abs(loc.getY() - center.getY()) <= 10; // Height limit
        }

        private boolean isAtBoundary(Location loc) {
            double distX = Math.abs(loc.getX() - center.getX());
            double distZ = Math.abs(loc.getZ() - center.getZ());
            return distX >= domainSize - 0.5 || distZ >= domainSize - 0.5;
        }
    }

    @Override
    public boolean requireUpgrade() {
        return true;
    }
}