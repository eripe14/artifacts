package pl.karoldronia.artefacts.artefact.item;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.Optional;
import java.util.UUID;

public class ArtefactItemController implements Listener {

    private final ProfileRepository profileRepository;
    private final ArtefactService artefactService;
    private final NoticeService noticeService;

    public ArtefactItemController(ProfileRepository profileRepository, ArtefactService artefactService, NoticeService noticeService) {
        this.profileRepository = profileRepository;
        this.artefactService = artefactService;
        this.noticeService = noticeService;
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Profile profile = this.profileRepository.findOrCreate(player);
        String artefactId = profile.getArtefactId();
        Optional<Artefact> artefactOptional = this.artefactService.findArtefact(artefactId);

        if (ArtefactItemsUtil.isTraderItem(item)) {
            Artefact randomArtefact = this.artefactService.getRandomArtefact(artefactOptional.orElse(null));
            profile.setArtefactId(randomArtefact.getId());
            this.profileRepository.save(profile);

            this.noticeService.create()
                    .player(uniqueId)
                    .notice(messages -> messages.traderItemUsed)
                    .placeholder("{artefact}", randomArtefact.getId())
                    .send();
            return;
        }

        if (ArtefactItemsUtil.isUpgraderItem(item)) {
            if (artefactOptional.isEmpty()) {
                return;
            }
            
        }
    }
}