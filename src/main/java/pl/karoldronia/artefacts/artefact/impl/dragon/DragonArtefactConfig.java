package pl.karoldronia.artefacts.artefact.impl.dragon;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.time.Duration;

public class DragonArtefactConfig extends OkaeriConfig {

    @Comment("First ability")
    public Duration abilityCooldown = Duration.ofSeconds(45);

    public Duration dragonBreathDuration = Duration.ofSeconds(10);

    @Comment("Dragon breath damage in hearts")
    public int dragonBreathDamage = 2;

    @Comment("Dragon breath are will be created 5 blocks in looking direction of the player")
    public int dragonBreathLocationMultiplier = 5;

    public Duration secondAbilityCooldown = Duration.ofMinutes(4);

    @Comment("14 damage is 7 hearts")
    public double secondAbilitySmashDamage = 14.0;

    public Duration thirdAbilityCooldown = Duration.ofSeconds(90);

    @Comment("4 damage is 2 hearts")
    public double thirdAbilityDragonBreathDamage = 4.0;

    @Comment("In seconds")
    public int thirdAbilityDragonBreathDuration = 5;

    @Comment("8.0 is 4 hearts")
    public double dragonFireballDamage = 8.0;

}