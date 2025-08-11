package kz.haru.implement.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kz.haru.api.command.Command;
import kz.haru.api.command.CommandRegister;
import kz.haru.api.system.staffs.StaffManager;
import kz.haru.implement.commands.argument.AnyStringArgument;
import kz.haru.implement.commands.argument.StaffNameArgument;
import net.minecraft.command.ISuggestionProvider;

import java.util.List;

@CommandRegister(name = "staff", desc = "Управление списком стаффов")
public class StaffCommand extends Command {

    @Override
    public void build(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.then(literal("add")
                        .then(argument("name", new AnyStringArgument())
                                .executes(this::addStaff)))
                .then(literal("remove")
                        .then(argument("name", new StaffNameArgument())
                                .executes(this::removeStaff)))
                .then(literal("list")
                        .executes(this::listStaffs))
                .then(literal("clear")
                        .executes(this::clearStaffs));
    }

    private int addStaff(CommandContext<ISuggestionProvider> context) {
        String name = context.getArgument("name", String.class);
        StaffManager.addStaff(name);
        sendMessage("Стафф " + name + " добавлен.");
        return SINGLE_SUCCESS;
    }

    private int removeStaff(CommandContext<ISuggestionProvider> context) {
        String name = context.getArgument("name", String.class);
        if (StaffManager.isStaff(name)) {
            sendMessage("Стафф " + name + " удален.");
            StaffManager.removeStaff(name);
        } else {
            sendMessage("Стафф " + name + " не найден.");
        }
        return SINGLE_SUCCESS;
    }

    private int listStaffs(CommandContext<ISuggestionProvider> context) {
        List<String> friends = StaffManager.getStaffNames();
        if (friends.isEmpty()) {
            sendMessage("Список стаффов пуст.");
        } else {
            sendMessage("Список стаффов:");
            friends.forEach(this::sendMessage);
        }
        return SINGLE_SUCCESS;
    }

    private int clearStaffs(CommandContext<ISuggestionProvider> context) {
        StaffManager.clear();
        sendMessage("Список стаффов очищен.");
        return SINGLE_SUCCESS;
    }
}
