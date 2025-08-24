package pl.karoldronia.artefacts.artefact.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import pl.karoldronia.artefacts.ArtefactsPlugin;
import pl.karoldronia.artefacts.artefact.item.crafting.CraftingService;

import java.util.Optional;

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

    public static Optional<String> getArtefactId(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return Optional.empty();
        }

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        if (dataContainer.has(ArtefactsPlugin.ARTEFACT_ITEM_KEY, PersistentDataType.STRING)) {
            return Optional.of(dataContainer.get(ArtefactsPlugin.ARTEFACT_ITEM_KEY, PersistentDataType.STRING));
        }

        return Optional.empty();
    }

    public static boolean isHoldingArtefact(String artefactId, Player player) {
        if (artefactId == null || player == null) return false;
        if (hasArtefact(player.getInventory().getItemInMainHand(), artefactId)) return true;
        return hasArtefact(player.getInventory().getItemInOffHand(), artefactId);
    }

    private static boolean hasArtefact(ItemStack stack, String artefactId) {
        if (stack == null || stack.getType() == Material.AIR) return false;
        if (!stack.hasItemMeta()) return false;

        PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
        String id = pdc.get(ArtefactsPlugin.ARTEFACT_ITEM_KEY, PersistentDataType.STRING);
        return artefactId.equals(id);
    }

    public static Optional<String> getArtefactFromInventory(Player player) {
        for (ItemStack content : player.getInventory().getContents()) {
            if (content == null || content.getType() == Material.AIR) continue;

            PersistentDataContainer pdc = content.getItemMeta().getPersistentDataContainer();
            if (pdc.has(ArtefactsPlugin.ARTEFACT_ITEM_KEY, PersistentDataType.STRING)) {
                return Optional.ofNullable(pdc.get(ArtefactsPlugin.ARTEFACT_ITEM_KEY, PersistentDataType.STRING));
            }
        }

        return Optional.empty();
    }

    public static void removeOldArtefactItem(String artefactId, Player player) {
        @Nullable ItemStack[] contents = player.getInventory().getContents();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        contents[contents.length - 1] = itemInOffHand; // Ensure off-hand item is also checked

        for (@Nullable ItemStack content : contents) {
            if (content == null || content.getType() == Material.AIR) continue;

            PersistentDataContainer pdc = content.getItemMeta().getPersistentDataContainer();
            String id = pdc.get(ArtefactsPlugin.ARTEFACT_ITEM_KEY, PersistentDataType.STRING);
            if (artefactId.equalsIgnoreCase(id)) {
                player.getInventory().remove(content);
            }
        }
    }


}