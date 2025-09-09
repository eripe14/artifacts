package pl.karoldronia.artefacts.artefact.impl.fire;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;

import java.time.Duration;
import java.util.List;

@Header("Fire artefact configuration")
public class FireArtefactConfig extends OkaeriConfig {

    public ArtefactItem item = ArtefactItem.builder()
            .name("&cFire Artefact")
            .lore(List.of("&7lorem ipsum"))
            .material(Material.PAPER)
            .customModelData(0)
            .itemFlags(List.of(ItemFlag.HIDE_ATTRIBUTES))
            .build();

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double abilityRadius = 3.0;

    @Comment("How long the fire will last on the targets (in ticks, 20 ticks = 1 second)")
    public int abilityFireTicks = 200;

    public  double abilityFireDamage = 4.0;

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public Duration upgradedAbilityChargingDuration = Duration.ofSeconds(10);

    @Comment("12 is 6 hearts")
    public double fireballDamage = 12.0;

}