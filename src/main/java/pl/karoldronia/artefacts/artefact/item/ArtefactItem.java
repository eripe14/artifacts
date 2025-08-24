package pl.karoldronia.artefacts.artefact.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.karoldronia.artefacts.ArtefactsPlugin;
import pl.karoldronia.artefacts.adventure.MiniMessageHolder;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class ArtefactItem implements MiniMessageHolder {

    private final int customModelData;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final List<ItemFlag> itemFlags;

    public ItemStack build() {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setCustomModelData(this.customModelData);
        itemMeta.displayName(MINI_MESSAGE.deserialize(this.name));
        itemMeta.lore(this.lore.stream().map(MINI_MESSAGE::deserialize).toList());
        itemMeta.addItemFlags(this.itemFlags.toArray(ItemFlag[]::new));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack build(String artefactId) {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setCustomModelData(this.customModelData);
        itemMeta.displayName(MINI_MESSAGE.deserialize(this.name));
        itemMeta.lore(this.lore.stream().map(MINI_MESSAGE::deserialize).toList());
        itemMeta.addItemFlags(this.itemFlags.toArray(ItemFlag[]::new));

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(ArtefactsPlugin.ARTEFACT_ITEM_KEY, PersistentDataType.STRING, artefactId);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}