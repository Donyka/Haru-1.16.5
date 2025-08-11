package kz.haru.implement.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kz.haru.client.functions.KeyboardFunctions;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KeyArgument implements ArgumentType<String> {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return StringArgumentType.string().parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> keys = KeyboardFunctions.getKeyNames();
        
        String remaining = builder.getRemaining().toLowerCase();
        keys.stream()
                .filter(keyName -> keyName.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("H", "F", "Space", "Enter", "Escape", "LShift");
    }
}
