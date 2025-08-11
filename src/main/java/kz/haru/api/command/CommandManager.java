package kz.haru.api.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kz.haru.api.event.EventTarget;
import kz.haru.client.Haru;
import kz.haru.implement.commands.*;
import kz.haru.implement.events.connection.HChatEvent;
import lombok.Getter;
import net.minecraft.command.ISuggestionProvider;

import java.util.List;

@Getter
public class CommandManager {
    private final String commandPrefix = ".";
    private final List<Command> commandList = Lists.newArrayList();
    private final CommandDispatcher<ISuggestionProvider> commandDispatcher = new CommandDispatcher<>();

    public void init() {
        List.of(
                new AdviceCommand(),
                new ToggleCommand(),
                new FriendCommand(),
                new ConfigCommand(),
                new StaffCommand(),
                new MacroCommand()
        ).forEach(command -> {
            commandList.add(command);
            LiteralArgumentBuilder<ISuggestionProvider> builder = LiteralArgumentBuilder.literal(command.getHeader().name());
            command.build(builder);
            commandDispatcher.register(builder);
        });

        Haru.getInstance().getEventManager().register(this);
    }

    @EventTarget
    private void onChat(HChatEvent event) {
        String message = event.getMessage();
        if (message.startsWith(commandPrefix)) {
            message = message.substring(1);
            if (message.isEmpty()) {
                return;
            }
            try {
                commandDispatcher.execute(message, null);
            } catch (CommandSyntaxException ignored) { }
            event.setCancelled(true);
        }
    }
}