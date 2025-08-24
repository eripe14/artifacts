package pl.karoldronia.artefacts.profile;

import com.eternalcode.multification.shared.Formatter;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.UUID;

@Command(name = "trust")
@Permission("artefacts.command.trust")
public class TrustCommand {

    private final ProfileRepository profileRepository;
    private final NoticeService noticeService;

    public TrustCommand(ProfileRepository profileRepository, NoticeService noticeService) {
        this.profileRepository = profileRepository;
        this.noticeService = noticeService;
    }

    @Execute
    void trust(@Context Player player, @Arg Player target) {
        Profile profile = this.profileRepository.findOrCreate(player);
        UUID targetUniqueId = target.getUniqueId();

        Formatter formatter = new Formatter().register("{target}", target.getName());

        if (profile.isTrusted(targetUniqueId)) {
            profile.untrustPlayer(targetUniqueId);
            this.noticeService.create()
                    .notice(messages -> messages.untrustPlayer)
                    .player(player.getUniqueId())
                    .formatter(formatter)
                    .send();
            return;
        }

        profile.trustPlayer(targetUniqueId);
        this.noticeService.create()
                .notice(messages -> messages.trustPlayer)
                .player(player.getUniqueId())
                .formatter(formatter)
                .send();
    }
}