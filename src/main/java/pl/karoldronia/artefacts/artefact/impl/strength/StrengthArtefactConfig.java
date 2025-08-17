package pl.karoldronia.artefacts.artefact.impl.strength;

import eu.okaeri.configs.OkaeriConfig;

import java.time.Duration;

public class StrengthArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public Duration upgradedAbilityWindow = Duration.ofSeconds(15);

    public int maxHits = 10;

    public Duration perHitBuffDuration = Duration.ofSeconds(3);

}