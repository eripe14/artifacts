package pl.karoldronia.artefacts.artefact.item.crafting;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

public class CraftingIngredient extends OkaeriConfig {

    private Material material;

    public CraftingIngredient() {
        // Default constructor for OkaeriConfig
    }

    public CraftingIngredient(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}