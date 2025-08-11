package kz.haru.implement.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kz.haru.api.command.Command;
import kz.haru.api.command.CommandRegister;
import kz.haru.api.system.friends.FriendManager;
import kz.haru.implement.commands.argument.AnyStringArgument;
import kz.haru.implement.commands.argument.FriendNameArgument;
import net.minecraft.command.ISuggestionProvider;

import java.util.List;

@CommandRegister(name = "friend", desc = "Управление списком друзей")
public class FriendCommand extends Command {

    @Override
    public void build(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.then(literal("add")
                        .then(argument("name", new AnyStringArgument())
                                .executes(this::addFriend)))
                .then(literal("remove")
                        .then(argument("name", new FriendNameArgument())
                                .executes(this::removeFriend)))
                .then(literal("list")
                        .executes(this::listFriends))
                .then(literal("clear")
                        .executes(this::clearFriends));
    }

    private int addFriend(CommandContext<ISuggestionProvider> context) {
        String name = context.getArgument("name", String.class);
        FriendManager.addFriend(name);
        sendMessage("Друг " + name + " добавлен.");
        return SINGLE_SUCCESS;
    }

    private int removeFriend(CommandContext<ISuggestionProvider> context) {
        String name = context.getArgument("name", String.class);
        if (FriendManager.isFriend(name)) {
            sendMessage("Друг " + name + " удален.");
            FriendManager.removeFriend(name);
        } else {
            sendMessage("Друг " + name + " не найден.");
        }
        return SINGLE_SUCCESS;
    }

    private int listFriends(CommandContext<ISuggestionProvider> context) {
        List<String> friends = FriendManager.getFriendNames();
        if (friends.isEmpty()) {
            sendMessage("Список друзей пуст.");
        } else {
            sendMessage("Список друзей:");
            friends.forEach(this::sendMessage);
        }
        return SINGLE_SUCCESS;
    }

    private int clearFriends(CommandContext<ISuggestionProvider> context) {
        FriendManager.clear();
        sendMessage("Список друзей очищен.");
        return SINGLE_SUCCESS;
    }
}