package pl.karoldronia.artefacts.command;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import dev.rollczi.litecommands.permission.MissingPermissionsHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.UUID;

public class MissingPermissionHandlerImpl implements MissingPermissionsHandler<CommandSender> {

    private final NoticeService noticeService;

    public MissingPermissionHandlerImpl(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Override
    public void handle(Invocation<CommandSender> invocation, MissingPermissions missingPermissions, ResultHandlerChain<CommandSender> chain) {
        CommandSender commandSender = invocation.sender();

        if (!(commandSender instanceof Player player)) {
            return;
        }

        UUID uniqueId = player.getUniqueId();
        this.noticeService.create()
                .notice(messages -> messages.noPermission)
                .player(uniqueId)
                .send();
    }

}