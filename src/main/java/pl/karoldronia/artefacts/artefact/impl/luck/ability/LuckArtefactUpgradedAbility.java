package pl.karoldronia.artefacts.artefact.impl.luck.ability;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.luck.LuckArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class LuckArtefactUpgradedAbility implements Ability, Listener {

    private static final Set<Material> CONSUMABLE_THROWABLES = EnumSet.of(
            Material.EXPERIENCE_BOTTLE,
            Material.ENDER_PEARL,
            Material.SNOWBALL,
            Material.EGG,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION,
            Material.CHORUS_FRUIT
    );

    private final Plugin plugin;
    private final LuckArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    private final Map<UUID, Boolean> luckyOwners;
    private final Map<UUID, Boolean> unluckyTargets;

    public LuckArtefactUpgradedAbility(Plugin plugin, LuckArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;

        long millis = this.artefactConfig.upgradedAbilityEffectDuration.toMillis();
        this.luckyOwners = ExpiringMap.builder()
                .expiration(millis, TimeUnit.MILLISECONDS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .build();
        this.unluckyTargets = ExpiringMap.builder()
                .expiration(millis, TimeUnit.MILLISECONDS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .build();

        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        this.luckyOwners.put(player.getUniqueId(), true);

        List<Player> targets = this.getTargets(player, profile, this.artefactConfig.upgradedAbilityRadius);
        for (Player target : targets) {
            this.unluckyTargets.put(target.getUniqueId(), true);
        }

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.upgradedLuckAbility)
                .placeholder("{affected}", String.valueOf(targets.size()))
                .send();

        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.8f, 1.2f);
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
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumed = event.getItem();
        if (consumed.getType() == Material.AIR) return;

        UUID id = player.getUniqueId();

        if (Boolean.TRUE.equals(luckyOwners.get(id))) {
            if (coinFlip()) {
                Bukkit.getScheduler().runTask(plugin, () -> giveBackOne(player, consumed.getType()));
                player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 8);
            }
            return;
        }

        if (Boolean.TRUE.equals(unluckyTargets.get(id))) {
            if (coinFlip()) {
                Bukkit.getScheduler().runTask(plugin, () -> takeExtraOne(player, consumed.getType()));
                player.spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 8);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        Material type = item.getType();
        if (!CONSUMABLE_THROWABLES.contains(type)) return;

        UUID id = player.getUniqueId();

        if (Boolean.TRUE.equals(luckyOwners.get(id))) {
            if (coinFlip()) {
                Bukkit.getScheduler().runTask(plugin, () -> giveBackOne(player, type));
                player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 8);
            }
            return;
        }

        if (Boolean.TRUE.equals(unluckyTargets.get(id))) {
            if (coinFlip()) {
                Bukkit.getScheduler().runTask(plugin, () -> takeExtraOne(player, type));
                player.spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 8);
            }
        }
    }

    private boolean coinFlip() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    private void giveBackOne(Player player, Material type) {
        player.getInventory().addItem(new ItemStack(type, 1));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
    }

    private void takeExtraOne(Player player, Material type) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() == type && main.getAmount() > 0) {
            main.setAmount(main.getAmount() - 1);
            player.getInventory().setItemInMainHand(main.getAmount() > 0 ? main : null);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 0.7f, 0.8f);
            return;
        }

        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() == type && off.getAmount() > 0) {
            off.setAmount(off.getAmount() - 1);
            player.getInventory().setItemInOffHand(off.getAmount() > 0 ? off : null);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 0.7f, 0.8f);
            return;
        }

        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack is = player.getInventory().getItem(slot);
            if (is != null && is.getType() == type && is.getAmount() > 0) {
                is.setAmount(is.getAmount() - 1);
                player.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 0.7f, 0.8f);
                return;
            }
        }
    }
}