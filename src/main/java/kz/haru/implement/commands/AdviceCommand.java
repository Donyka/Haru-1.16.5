package kz.haru.implement.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kz.haru.api.command.Command;
import kz.haru.api.command.CommandRegister;
import kz.haru.implement.commands.argument.CommandListArgument;
import net.minecraft.command.ISuggestionProvider;

@CommandRegister(name = "advice", desc = "Ну раз тебе надо помощь с такой командой, то могу только соболезновать")
public class AdviceCommand extends Command {
    @Override
    public void build(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.then(super.argument("commands", new CommandListArgument())
                .executes(context -> {
                    String commandHelp = context.getArgument("commands", Command.class).getHeader().desc();
                    sendMessage(commandHelp);
                    return SINGLE_SUCCESS;
                }));
    }
}
