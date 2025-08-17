package pl.karoldronia.artefacts.artefact.impl.fire.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.ability.charging.ChargingAbility;
import pl.karoldronia.artefacts.artefact.impl.fire.FireArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;

public class FireArtefactUpgradedAbility implements ChargingAbility, Listener {

    private final FireArtefactConfig artefactConfig;
    private final NoticeService noticeService;
    private final NamespacedKey projectileKey;

    public FireArtefactUpgradedAbility(Plugin plugin, FireArtefactConfig artefactConfig, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.projectileKey = new NamespacedKey(plugin, "fireball_projectile");
        this.noticeService = noticeService;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Vector direction = player.getLocation().getDirection().normalize().multiply(1.2);

        SmallFireball smallFireball = player.launchProjectile(SmallFireball.class, direction);
        smallFireball.setIsIncendiary(false);
        smallFireball.setYield(0);
        smallFireball.getPersistentDataContainer().set(projectileKey, PersistentDataType.BYTE, (byte) 1);

        player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_FIRECHARGE_USE, 0.9f, 1.4f);

        this.noticeService.create()
                .notice(messages -> messages.upgradedFireAbility)
                .player(player.getUniqueId())
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
    public Duration getChargingDuration() {
        return this.artefactConfig.upgradedAbilityChargingDuration;
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof SmallFireball fireball)) return;

        Byte tag = fireball.getPersistentDataContainer().get(this.projectileKey, PersistentDataType.BYTE);
        if (tag == null || tag != (byte) 1) return;

        event.setDamage(this.artefactConfig.fireballDamage);
    }
}