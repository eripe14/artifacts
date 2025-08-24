package pl.karoldronia.artefacts.artefact.item.crafting;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.config.impl.PluginConfig;

public class CraftingService {

    public static final NamespacedKey TRADER_ITEM_KEY = new NamespacedKey("artefacts", "trader_item");
    public static final NamespacedKey UPGRADER_ITEM_KEY = new NamespacedKey("artefacts", "upgrader_item");

    private final Plugin plugin;
    private final PluginConfig pluginConfig;
    private final NamespacedKey traderKey;
    private final NamespacedKey upgraderKey;
    private final NamespacedKey dragonArtefactKey;

    public CraftingService(Plugin plugin, PluginConfig pluginConfig) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.traderKey = new NamespacedKey(this.plugin, "trader_crafting");
        this.upgraderKey = new NamespacedKey(this.plugin, "upgrader_crafting");
        this.dragonArtefactKey = new NamespacedKey(this.plugin, "dragon_artefact_crafting");
    }

    public void unregister() {
        this.plugin.getServer().removeRecipe(traderKey);
        this.plugin.getServer().removeRecipe(upgraderKey);
        this.plugin.getServer().removeRecipe(dragonArtefactKey);
    }

    public void register() {
        ShapedRecipe dragonArtefactRecipe = new ShapedRecipe(dragonArtefactKey, this.pluginConfig.dragonArtefactItem.build(DragonArtefact.ID));
        dragonArtefactRecipe.shape(
                "123",
                "456",
                "789"
        );

        // Mapowanie pozycji na znaki używane w shape
        String[] shapeLines = {"123", "456", "789"};
        int position = 1;
        for (String line : shapeLines) {
            for (char c : line.toCharArray()) {
                CraftingIngredient craftingIngredient = this.pluginConfig.dragonArtefactScheme.get(position);
                if (craftingIngredient != null) {
                    dragonArtefactRecipe.setIngredient(c, craftingIngredient.getMaterial());
                }
                position++;
            }
        }

        ShapedRecipe traderRecipe = new ShapedRecipe(traderKey, this.getTraderItem());
        traderRecipe.shape(
                "123",
                "456",
                "789"
        );

        position = 1;
        for (String line : shapeLines) {
            for (char c : line.toCharArray()) {
                CraftingIngredient craftingIngredient = this.pluginConfig.traderCraftingScheme.get(position);
                if (craftingIngredient != null) {
                    traderRecipe.setIngredient(c, craftingIngredient.getMaterial());
                }
                position++;
            }
        }

        ShapedRecipe upgraderRecipe = new ShapedRecipe(upgraderKey, this.getUpgraderItem());
        upgraderRecipe.shape(
                "123",
                "456",
                "789"
        );

        // Mapowanie pozycji na znaki używane w shape
        position = 1;
        for (String line : shapeLines) {
            for (char c : line.toCharArray()) {
                CraftingIngredient craftingIngredient = this.pluginConfig.upgraderCraftingScheme.get(position);
                if (craftingIngredient != null) {
                    upgraderRecipe.setIngredient(c, craftingIngredient.getMaterial());
                }
                position++;
            }
        }

        this.plugin.getServer().addRecipe(traderRecipe);
        this.plugin.getServer().addRecipe(upgraderRecipe);
        this.plugin.getServer().addRecipe(dragonArtefactRecipe);
    }

    private ItemStack getTraderItem() {
        ItemStack trader = this.pluginConfig.traderItem.build();
        ItemMeta itemMeta = trader.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(TRADER_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
        trader.setItemMeta(itemMeta);

        return trader;
    }

    private ItemStack getUpgraderItem() {
        ItemStack upgrader = this.pluginConfig.upgraderItem.build();
        ItemMeta itemMeta = upgrader.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(UPGRADER_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
        upgrader.setItemMeta(itemMeta);

        return upgrader;
    }
}