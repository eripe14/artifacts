package pl.karoldronia.artefacts.artefact;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import pl.karoldronia.artefacts.ArtefactsPlugin;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.artefact.item.crafting.CraftingService;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;
import pl.karoldronia.artefacts.scheduler.Scheduler;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArtefactController implements Listener {

    private final Scheduler scheduler;
    private final ProfileRepository profileRepository;
    private final ArtefactService artefactService;
    private final NoticeService noticeService;

    private final Map<UUID, List<ItemStack>> pendingRestoration = new ConcurrentHashMap<>();

    public ArtefactController(
            Scheduler scheduler,
            ProfileRepository profileRepository,
            ArtefactService artefactService,
            NoticeService noticeService
    ) {
        this.scheduler = scheduler;
        this.profileRepository = profileRepository;
        this.artefactService = artefactService;
        this.noticeService = noticeService;
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Profile profile = this.profileRepository.findOrCreate(player);
        if (!profile.getArtefactId().isEmpty()) {
            return;
        }

        Artefact randomArtefact = this.artefactService.getRandomArtefact(null);
        player.clearActivePotionEffects();

        ItemStack artefactItem = randomArtefact.getArtefactItem().build(randomArtefact.getId());
        player.getInventory().addItem(artefactItem);

        profile.setArtefactId(randomArtefact.getId());
        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.artifactAssigned)
                .placeholder("{id}", randomArtefact.getId())
                .send();

        profile.save();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> protectedItems = new ArrayList<>();

        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();

            // Check if it's an artefact item
            if (this.isArtefactItem(item)) {
                ArtefactItemsUtil.getArtefactId(item).ifPresent(artefactId -> {
                    // Only protect artefacts that are NOT Dragon Artefacts
                    if (!artefactId.equalsIgnoreCase(DragonArtefact.ID)) {
                        iterator.remove();
                        protectedItems.add(item);
                    }
                    // Dragon Artefacts will drop naturally
                });
            }
            // Check if it's an upgrader item
            else if (this.isUpgraderItem(item)) {
                // Let upgraders drop - don't remove from drops, don't add to protected items
                // This means upgraders will drop naturally on death
            }
            // Trader items will be handled by existing logic (if you want them protected,
            // you'd need to add similar logic as for artefacts)
        }

        if (protectedItems.isEmpty()) {
            return;
        }

        this.pendingRestoration.put(player.getUniqueId(), protectedItems);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        List<ItemStack> protectedItems = this.pendingRestoration.remove(uniqueId);
        if (protectedItems == null || protectedItems.isEmpty()) {
            return;
        }

        this.scheduler.laterSync(() -> {
            if (player.isOnline()) {
                this.restoreProtectedItems(player, protectedItems);
            }
        }, Duration.ofMillis(100));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinKeepArtefact(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        List<ItemStack> protectedItems = this.pendingRestoration.remove(uniqueId);
        if (protectedItems == null || protectedItems.isEmpty()) {
            return;
        }

        this.scheduler.laterSync(() -> {
            if (player.isOnline()) {
                this.restoreProtectedItems(player, protectedItems);
            }
        }, Duration.ofMillis(100));
    }

    private boolean isArtefactItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.has(ArtefactsPlugin.ARTEFACT_ITEM_KEY);
    }

    private boolean isUpgraderItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.has(CraftingService.UPGRADER_ITEM_KEY);
    }

    private boolean isTraderItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.has(CraftingService.TRADER_ITEM_KEY);
    }

    private void restoreProtectedItems(Player player, List<ItemStack> protectedItems) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> couldntFit = new ArrayList<>();

        for (ItemStack item : protectedItems) {
            HashMap<Integer, ItemStack> leftover = inventory.addItem(item);

            if (!leftover.isEmpty()) {
                couldntFit.addAll(leftover.values());
            }
        }

        if (!couldntFit.isEmpty()) {
            Location dropLocation = player.getLocation();
            for (ItemStack item : couldntFit) {
                player.getWorld().dropItemNaturally(dropLocation, item);
            }
        }
    }
}