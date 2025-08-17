package pl.karoldronia.artefacts.artefact;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;
import pl.karoldronia.artefacts.config.impl.MessageConfig;

import java.util.Optional;

public class ArtefactArgumentResolver extends ArgumentResolver<CommandSender, Artefact> {

    private final ArtefactService artefactService;
    private final MessageConfig messageConfig;

    public ArtefactArgumentResolver(ArtefactService artefactService, MessageConfig messageConfig) {
        this.artefactService = artefactService;
        this.messageConfig = messageConfig;
    }

    @Override
    protected ParseResult<Artefact> parse(Invocation<CommandSender> invocation, Argument<Artefact> context, String argument) {
        Optional<Artefact> optionalArtefact = this.artefactService.findArtefact(argument);
        return optionalArtefact.<ParseResult<Artefact>>map(ParseResult::success)
                .orElseGet(() -> ParseResult.failure(this.messageConfig.artefactNotFound));
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Artefact> argument, SuggestionContext context) {
        return this.artefactService.findAll().stream()
                .map(Artefact::getId)
                .collect(SuggestionResult.collector());
    }
}