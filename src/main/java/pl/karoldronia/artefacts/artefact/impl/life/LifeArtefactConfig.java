package pl.karoldronia.artefacts.artefact.impl.life;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.time.Duration;

public class LifeArtefactConfig extends OkaeriConfig {

    public Duration abilityCooldown = Duration.ofSeconds(60);

    public Duration upgradedAbilityCooldown = Duration.ofSeconds(90);

    public double upgradedAbilityRadius = 3.0;

    @Comment("How many hearts should targets have after being affected by the upgraded life artefact ability.")
    public int upgradedAbilityHealthAmount = 7;

    public Duration upgradedAbilityHealthRestoreDuration = Duration.ofSeconds(20);

}