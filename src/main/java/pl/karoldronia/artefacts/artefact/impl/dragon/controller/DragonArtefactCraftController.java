package pl.karoldronia.artefacts.artefact.impl.dragon.controller;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.config.impl.PluginConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.Optional;

public class DragonArtefactCraftController implements Listener {

    private final ProfileRepository profileRepository;
    private final NoticeService noticeService;
    private final PluginConfig pluginConfig;

    public DragonArtefactCraftController(ProfileRepository profileRepository, NoticeService noticeService, PluginConfig pluginConfig) {
        this.profileRepository = profileRepository;
        this.noticeService = noticeService;
        this.pluginConfig = pluginConfig;
    }

    @EventHandler
    void onDragonArtefactCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!event.getRecipe().getResult().isSimilar(this.pluginConfig.dragonArtefactItem.build(DragonArtefact.ID))) {
            return;
        }

        Optional<String> artefactIdOptional = ArtefactItemsUtil.getArtefactFromInventory(player);

        if (artefactIdOptional.isEmpty()) {
            System.out.println("No artefact found in player's inventory.");
            return;
        }

        String artefactId = artefactIdOptional.get();
        ArtefactItemsUtil.removeOldArtefactItem(artefactId, player);

        Profile profile = this.profileRepository.findOrCreate(player);
        profile.setArtefactId(DragonArtefact.ID);
        profile.setHasDragon(true);
        profile.setPreviousArtefactId(artefactId);

        this.noticeService.create()
                .notice(messages -> messages.dragonArtefactCrafted)
                .player(player.getUniqueId())
                .send();

        this.profileRepository.save(profile);
    }
}