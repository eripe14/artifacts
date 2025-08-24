package pl.karoldronia.artefacts.artefact.impl.life.ability;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.life.LifeArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LifeArtefactUpgradedAbility implements Ability, Listener {

    private final Plugin plugin;
    private final LifeArtefactConfig artefactConfig;
    private final NoticeService noticeService;
    private final Map<UUID, Double> originalHealths;

    public LifeArtefactUpgradedAbility(Plugin plugin, LifeArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
        this.originalHealths = ExpiringMap.builder()
                .expiration(this.artefactConfig.upgradedAbilityHealthRestoreDuration.toSeconds(), TimeUnit.SECONDS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expirationListener((key, value) -> {
                    UUID uuid = UUID.fromString(key.toString());
                    double originalHealth = Double.parseDouble(value.toString());

                    Bukkit.getScheduler().runTask(this.plugin, () -> restoreMaxHealth(uuid, originalHealth));
                })
                .build();

        if (this.plugin.isEnabled()) {
            Bukkit.getPluginManager().registerEvents(this, this.plugin);
        }
    }

    @Override
    public String getId() {
        return "life-upgraded";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        List<Player> targets = this.getTargets(player, profile, this.artefactConfig.upgradedAbilityRadius);

        for (Player target : targets) {
            this.applyClamp(target, this.artefactConfig.upgradedAbilityHealthAmount);
        }

        this.noticeService.create()
                .notice(messages -> messages.lifeUpgradedAbility)
                .player(player.getUniqueId())
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

    @Override
    public void onDisable() {
        this.restoreAllAndClear();
    }

    private void applyClamp(Player player, int hearts) {
        if (player == null || !player.isOnline() || hearts <= 0) return;

        double targetHp = hearts * 2.0;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;

        this.originalHealths.computeIfAbsent(player.getUniqueId(), k -> attr.getBaseValue());

        double newMax = Math.min(attr.getBaseValue(), targetHp);
        attr.setBaseValue(newMax);
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }

        this.originalHealths.put(player.getUniqueId(), this.originalHealths.get(player.getUniqueId()));
    }

    private void restoreMaxHealth(UUID uuid, Double originalBase) {
        if (uuid == null || originalBase == null) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (attr != null) {
                attr.setBaseValue(originalBase);
                if (player.getHealth() > originalBase) {
                    player.setHealth(originalBase);
                }
            }
        }

        this.originalHealths.remove(uuid);
    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        Double original = originalHealths.get(id);
        if (original != null) {
            restoreMaxHealth(id, original);
        }
    }

    public void restoreAllAndClear() {
        for (UUID id : originalHealths.keySet()) {
            Double original = originalHealths.get(id);
            if (original != null) {
                restoreMaxHealth(id, original);
            }
        }
        originalHealths.clear();
    }
}