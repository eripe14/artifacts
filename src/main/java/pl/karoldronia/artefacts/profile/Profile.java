package pl.karoldronia.artefacts.profile;

import eu.okaeri.persistence.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.karoldronia.artefacts.artefact.ability.Ability;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=false)
public class Profile extends Document {

    private String name;
    private String artefactId;
    private List<UUID> trustedPlayers;
    private boolean upgraded;
    private Map<String, Instant> abilitiesCooldowns;

    private boolean hasDragon;
    private boolean dragonUpgraded;
    private String previousArtefactId;

    public void trustPlayer(UUID playerId) {
        this.trustedPlayers.add(playerId);
    }

    public void untrustPlayer(UUID playerId) {
        this.trustedPlayers.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return this.trustedPlayers.contains(playerId);
    }

    public void setAbilityCooldown(String abilityId, Duration cooldown) {
        Instant cooldownEnd = Instant.now().plus(cooldown);
        this.abilitiesCooldowns.put(abilityId, cooldownEnd);
        this.save();
    }

    public Duration getAbilityCooldown(String abilityId) {
        Instant cooldown = this.abilitiesCooldowns.get(abilityId);
        if (cooldown == null) {
            return Duration.ZERO;
        }

        if (cooldown.isBefore(Instant.now())) {
            this.abilitiesCooldowns.remove(abilityId);
            this.save();
            return Duration.ZERO;
        }

        return Duration.between(Instant.now(), cooldown);
    }

    public boolean hasCooldown(Ability ability) {
        Duration cooldown = this.getAbilityCooldown(ability.getId());
        return !cooldown.isNegative() && !cooldown.isZero();
    }

    public UUID getUniqueId() {
        return this.getPath().toUUID();
    }

}