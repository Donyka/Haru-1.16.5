package kz.haru.implement.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kz.haru.api.system.friends.FriendManager;

import java.util.concurrent.CompletableFuture;

public class FriendNameArgument implements ArgumentType<String> {
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        if (!FriendManager.isFriend(name)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                    .dispatcherUnknownArgument()
                    .create();
        }
        return name;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        FriendManager.getFriendNames().forEach(builder::suggest);
        return builder.buildFuture();
    }
}