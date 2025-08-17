package pl.karoldronia.artefacts.command;

import com.eternalcode.multification.shared.Formatter;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.karoldronia.artefacts.notice.NoticeService;

import java.util.UUID;

public class InvalidUsageHandlerImpl implements InvalidUsageHandler<CommandSender> {

    private final NoticeService noticeService;

    public InvalidUsageHandlerImpl(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        CommandSender commandSender = invocation.sender();
        Schematic schematic = result.getSchematic();

        if (!(commandSender instanceof Player player)) {
            return;
        }

        UUID uniqueId = player.getUniqueId();

        Formatter formatter = new Formatter();
        formatter.register("{COMMAND}", schematic.first());

        if (schematic.isOnlyFirst()) {
            this.noticeService.create()
                    .notice(messages -> messages.invalidUsage)
                    .formatter(formatter)
                    .player(uniqueId)
                    .send();
            return;
        }

        this.noticeService.create()
                .notice(messages -> messages.invalidUsageHeader)
                .player(uniqueId)
                .send();

        for (String scheme : schematic.all()) {
            formatter.register("{SCHEME}", scheme);
            this.noticeService.create()
                    .notice(messages -> messages.invalidUsageEntry)
                    .formatter(formatter)
                    .player(uniqueId)
                    .send();
        }
    }

}