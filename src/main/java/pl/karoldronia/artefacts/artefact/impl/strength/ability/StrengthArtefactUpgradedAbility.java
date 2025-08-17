package pl.karoldronia.artefacts.artefact.impl.strength.ability;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.strength.StrengthArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StrengthArtefactUpgradedAbility implements Ability, Listener {

    private final Plugin plugin;
    private final StrengthArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    private final Map<UUID, Boolean> activeWindows;
    private final Map<UUID, Integer> hitsCount;

    public StrengthArtefactUpgradedAbility(Plugin plugin, StrengthArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;

        this.activeWindows = ExpiringMap.builder()
                .expiration(this.artefactConfig.upgradedAbilityWindow.toMillis(), TimeUnit.MILLISECONDS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .build();

        this.hitsCount = ExpiringMap.builder()
                .expiration(this.artefactConfig.upgradedAbilityWindow.toMillis(), TimeUnit.MILLISECONDS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .build();

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        UUID uniqueId = player.getUniqueId();

        this.activeWindows.put(uniqueId, Boolean.TRUE);
        this.hitsCount.put(uniqueId, 0);

        this.noticeService.create()
                .notice(messages -> messages.upgradedStrengthAbility)
                .player(uniqueId)
                .send();

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);

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

    @EventHandler(ignoreCancelled = true)
    void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player player)) return;

        UUID id = player.getUniqueId();

        if (!Boolean.TRUE.equals(this.activeWindows.get(id))) return;

        Integer hits = this.hitsCount.get(id);
        int current = (hits == null ? 0 : hits);

        if (current >= this.artefactConfig.maxHits) return;

        int ticks = (int) Math.max(1, this.artefactConfig.perHitBuffDuration.toMillis() / 50L);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                ticks,
                2,
                false,
                true,
                true
        ));

        this.hitsCount.put(id, current + 1);

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.4f);
    }
}