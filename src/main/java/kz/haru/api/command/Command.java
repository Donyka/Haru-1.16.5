package kz.haru.api.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import kz.haru.common.interfaces.IMinecraft;
import lombok.Getter;
import net.minecraft.command.ISuggestionProvider;

public abstract class Command implements IMinecraft {
    protected final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;
    @Getter
    private final CommandRegister header = this.getClass().getAnnotation(CommandRegister.class);

    public abstract void build(LiteralArgumentBuilder<ISuggestionProvider> builder);

    protected LiteralArgumentBuilder<ISuggestionProvider> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    protected <T> RequiredArgumentBuilder<ISuggestionProvider, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}