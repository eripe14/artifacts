package pl.karoldronia.artefacts.artefact.impl.ice;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.karoldronia.artefacts.artefact.impl.fire.FireArtefact;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

public class IceArtefactController implements Listener {

    private static final float DEFAULT_WALK_SPEED = 0.2f; // Default walk speed in Minecraft
    private static final float ICE_WALK_SPEED = 0.6f; // Speed when walking on ice
    private final ProfileRepository profileRepository;

    public IceArtefactController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Profile profile = this.profileRepository.findOrCreate(player);

        if (!profile.getArtefactId().equalsIgnoreCase(IceArtefact.ID)) {
            return;
        }

        Location toLocation = event.getTo().clone().subtract(0, 1, 0);
        Block toBlock = toLocation.getBlock();
        Material toType = toBlock.getType();

        Location from = event.getFrom().clone().subtract(0, 1, 0);
        Block fromBlock = from.getBlock();
        Material fromType = fromBlock.getType();

        // player is on ice and moves to a non-ice block
        if (!this.isOnIce(toType) && this.isOnIce(fromType)) {
            player.setWalkSpeed(DEFAULT_WALK_SPEED);
            return;
        }

        if (this.isOnIce(toType)) {
            player.setWalkSpeed(ICE_WALK_SPEED);
        }
    }

    private boolean isOnIce(Material material) {
        return material == Material.ICE || material == Material.PACKED_ICE || material == Material.BLUE_ICE;
    }

}