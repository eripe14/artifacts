package pl.karoldronia.artefacts.artefact.impl.dragon.ability;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
import java.util.HashSet;
import java.util.Set;

public class SecondDragonArtefactAbility implements DragonAbility, Listener {

    private final Plugin plugin;
    private final DragonArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    private final Set<Player> launchedPlayers = new HashSet<>();

    public SecondDragonArtefactAbility(
            Plugin plugin,
            DragonArtefactConfig artefactConfig,
            NoticeService noticeService
    ) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;

        if (this.plugin.isEnabled()) {
            this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        }
    }

    @Override
    public String getId() {
        return "dragon_second";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        this.launchedPlayers.add(player);

        Vector velocity = new Vector(0, 3.5, 0); // ~60 bloków wysokości
        player.setVelocity(velocity);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 30, 1, 1, 1, 0.1);

        this.noticeService.create()
                .notice(messages -> messages.secondDragonAbility)
                .player(player.getUniqueId())
                .send();

        new BukkitRunnable() {
            private int ticks = 0;
            private boolean wasInAir = false;

            @Override
            public void run() {
                if (!player.isOnline() || !launchedPlayers.contains(player)) {
                    cancel();
                    return;
                }

                if (!player.isOnGround()) {
                    wasInAir = true;
                } else if (wasInAir) {
                    performLandingExplosion(player);
                    launchedPlayers.remove(player);
                    cancel();
                    return;
                }

                ticks++;
                // Timeout po 10 sekundach
                if (ticks > 200) {
                    launchedPlayers.remove(player);
                    cancel();
                }
            }
        }.runTaskTimer(this.plugin, 0L, 1L);

        return AbilityResult.SUCCESS;
    }

    private void performLandingExplosion(Player player) {
        Location loc = player.getLocation();

        // Efekty wizualne eksplozji
        player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 1, 0.5, 1, 0);
        player.getWorld().spawnParticle(Particle.FLAME, loc, 50, 2, 1, 2, 0.1);
        player.getWorld().spawnParticle(Particle.SMOKE, loc, 30, 2, 1, 2, 0.1);
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 40, 2, 1, 2, 0.1);

        // Efekt dźwiękowy
        player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.0f);

        // Zadawanie obrażeń w obszarze 3x3
        for (Entity entity : player.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            if (entity instanceof Player target && !entity.equals(player)) {
                if (target.getLocation().distance(loc) <= 3) {
                    double newHealth = Math.max(0, target.getHealth() - artefactConfig.secondAbilitySmashDamage);
                    target.setHealth(newHealth);

                    target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                            target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                }
            }
        }

        this.noticeService.create()
                .notice(messages -> messages.secondDragonAbilityLanding)
                .player(player.getUniqueId())
                .send();
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && this.launchedPlayers.contains(player)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.secondAbilityCooldown;
    }

    @Override
    public DragonAbilityTrigger getDragonTrigger() {
        return DragonAbilityTrigger.LPM_SHIFT;
    }

}