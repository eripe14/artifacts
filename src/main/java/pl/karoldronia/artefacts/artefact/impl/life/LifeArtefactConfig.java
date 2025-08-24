package pl.karoldronia.artefacts.artefact.impl.life;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

public class LifeArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&4Life Artefact")
            .lore(List.of("&7lorem ipsum"))
            .material(Material.PAPER)
            .customModelData(0)
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(60);

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 3.0;

    @Comment("How many hearts should targets have after being affected by the upgraded life artefact ability.")
    public int upgradedAbilityHealthAmount = 7;

    public Duration upgradedAbilityHealthRestoreDuration = Duration.ofSeconds(20);

}