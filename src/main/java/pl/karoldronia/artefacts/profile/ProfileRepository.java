package pl.karoldronia.artefacts.profile;

import dev.rollczi.litecommands.util.StringUtil;
import eu.okaeri.persistence.repository.DocumentRepository;
import eu.okaeri.persistence.repository.annotation.DocumentCollection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@DocumentCollection(path = "profiles", keyLength = 36)
public interface ProfileRepository extends DocumentRepository<UUID, Profile> {

    default Profile findOrCreate(Player player) {
        Profile profile = this.findOrCreateByPath(player.getUniqueId());
        profile.setName(player.getName());

        if (profile.getArtefactId() == null) {
            profile.setArtefactId(StringUtil.EMPTY);
        }

        if (profile.getTrustedPlayers() == null) {
            profile.setTrustedPlayers(new ArrayList<>());
        }

        if (profile.getAbilitiesCooldowns() == null) {
            profile.setAbilitiesCooldowns(new HashMap<>());
        }

        return profile;
    }

}