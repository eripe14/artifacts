package pl.karoldronia.artefacts.artefact.impl.luck;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

public class LuckArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&2Luck Artefact")
            .lore(List.of("&7lorem ipsum"))
            .material(Material.PAPER)
            .customModelData(0)
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 5.0;

    public Duration upgradedAbilityEffectDuration = Duration.ofSeconds(30);

}