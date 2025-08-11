package kz.haru.implement.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kz.haru.api.command.Command;
import kz.haru.client.Haru;
import kz.haru.implement.commands.AdviceCommand;

import java.util.concurrent.CompletableFuture;

public class CommandListArgument implements ArgumentType<Command> {
    @Override
    public Command parse(StringReader stringReader) throws CommandSyntaxException {
        String commandName = stringReader.readUnquotedString();
        return Haru.getInstance().getCommandManager().getCommandList().stream()
                .filter(command -> {
                    return !(command instanceof AdviceCommand) && command.getHeader().name().equals(commandName);
                })
                .findFirst()
                .orElseThrow(() -> {
                    return CommandSyntaxException.BUILT_IN_EXCEPTIONS
                            .dispatcherUnknownCommand()
                            .create();
                });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Haru.getInstance().getCommandManager().getCommandList().stream()
                .filter(command -> {
                    return !(command instanceof AdviceCommand);
                })
                .forEach(command -> {
            builder.suggest(command.getHeader().name());
        });
        return builder.buildFuture();
    }
}
