package pl.karoldronia.artefacts.artefact;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.List;

public interface Artefact {

    String getId();

    void givePassiveEffects(Player player);

    List<Ability> getAbilities();

    default void registerControllers(Plugin plugin, Server server, ProfileRepository profileRepository) {

    }

    default void shutdown() {
        for (Ability ability : this.getAbilities()) {
            ability.onDisable();
        }
    }

}