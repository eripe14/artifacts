package pl.karoldronia.artefacts.artefact.impl.fire;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

public class FireArtefactController implements Listener {

    private static final float DEFAULT_WALK_SPEED = 0.2f; // Default walk speed in Minecraft
    private static final float LAVA_WALK_SPEED = 0.6f; // Speed when walking on lava
    private final ProfileRepository profileRepository;

    public FireArtefactController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Profile profile = this.profileRepository.findOrCreate(player);

        if (!profile.getArtefactId().equalsIgnoreCase(FireArtefact.ID)) {
            return;
        }

        Location toLocation = event.getTo().clone();
        Block toBlock = toLocation.getBlock();
        Material toType = toBlock.getType();

        Location from = event.getFrom().clone();
        Block fromBlock = from.getBlock();
        Material fromType = fromBlock.getType();

        // player exits lava
        if (!this.isOnLava(toType) && this.isOnLava(fromType)) {
            player.setWalkSpeed(DEFAULT_WALK_SPEED);
            return;
        }

        if (this.isOnLava(toType)) {
            player.setWalkSpeed(LAVA_WALK_SPEED);
        }
    }

    private boolean isOnLava(Material material) {
        return material == Material.LAVA;
    }

}