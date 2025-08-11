package kz.haru.implement.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kz.haru.api.system.macro.MacroManager;
import kz.haru.client.Haru;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MacroNameArgument implements ArgumentType<String> {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return StringArgumentType.string().parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        // Получаем имена всех существующих макросов
        List<String> macroNames = Haru.getInstance().getMacroManager().macroList.stream()
                .map(MacroManager.Macro::getName)
                .collect(Collectors.toList());
        
        String remaining = builder.getRemaining().toLowerCase();
        macroNames.stream()
                .filter(name -> name.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("home", "login", "tp");
    }
}
