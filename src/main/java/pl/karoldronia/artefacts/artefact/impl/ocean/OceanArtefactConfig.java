package pl.karoldronia.artefacts.artefact.impl.ocean;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

@Header("Ocean artefact configuration")
public class OceanArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&9Ocean Artefact")
            .lore(List.of("&7lorem ipsum"))
            .material(Material.PAPER)
            .customModelData(0)
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double abilityRadius = 6.0;

    public double damage = 6.0;

    public double pushForce = 1.25;

    @Comment("All settings below are for the upgraded ability")
    public Duration upgradedAbilityCooldown = Duration.ofSeconds(60);

    public double landDamage = 8.0;

    public double waterDamage = 14.0;

    public double upgradedEffectRadius = 4.0;

}