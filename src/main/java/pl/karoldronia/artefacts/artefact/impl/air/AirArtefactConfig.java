package pl.karoldronia.artefacts.artefact.impl.air;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

import java.time.Duration;

public class AirArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(45);

    public double dashDistance = 25.0;

    @Comment("4.0 is 2 hearts")
    public double dashDamage = 4.0;

    @Comment("Options below are for the upgraded ability")
    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 15.0;

    public double upgradedDashDistance = 8.0;

    public double singleDashDamage = 3.0;

    public double groundPoundDamage = 3.0;

    public double groundPoundRadius = 5.0;

}