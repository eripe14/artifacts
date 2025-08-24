package pl.karoldronia.artefacts.artefact.impl.ice;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

public class IceArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&bIce Artefact")
            .lore(List.of("&7lorem ipsum"))
            .material(Material.PAPER)
            .customModelData(0)
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double spikeDamage = 5.0;

    public int spikeCount = 5;

    public double abilityRadius = 5.0;

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 5.0;

    @Comment("Duration in seconds for which players are frozen")
    public int freezeDuration = 5;

}