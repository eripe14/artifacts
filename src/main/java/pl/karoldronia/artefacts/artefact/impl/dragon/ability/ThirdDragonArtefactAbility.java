package pl.karoldronia.artefacts.artefact.impl.dragon.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.ability.dragon.DragonAbility;
import pl.karoldronia.artefacts.artefact.ability.dragon.DragonAbilityTrigger;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ThirdDragonArtefactAbility implements DragonAbility, Listener {

    private final DragonArtefactConfig artefactConfig;
    private final NoticeService noticeService;
    private final Plugin plugin;

    private final Map<DragonFireball, Player> dragonFireballs = new HashMap<>();
    private final Set<Location> dragonBreathAreas = new HashSet<>();

    public ThirdDragonArtefactAbility(
            DragonArtefactConfig artefactConfig,
            NoticeService noticeService,
            Plugin plugin
    ) {
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
        this.plugin = plugin;

        if (this.plugin.isEnabled()) {
            this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public DragonAbilityTrigger getDragonTrigger() {
        return DragonAbilityTrigger.PPM;
    }

    @Override
    public String getId() {
        return "dragon_third";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().multiply(2);

        DragonFireball dragonFireball = player.getWorld().spawn(eyeLoc.add(direction.clone().multiply(0.5)), DragonFireball.class);
        dragonFireball.setShooter(player);
        dragonFireball.setDirection(direction);
        dragonFireball.setYield(0); // Brak zniszczenia bloków
        dragonFireball.setIsIncendiary(false); // Brak podpalania

        this.dragonFireballs.put(dragonFireball, player);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 1.0f);
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, eyeLoc, 20, 0.5, 0.5, 0.5, 0.1);

        this.noticeService.create()
                .notice(messages -> messages.thirdDragonAbility)
                .player(player.getUniqueId())
                .send();

        return AbilityResult.SUCCESS;
    }

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof DragonFireball fireball)) return;

        if (!dragonFireballs.containsKey(fireball)) return;

        Player shooter = dragonFireballs.get(fireball);
        dragonFireballs.remove(fireball);

        Location hitLoc = fireball.getLocation();

        // Zadaj obrażenia bezpośrednie (4 serca = 8 HP)
        if (event.getHitEntity() instanceof Player target) {
            if (!target.equals(shooter)) {
                double newHealth = Math.max(0, target.getHealth() - artefactConfig.dragonFireballDamage);
                target.setHealth(newHealth);
            }
        }

        // Stwórz obszar Dragon Breath
        createDragonBreathArea(hitLoc, shooter);
    }

    private void createDragonBreathArea(Location centerLoc, Player shooter) {
        dragonBreathAreas.add(centerLoc.clone());

        centerLoc.getWorld().playSound(centerLoc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 2.0f, 0.8f);
        centerLoc.getWorld().spawnParticle(Particle.EXPLOSION, centerLoc, 3, 1, 0.5, 1, 0);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = artefactConfig.thirdAbilityDragonBreathDuration * 20;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    dragonBreathAreas.remove(centerLoc);
                    centerLoc.getWorld().spawnParticle(Particle.SMOKE, centerLoc, 20, 2, 1, 2, 0.1);
                    cancel();
                    return;
                }

                if (ticks % 20 == 0) {
                    dealDragonBreathDamage(centerLoc, shooter);
                }

                if (ticks % 5 == 0) {
                    spawnDragonBreathParticles(centerLoc);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void dealDragonBreathDamage(Location centerLoc, Player shooter) {
        for (Entity entity : centerLoc.getWorld().getNearbyEntities(centerLoc, 3, 2, 3)) {
            if (entity instanceof Player target && !entity.equals(shooter)) {
                if (target.getLocation().distance(centerLoc) <= 3) {
                    double newHealth = Math.max(0, target.getHealth() - artefactConfig.thirdAbilityDragonBreathDamage);
                    target.setHealth(newHealth);

                    target.getWorld().spawnParticle(
                            Particle.DRAGON_BREATH,
                            target.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.1
                    );
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.5f, 1.2f);
                }
            }
        }
    }

    private void spawnDragonBreathParticles(Location centerLoc) {
        centerLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, centerLoc, 15, 2, 0.5, 2, 0.1);

        centerLoc.getWorld().spawnParticle(Particle.SMOKE, centerLoc, 8, 1.5, 0.3, 1.5, 0.05);
        centerLoc.getWorld().spawnParticle(Particle.WITCH, centerLoc, 5, 2, 0.5, 2, 0.1);

        if (Math.random() < 0.1) { // 10% szansy co wywołanie
            centerLoc.getWorld().playSound(centerLoc, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.3f, 1.5f);
        }
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.thirdAbilityCooldown;
    }

    @Override
    public void onDisable() {
        this.dragonFireballs.clear();
        this.dragonBreathAreas.clear();
    }
}