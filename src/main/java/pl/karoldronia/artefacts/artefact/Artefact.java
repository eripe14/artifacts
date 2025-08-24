package pl.karoldronia.artefacts.artefact;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.item.ArtefactItem;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

public interface Artefact {

    String getId();

    ArtefactItem getArtefactItem();

    List<PotionEffectType> givePassiveEffects(Player player);

    List<Ability> getAbilities();

    default void registerControllers(Plugin plugin, Server server, ProfileRepository profileRepository) {

    }

    default void shutdown() {
        for (Ability ability : this.getAbilities()) {
            ability.onDisable();
        }
    }

}