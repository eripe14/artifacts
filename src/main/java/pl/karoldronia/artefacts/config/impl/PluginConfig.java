package pl.karoldronia.artefacts.config.impl;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.impl.air.AirArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.earth.EarthArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.fire.FireArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.ice.IceArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.life.LifeArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.luck.LuckArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.ocean.OceanArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.sculk.SculkArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.strength.StrengthArtefactConfig;
import pl.karoldronia.artefacts.artefact.impl.thunder.ThunderArtefactConfig;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;
import pl.karoldronia.artefacts.artefact.item.crafting.CraftingIngredient;

import java.util.List;
import java.util.Map;

public class PluginConfig extends OkaeriConfig {

    public StorageConfig storage = new StorageConfig();

    public static class StorageConfig extends OkaeriConfig {
        public String prefix = "artefacts";

        //jdbc:mysql://{host}:{port}/{database}?user={username}&password={password}
        public String url = "jdbc:mysql://localhost:3306/mydb?user=karol&password=123";

    }

    public Map<Integer, CraftingIngredient> dragonArtefactScheme = Map.of(
            1, new CraftingIngredient(Material.DRAGON_EGG),
            2, new CraftingIngredient(Material.DRAGON_BREATH),
            3, new CraftingIngredient(Material.DRAGON_EGG),

            4, new CraftingIngredient(Material.NETHER_STAR),
            5, new CraftingIngredient(Material.DRAGON_BREATH),
            6, new CraftingIngredient(Material.NETHER_STAR),

            7, new CraftingIngredient(Material.DRAGON_EGG),
            8, new CraftingIngredient(Material.DRAGON_BREATH),
            9, new CraftingIngredient(Material.DRAGON_EGG)
    );

    public ArtefactItem dragonArtefactItem = new ArtefactItem(
            0,
            Material.DRAGON_EGG,
            "&6&lDragon artefact",
            List.of(
                    "&7Click to assign dragon artefact"
            ),
            List.of(ItemFlag.HIDE_ATTRIBUTES)
    );

    public Map<Integer, CraftingIngredient> traderCraftingScheme = Map.of(
            1, new CraftingIngredient(Material.ENDER_PEARL),
            2, new CraftingIngredient(Material.ENDER_EYE),
            3, new CraftingIngredient(Material.ENDER_PEARL),

            4, new CraftingIngredient(Material.DIAMOND_BLOCK),
            5, new CraftingIngredient(Material.REDSTONE_BLOCK),
            6, new CraftingIngredient(Material.GOLD_BLOCK),

            7, new CraftingIngredient(Material.ENDER_PEARL),
            8, new CraftingIngredient(Material.EMERALD_BLOCK),
            9, new CraftingIngredient(Material.ENDER_PEARL)
    );

    public ArtefactItem traderItem = new ArtefactItem(
            0,
            Material.EMERALD,
            "&a&lTrader",
            List.of(
                    "&7Click to reroll artefact"
            ),
            List.of(ItemFlag.HIDE_ATTRIBUTES)
    );

    public Map<Integer, CraftingIngredient> upgraderCraftingScheme = Map.of(
            1, new CraftingIngredient(Material.NETHERITE_INGOT),
            2, new CraftingIngredient(Material.DRAGON_BREATH),
            3, new CraftingIngredient(Material.NETHERITE_INGOT),

            4, new CraftingIngredient(Material.DRAGON_BREATH),
            5, new CraftingIngredient(Material.NETHER_STAR),
            6, new CraftingIngredient(Material.DRAGON_BREATH),

            7, new CraftingIngredient(Material.DIAMOND_BLOCK),
            8, new CraftingIngredient(Material.DRAGON_BREATH),
            9, new CraftingIngredient(Material.DIAMOND_BLOCK)
    );

    public ArtefactItem upgraderItem = new ArtefactItem(
            0,
            Material.EMERALD,
            "&a&lUpgrader",
            List.of(
                    "&7Click to upgrade artefact"
            ),
            List.of(ItemFlag.HIDE_ATTRIBUTES)
    );

    public OceanArtefactConfig oceanArtefactConfig = new OceanArtefactConfig();

    public FireArtefactConfig fireArtefactConfig = new FireArtefactConfig();

    public ThunderArtefactConfig thunderArtefactConfig = new ThunderArtefactConfig();

    public EarthArtefactConfig earthArtefactConfig = new EarthArtefactConfig();

    public AirArtefactConfig airArtefactConfig = new AirArtefactConfig();

    public IceArtefactConfig iceArtefactConfig = new IceArtefactConfig();

    public LifeArtefactConfig lifeArtefactConfig = new LifeArtefactConfig();

    public SculkArtefactConfig sculkArtefactConfig = new SculkArtefactConfig();

    public StrengthArtefactConfig strengthArtefactConfig = new StrengthArtefactConfig();

    public LuckArtefactConfig luckArtefactConfig = new LuckArtefactConfig();

    public DragonArtefactConfig dragonArtefactConfig = new DragonArtefactConfig();

}