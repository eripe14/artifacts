package pl.karoldronia.artefacts.artefact.impl.sculk;

import eu.okaeri.configs.OkaeriConfig;

import java.time.Duration;

public class SculkArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double abilityRadius = 5.0;

    public double abilityDamage = 10.0;

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

}