package pl.karoldronia.artefacts.artefact;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.Optional;

@Command(name = "artefact", aliases = "artefacts")
public class ArtefactCommand {

    private final ProfileRepository profileRepository;
    private final NoticeService noticeService;

    public ArtefactCommand(ProfileRepository profileRepository, NoticeService noticeService) {
        this.profileRepository = profileRepository;
        this.noticeService = noticeService;
    }

    @Execute(name = "set")
    void setArtefact(@Context Player sender, @Arg Artefact artefact, @Arg Optional<Player> targetOptional) {
        Player target = targetOptional.orElse(sender);

        Profile profile = this.profileRepository.findOrCreate(target);
        profile.setArtefactId(artefact.getId());
        profile.save();

        this.noticeService.create()
                .notice(messages -> messages.artefactSet)
                .player(sender.getUniqueId())
                .placeholder("{id}", artefact.getId())
                .placeholder("{player}", target.getName())
                .send();
    }

}