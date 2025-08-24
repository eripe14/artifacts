package pl.karoldronia.artefacts.artefact.impl.earth;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

@Header("Earth artefact configuration")
public class EarthArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&2Earth Artefact")
            .lore(List.of("&7lorem ipsum"))
            .material(Material.PAPER)
            .customModelData(0)
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double abilityRadius = 5.0;

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 5.0;

    public double damage = 5;

}