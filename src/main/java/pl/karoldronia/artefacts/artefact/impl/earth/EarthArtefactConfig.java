package pl.karoldronia.artefacts.artefact.impl.earth;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

import java.time.Duration;

@Header("Earth artefact configuration")
public class EarthArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double abilityRadius = 5.0;

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 5.0;

    public double damage = 5;

}