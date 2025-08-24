package pl.karoldronia.artefacts.artefact.impl.earth.ability;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.earth.EarthArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EarthArtefactUpgradedAbility implements Ability {

    private final Plugin plugin;
    private final EarthArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public EarthArtefactUpgradedAbility(Plugin plugin, EarthArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "earth-upgraded";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        List<Player> targets = this.getTargets(player, profile, this.artefactConfig.upgradedAbilityRadius);
        for (Player target : targets) {
            double newHealth = Math.max(0, target.getHealth() - this.artefactConfig.damage);
            target.setHealth(newHealth);
            target.setVelocity(new Vector(0, 0.75, 0));
        }

        this.playGroundSmashEffect(player, this.plugin);
        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.earthUpgradedAbility)
                .placeholder("{affected}", String.valueOf(targets.size()))
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

    public void playGroundSmashEffect(Player player, Plugin plugin) {
        World world = player.getWorld();
        Location center = player.getLocation();

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);

        world.spawnParticle(Particle.EXPLOSION, center, 1);
        world.spawnParticle(Particle.CLOUD, center, 40, 0.5, 0.1, 0.5, 0.02);

        Material material = Material.DIRT;

        for (int i = 0; i < 20; i++) {
            double angle = Math.toRadians(ThreadLocalRandom.current().nextDouble(0, 360));
            double radius = ThreadLocalRandom.current().nextDouble(1, 3);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location spawnLoc = center.clone().add(x, 1.2, z);

            FallingBlock falling = world.spawnFallingBlock(spawnLoc, material.createBlockData());
            Vector velocity = new Vector(x * 0.25, 0.6 + Math.random() * 0.4, z * 0.25);
            falling.setVelocity(velocity);
            falling.setDropItem(false);
            falling.setHurtEntities(false);
        }

        new BukkitRunnable() {
            int ticks = 0;
            final int max = 8;

            @Override
            public void run() {
                if (ticks++ >= max) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 360; i += 20) {
                    double radius = 1.5 + ticks * 0.5;
                    double angle = Math.toRadians(i);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location loc = center.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.BLOCK, loc, 8, 0.2, 0.1, 0.2, material.createBlockData());
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

}