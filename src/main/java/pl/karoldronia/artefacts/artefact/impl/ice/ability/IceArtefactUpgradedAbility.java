package pl.karoldronia.artefacts.artefact.impl.ice.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.ice.IceArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public class IceArtefactUpgradedAbility implements Ability {

    private final IceArtefactConfig artefactConfig;
    private final Plugin plugin;
    private final NoticeService noticeService;

    public IceArtefactUpgradedAbility(IceArtefactConfig artefactConfig, Plugin plugin, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.plugin = plugin;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "ice-upgraded";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        List<Player> freezeTargets = getTargets(player, profile, this.artefactConfig.upgradedAbilityRadius);
        this.createFreezeZone(player, freezeTargets);

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.iceUpgradedAbility)
                .placeholder("{affected}", String.valueOf(freezeTargets.size()))
                .send();
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

    private void createFreezeZone(Player player, List<Player> freezeTargets) {
        Location center = player.getLocation();
        double radius = this.artefactConfig.upgradedAbilityRadius;
        int freezeDuration = this.artefactConfig.freezeDuration;

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;

                // Create expanding ice circle
                for (int angle = 0; angle < 360; angle += 15) {
                    double radians = Math.toRadians(angle);
                    double x = center.getX() + radius * Math.cos(radians);
                    double z = center.getZ() + radius * Math.sin(radians);
                    Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);

                    center.getWorld().spawnParticle(Particle.BLOCK, particleLoc, 3, 0.1, 0.1, 0.1, 0.05,
                            Material.ICE.createBlockData());
                    center.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
                }

                if (ticks >= freezeDuration * 4) { // 20 ticks per second / 5 = 4 ticks per quarter second
                    this.cancel();
                }
            }
        }.runTaskTimer(this.plugin, 0L, 5L);


        for (Player target : freezeTargets) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, freezeDuration * 20, 255, true, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, freezeDuration * 20, -10, true, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, freezeDuration * 20, 0, true, false));

            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 2.0f, 0.5f);

            new BukkitRunnable() {
                private int freezeTicks = 0;

                @Override
                public void run() {
                    freezeTicks++;

                    target.getWorld().spawnParticle(
                            Particle.SNOWFLAKE,
                            target.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.02
                    );
                    target.getWorld().spawnParticle(
                            Particle.BLOCK,
                            target.getLocation().add(0, 0.5, 0), 3, 0.3, 0.5, 0.3, 0.05,
                            Material.ICE.createBlockData()
                    );

                    if (freezeTicks >= freezeDuration * 4) {
                        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
                        target.getWorld().spawnParticle(
                                Particle.BLOCK,
                                target.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.1,
                                Material.ICE.createBlockData()
                        );
                        this.cancel();
                    }
                }
            }.runTaskTimer(this.plugin, 0L, 5L);
        }
    }

}