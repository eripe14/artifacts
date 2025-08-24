package pl.karoldronia.artefacts.artefact;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.artefact.item.crafting.CraftingService;
import pl.karoldronia.artefacts.config.impl.MessageConfig;
import pl.karoldronia.artefacts.config.impl.PluginConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.Optional;

@Command(name = "artefact", aliases = "artefacts")
@RequiredArgsConstructor
public class ArtefactCommand {

    private final ProfileRepository profileRepository;
    private final NoticeService noticeService;
    private final MessageConfig messageConfig;
    private final PluginConfig pluginConfig;
    private final CraftingService craftingService;

    @Execute(name = "reload")
    void reload(@Context CommandSender sender) {
        this.messageConfig.load();
        this.pluginConfig.load();

        this.craftingService.unregister();
        this.craftingService.register();

        this.noticeService.create()
                .notice(messages -> messages.reload)
                .viewer(sender)
                .send();
    }

    @Execute(name = "set")
    void setArtefact(@Context Player sender, @Arg Artefact artefact, @Arg Optional<Player> targetOptional) {
        Player target = targetOptional.orElse(sender);

        Profile profile = this.profileRepository.findOrCreate(target);
        profile.setArtefactId(artefact.getId());
        profile.save();

        ArtefactItemsUtil.removeOldArtefactItem(profile.getArtefactId(), target);
        ItemStack build = artefact.getArtefactItem().build(artefact.getId());
        target.getInventory().addItem(build);

        this.noticeService.create()
                .notice(messages -> messages.artefactSet)
                .player(sender.getUniqueId())
                .placeholder("{id}", artefact.getId())
                .placeholder("{player}", target.getName())
                .send();
    }

}