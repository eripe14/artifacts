package pl.karoldronia.artefacts.artefact.passive;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import pl.karoldronia.artefacts.ArtefactsPlugin;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;
import pl.karoldronia.artefacts.scheduler.Scheduler;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
public class ArtefactPassiveController implements Listener {

    private final ArtefactService artefactService;
    private final ArtefactPassiveService passiveManager;
    private final ProfileRepository profileRepository;
    private final Scheduler scheduler;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        passiveManager.onPlayerJoin(playerId);

        this.scheduler.async(() -> {
            Profile profile = this.profileRepository.findOrCreate(player);
            passiveManager.updatePlayerCache(playerId, profile.getArtefactId());

            // Natychmiastowe sprawdzenie po załadowaniu cache
            this.scheduler.sync(() -> passiveManager.checkPlayerImmediately(player));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        passiveManager.onPlayerQuit(playerId);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Krótki delay na respawn, potem natychmiast sprawdź
        this.scheduler.laterSync(() -> {
            passiveManager.invalidatePlayerCache(playerId);
            checkAndApplyEffectsImmediately(player);
        }, Duration.ofMillis(50));
    }

    private void checkAndApplyEffectsImmediately(Player player) {
        this.scheduler.async(() -> {
            Profile profile = this.profileRepository.findOrCreate(player);
            String artefactId = profile.getArtefactId();
            passiveManager.updatePlayerCache(player.getUniqueId(), artefactId);

            // Natychmiast sprawdź w sync
            this.scheduler.sync(() -> passiveManager.checkPlayerImmediately(player));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Natychmiastowe sprawdzenie po zmianie slotu
        this.scheduler.laterSync(() -> {
            passiveManager.invalidatePlayerCache(playerId);
            // DOŁÓŻ to:
            this.scheduler.async(() -> {
                Profile profile = this.profileRepository.findOrCreate(player);
                String artefactId = profile.getArtefactId();
                passiveManager.updatePlayerCache(playerId, artefactId);
                this.scheduler.sync(() -> passiveManager.checkPlayerImmediately(player));
            });
        }, Duration.ofMillis(50));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        this.scheduler.laterSync(() -> {
            passiveManager.invalidatePlayerCache(playerId);
            // DOŁÓŻ to:
            this.scheduler.async(() -> {
                Profile profile = this.profileRepository.findOrCreate(player);
                String artefactId = profile.getArtefactId();
                passiveManager.updatePlayerCache(playerId, artefactId);
                this.scheduler.sync(() -> passiveManager.checkPlayerImmediately(player));
            });
        }, Duration.ofMillis(50));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.isCancelled()) return;

        // Sprawdź czy to main hand lub off hand slot
        if (event.getSlot() == player.getInventory().getHeldItemSlot() || event.getSlot() == 40) {
            UUID playerId = player.getUniqueId();

            this.scheduler.laterSync(() -> {
                passiveManager.invalidatePlayerCache(playerId);
                // DOŁÓŻ to:
                this.scheduler.async(() -> {
                    Profile profile = this.profileRepository.findOrCreate(player);
                    String artefactId = profile.getArtefactId();
                    passiveManager.updatePlayerCache(playerId, artefactId);
                    this.scheduler.sync(() -> passiveManager.checkPlayerImmediately(player));
                });
            }, Duration.ofMillis(50));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        ArtefactItemsUtil.getArtefactId(droppedItem).ifPresent(artefactId -> {
            // Nie pozwól dropować artefaktów oprócz dragon
            if (!artefactId.equalsIgnoreCase(DragonArtefact.ID)) {
                event.setCancelled(true);
                return;
            }


            // Natychmiastowe sprawdzenie po drop
            this.scheduler.laterSync(() -> {
                passiveManager.invalidatePlayerCache(player.getUniqueId());
                // DOŁÓŻ to:
                this.scheduler.async(() -> {
                    passiveManager.updatePlayerCache(player.getUniqueId(), artefactId);
                    this.scheduler.sync(() -> passiveManager.checkPlayerImmediately(player));
                });
            }, Duration.ofMillis(50));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.isCancelled()) return;

        UUID playerId = player.getUniqueId();
        ItemStack pickedItem = event.getItem().getItemStack();

        if (isArtefactItem(pickedItem)) {
            // Natychmiastowe sprawdzenie po pickup
            this.scheduler.laterSync(() -> {
                passiveManager.invalidatePlayerCache(playerId);
                // DOŁÓŻ to:
                this.scheduler.async(() -> {
                    Profile profile = this.profileRepository.findOrCreate(player);
                    String artefactId = profile.getArtefactId();
                    passiveManager.updatePlayerCache(playerId, artefactId);
                    this.scheduler.sync(() -> passiveManager.checkPlayerImmediately(player));
                });
            }, Duration.ofMillis(100));
        }
    }

    private boolean isArtefactItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.has(ArtefactsPlugin.ARTEFACT_ITEM_KEY);
    }
}