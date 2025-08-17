package pl.karoldronia.artefacts.artefact.impl.thunder;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;

import java.time.Duration;

@Header("Thunder artefact configuration")
public class ThunderArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double abilityRadius = 5.0;

    public Duration abilityDelayBetween = Duration.ofSeconds(1);

    public int abilityTimes = 5;

}