package kz.haru.implement.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kz.haru.client.Haru;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ConfigNameArgument implements ArgumentType<String> {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        File configFolder = new File(Haru.getClientModulesConfigPath());
        String[] configFiles = configFolder.list((dir, name) -> name.endsWith(".json"));

        if (configFiles != null) {
            for (String file : configFiles) {
                String configName = file.replace(".json", "");
                builder.suggest(configName);
            }
        }

        return builder.buildFuture();
    }
}
