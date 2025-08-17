package pl.karoldronia.artefacts.artefact.impl.thunder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

public class ThunderArtefactController implements Listener {

    private final ProfileRepository profileRepository;

    public ThunderArtefactController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @EventHandler
    void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Profile profile = this.profileRepository.findOrCreate(player);
        if (!profile.getArtefactId().equalsIgnoreCase(ThunderArtefact.ID)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setCancelled(true);
        }
    }

}