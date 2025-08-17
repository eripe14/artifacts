package pl.karoldronia.artefacts.artefact.impl.ice;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.time.Duration;

public class IceArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double spikeDamage = 5.0;

    public int spikeCount = 5;

    public double abilityRadius = 5.0;

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 5.0;

    @Comment("Duration in seconds for which players are frozen")
    public int freezeDuration = 5;

}