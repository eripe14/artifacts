package pl.karoldronia.artefacts.artefact.impl.dragon.controller;

import dev.rollczi.litecommands.util.StringUtil;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

@RequiredArgsConstructor
public class DragonArtefactItemController implements Listener {

    private final ProfileRepository profileRepository;
    private final ArtefactService artefactService;
    private final NoticeService noticeService;

    @EventHandler
    void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemDrop().getItemStack();

        Profile profile = this.profileRepository.findOrCreate(player);
        if (!profile.getArtefactId().equalsIgnoreCase(DragonArtefact.ID)) {
            return;
        }

        ArtefactItemsUtil.getArtefactId(itemStack).ifPresent(artefactId -> {
            if (!artefactId.equals(DragonArtefact.ID)) {
                return;
            }

            profile.setArtefactId(profile.getPreviousArtefactId());
            profile.setPreviousArtefactId(StringUtil.EMPTY);
            profile.save();
        });

        this.artefactService.findArtefact(profile.getArtefactId()).ifPresent(artefact -> {
            if (artefact.getId().equals(DragonArtefact.ID)) {
                return;
            }

            ItemStack previousArtefactItem = artefact.getArtefactItem().build(artefact.getId());
            player.getInventory().addItem(previousArtefactItem);

            this.noticeService.create()
                    .player(player.getUniqueId())
                    .notice(messages -> messages.droppedDragonArtefact)
                    .placeholder("{artefact}", artefact.getId())
                    .send();
        });
    }

    @EventHandler
    void onItemPickUp(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        Profile profile = this.profileRepository.findOrCreate(player);
        ItemStack itemStack = event.getItem().getItemStack();

        ArtefactItemsUtil.getArtefactId(itemStack).ifPresent(artefactId -> {
            if (!artefactId.equals(DragonArtefact.ID)) {
                return;
            }

            ArtefactItemsUtil.removeOldArtefactItem(profile.getArtefactId(), player);
            profile.setPreviousArtefactId(profile.getArtefactId());
            profile.setArtefactId(DragonArtefact.ID);
            profile.save();

            this.noticeService.create()
                    .player(player.getUniqueId())
                    .notice(messages -> messages.pickedUpDragonArtefact)
                    .placeholder("{artefact}", profile.getArtefactId())
                    .send();
        });
    }
}