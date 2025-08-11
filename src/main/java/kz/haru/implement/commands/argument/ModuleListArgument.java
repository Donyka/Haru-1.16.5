package kz.haru.implement.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kz.haru.api.module.Module;
import kz.haru.client.Haru;
import kz.haru.implement.commands.argument.builder.ArgumentConstants;

import java.util.concurrent.CompletableFuture;

public class ModuleListArgument implements ArgumentType<Module> {

    @Override
    public Module parse(StringReader stringReader) throws CommandSyntaxException {
        String moduleName = ArgumentConstants.LINE_STRING_ARGUMENT.parse(stringReader);
        return Haru.getInstance().getModuleManager().getModules().stream()
                .filter(module -> {
                    return module.getName().equalsIgnoreCase(moduleName);
                })
                .findFirst()
                .orElseThrow(() -> {
                    return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
                });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Haru.getInstance().getModuleManager().getModules().forEach(module -> {
            builder.suggest(module.getName());
        });
        return builder.buildFuture();
    }
}
