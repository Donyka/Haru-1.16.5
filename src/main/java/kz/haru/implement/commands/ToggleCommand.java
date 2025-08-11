package kz.haru.implement.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kz.haru.api.command.Command;
import kz.haru.api.command.CommandRegister;
import kz.haru.api.module.Module;
import kz.haru.implement.commands.argument.ModuleListArgument;
import net.minecraft.command.ISuggestionProvider;

@CommandRegister(name = "toggle", desc = "Переключает состояние активности модуля")
public class ToggleCommand extends Command {
    @Override
    public void build(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.then(super.argument("modules", new ModuleListArgument())
                .executes(context -> {
                    context.getArgument("modules", Module.class).toggle();
                    return super.SINGLE_SUCCESS;
                }));
    }
}
