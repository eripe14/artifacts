package pl.karoldronia.artefacts.profile;

import eu.okaeri.persistence.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    public void setAbilityCooldown(String abilityId, Duration cooldown) {
        Instant cooldownEnd = Instant.now().plus(cooldown);
        this.abilitiesCooldowns.put(abilityId, cooldownEnd);
    }

    public Duration getAbilityCooldown(String abilityId) {
        Instant cooldown = this.abilitiesCooldowns.get(abilityId);
        if (cooldown == null) {
            return Duration.ZERO;
        }
        return Duration.between(Instant.now(), cooldown);
    }

    public UUID getUniqueId() {
        return this.getPath().toUUID();
    }

}