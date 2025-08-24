package pl.karoldronia.artefacts.artefact.impl.strength;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

public class StrengthArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&cStrength Artefact")
            .lore(List.of("&7lorem ipsum"))
            .material(Material.PAPER)
            .customModelData(0)
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public Duration upgradedAbilityWindow = Duration.ofSeconds(15);

    public int maxHits = 10;

    public Duration perHitBuffDuration = Duration.ofSeconds(3);

}