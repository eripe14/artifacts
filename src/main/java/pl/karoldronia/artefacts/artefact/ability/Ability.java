package pl.karoldronia.artefacts.artefact.ability;

import org.bukkit.entity.Player;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public interface Ability {

    String getId();

    AbilityResult performAbility(Player player, Profile profile);

    Duration getCooldown();

    default List<Player> getTargets(Player player, Profile profile, double radius) {
        return player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(nearbyPlayer -> !nearbyPlayer.equals(player))
                .filter(nearbyPlayer -> !profile.getTrustedPlayers().contains(nearbyPlayer.getUniqueId()))
                .toList();
    }

    default boolean requireUpgrade() {
        return false;
    }

    default void onDisable() {

    }

    default AbilityTrigger getTrigger() {
        return this.requireUpgrade() ? AbilityTrigger.RPM_SHIFT : AbilityTrigger.RPM;
    }

}