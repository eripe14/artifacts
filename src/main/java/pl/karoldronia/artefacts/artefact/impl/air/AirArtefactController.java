package pl.karoldronia.artefacts.artefact.impl.air;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

public class AirArtefactController implements Listener {

    private final ProfileRepository profileRepository;

    public AirArtefactController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @EventHandler
    void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Profile profile = this.profileRepository.findOrCreate(player);
        if (!profile.getArtefactId().equalsIgnoreCase(AirArtefact.ID)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }
}