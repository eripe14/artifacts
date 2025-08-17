package pl.karoldronia.artefacts.artefact.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import pl.karoldronia.artefacts.artefact.item.crafting.CraftingService;

public final class ArtefactItemsUtil {

    private ArtefactItemsUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isTraderItem(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        return itemMeta.getPersistentDataContainer().has(CraftingService.TRADER_ITEM_KEY, PersistentDataType.BYTE);
    }

    public static boolean isUpgraderItem(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        return itemMeta.getPersistentDataContainer().has(CraftingService.UPGRADER_ITEM_KEY, PersistentDataType.BYTE);
    }

}