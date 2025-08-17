package pl.karoldronia.artefacts.artefact.impl.luck;

import eu.okaeri.configs.OkaeriConfig;

import java.time.Duration;

public class LuckArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 5.0;

    public Duration upgradedAbilityEffectDuration = Duration.ofSeconds(30);

}