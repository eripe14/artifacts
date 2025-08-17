package pl.karoldronia.artefacts.artefact.ability.charging;

import pl.karoldronia.artefacts.artefact.ability.Ability;

import java.time.Duration;

public interface ChargingAbility extends Ability {

    Duration getChargingDuration();

}