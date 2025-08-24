package pl.karoldronia.artefacts.artefact.impl.air;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

public class AirArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&bArtefact of Air")
            .customModelData(0)
            .material(Material.PAPER)
            .lore(List.of(
                    "&7This artefact allows you to",
                    "&7dash in the air, dealing damage",
                    "&7to enemies in your path."
            ))
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double dashDistance = 25.0;

    @Comment("4.0 is 2 hearts")
    public double dashDamage = 4.0;

    @Comment("Options below are for the upgraded ability")
    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 15.0;

    public double upgradedDashDistance = 8.0;

    public double singleDashDamage = 3.0;

    public double groundPoundDamage = 3.0;

    public double groundPoundRadius = 5.0;

}