package pl.karoldronia.artefacts.artefact.item;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.Optional;
import java.util.UUID;

public class ArtefactItemController implements Listener {

    private final ProfileRepository profileRepository;
    private final ArtefactService artefactService;
    private final NoticeService noticeService;

    public ArtefactItemController(ProfileRepository profileRepository, ArtefactService artefactService, NoticeService noticeService) {
        this.profileRepository = profileRepository;
        this.artefactService = artefactService;
        this.noticeService = noticeService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Profile profile = this.profileRepository.findOrCreate(player);
        String artefactId = profile.getArtefactId();
        Optional<Artefact> artefactOptional = this.artefactService.findArtefact(artefactId);

        if (ArtefactItemsUtil.isTraderItem(item)) {
            if (artefactId.equalsIgnoreCase(DragonArtefact.ID)) {
                this.noticeService.create()
                        .player(uniqueId)
                        .notice(messages -> messages.traderItemCannotBeUsedWithDragonArtefact)
                        .send();
                return;
            }

            Artefact randomArtefact = this.artefactService.getRandomArtefact(artefactOptional.orElse(null));
            profile.setArtefactId(randomArtefact.getId());

            randomArtefact.givePassiveEffects(player);
            item.setAmount(item.getAmount() - 1);

            this.noticeService.create()
                    .player(uniqueId)
                    .notice(messages -> messages.traderItemUsed)
                    .placeholder("{artefact}", randomArtefact.getId())
                    .send();

            this.profileRepository.save(profile);
            return;
        }

        if (ArtefactItemsUtil.isUpgraderItem(item)) {
            if (artefactOptional.isEmpty()) {
                return;
            }

            if (!artefactId.equalsIgnoreCase(DragonArtefact.ID) && profile.isUpgraded()) {
                this.noticeService.create()
                        .player(uniqueId)
                        .notice(messages -> messages.artefactAlreadyUpgraded)
                        .send();
                return;
            }

            if (artefactId.equalsIgnoreCase(DragonArtefact.ID) && profile.isUpgraded()) {
                if (profile.isDragonUpgraded()) {
                    this.noticeService.create()
                            .player(uniqueId)
                            .notice(messages -> messages.artefactAlreadyUpgraded)
                            .send();
                    return;
                }

                item.setAmount(item.getAmount() - 1);
                profile.setDragonUpgraded(true);
                profile.save();
                this.noticeService.create()
                        .player(uniqueId)
                        .notice(messages -> messages.dragonArtefactUpgraded)
                        .send();
                return;
            }

            item.setAmount(item.getAmount() - 1);

            this.noticeService.create()
                    .player(uniqueId)
                    .notice(messages -> messages.upgraderItemUsed)
                    .send();

            profile.setUpgraded(true);
            profile.save();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory clickedInv = event.getClickedInventory(); // źródło działania (slot kliknięty)
        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        Inventory bottom = view.getBottomInventory(); // zawsze EQ gracza
        InventoryAction action = event.getAction();

        // helpery
        boolean cursorIsArtefact = cursorItem != null && ArtefactItemsUtil.getArtefactId(cursorItem).isPresent();
        boolean clickedIsArtefact = clickedItem != null && ArtefactItemsUtil.getArtefactId(clickedItem).isPresent();

        // 1) Kładzenie kursorem artefaktu do slotu w innym inventory (PLACE_*, SWAP_WITH_CURSOR)
        if (cursorIsArtefact && clickedInv != null) {
            if (!isOwnInventory(clickedInv, player)) {
                event.setCancelled(true);
                return;
            }
        }

        // 2) Shift-click przenoszący między inventory (MOVE_TO_OTHER_INVENTORY)
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedIsArtefact && clickedInv != null) {
            Inventory destination = (clickedInv.equals(bottom) ? top : bottom);
            if (!isOwnInventory(destination, player)) {
                event.setCancelled(true);
                return;
            }
        }

        // 3) Hotbar swap / number key (HOTBAR_SWAP, HOTBAR_MOVE_AND_READD)
        // Jeżeli klikamy slot w obcym inventory i próbujemy wstawić/wyjąć artefakt hotbarem – blokuj.
        if ((action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) && clickedInv != null) {
            // przedmiot w hotbarze:
            if (event.getWhoClicked() instanceof Player p) {
                int hotbarButton = event.getHotbarButton(); // 0..8 lub -1
                if (hotbarButton >= 0) {
                    ItemStack hotbarItem = p.getInventory().getItem(hotbarButton);
                    boolean hotbarIsArtefact = hotbarItem != null && ArtefactItemsUtil.getArtefactId(hotbarItem).isPresent();

                    // jeśli operacja dotyczy artefaktu (w klikniętym slocie lub w hotbarze) i target nie jest własnym EQ -> cancel
                    if ((clickedIsArtefact || hotbarIsArtefact) && !isOwnInventory(clickedInv, player)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // 4) SWAP_WITH_CURSOR gdy kliknięty slot jest w obcym inv, a którykolwiek z itemów to artefakt
        if (action == InventoryAction.SWAP_WITH_CURSOR && clickedInv != null) {
            if ((cursorIsArtefact || clickedIsArtefact) && !isOwnInventory(clickedInv, player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack oldCursor = event.getOldCursor();
        boolean draggingArtefact = oldCursor != null && ArtefactItemsUtil.getArtefactId(oldCursor).isPresent();
        if (!draggingArtefact) return;

        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        Inventory bottom = view.getBottomInventory(); // własne EQ

        // jeśli ANY z target slotów należy do inventory innego niż własne – cancel
        int topSize = top.getSize();
        for (int rawSlot : event.getRawSlots()) {
            Inventory target = (rawSlot < topSize) ? top : bottom;
            if (!isOwnInventory(target, (Player) event.getWhoClicked())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Zwraca true, jeśli dany inventory jest "własnym" gracza:
     * - jego główne EQ (bottom inventory w widoku),
     * - jego własny Ender Chest.
     */
    private boolean isOwnInventory(Inventory inventory, HumanEntity player) {
        if (inventory == null) return false;

        // EQ gracza w aktualnym widoku:
        if (player.getOpenInventory() != null && inventory.equals(player.getOpenInventory().getBottomInventory())) {
            return true;
        }
        // Własny ender chest:
        return inventory.equals(player.getEnderChest());
    }
}