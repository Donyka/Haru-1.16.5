package kz.haru.implement.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kz.haru.api.command.Command;
import kz.haru.api.command.CommandRegister;
import kz.haru.api.system.macro.MacroManager;
import kz.haru.client.Haru;
import kz.haru.client.functions.KeyboardFunctions;
import kz.haru.implement.commands.argument.AnyStringArgument;
import kz.haru.implement.commands.argument.KeyArgument;
import kz.haru.implement.commands.argument.LineStringArgument;
import kz.haru.implement.commands.argument.MacroNameArgument;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TextFormatting;

@CommandRegister(name = "macro", desc = "Управление макросами")
public class MacroCommand extends Command {

    @Override
    public void build(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        // Add macro
        builder.then(literal("add")
                .then(argument("name", new AnyStringArgument())
                        .then(argument("key", new KeyArgument())
                                .then(argument("message", new LineStringArgument())
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            String keyName = StringArgumentType.getString(context, "key");
                                            String message = StringArgumentType.getString(context, "message");
                                            
                                            int keyCode = KeyboardFunctions.getBind(keyName);
                                            if (keyCode == -1) {
                                                sendMessage(TextFormatting.RED + "Клавиша " + keyName + " не найдена!");
                                                return 0;
                                            }
                                            
                                            MacroManager macroManager = Haru.getInstance().getMacroManager();
                                            if (macroManager.hasMacro(name)) {
                                                sendMessage(TextFormatting.RED + "Макрос с таким именем уже существует!");
                                                return 0;
                                            }
                                            
                                            macroManager.addMacro(name, message, keyCode);
                                            sendMessage(TextFormatting.GREEN + "Добавлен макрос с названием " + 
                                                    TextFormatting.RED + name + 
                                                    TextFormatting.GREEN + " с кнопкой " + 
                                                    TextFormatting.RED + keyName + 
                                                    TextFormatting.GREEN + " с командой " + 
                                                    TextFormatting.RED + message);
                                            
                                            return SINGLE_SUCCESS;
                                        })))));
        
        // Remove macro
        builder.then(literal("remove")
                .then(argument("name", new MacroNameArgument())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            
                            MacroManager macroManager = Haru.getInstance().getMacroManager();
                            if (!macroManager.hasMacro(name)) {
                                sendMessage(TextFormatting.RED + "Макрос с таким именем не найден!");
                                return 0;
                            }
                            
                            macroManager.deleteMacro(name);
                            sendMessage(TextFormatting.GREEN + "Макрос " + 
                                    TextFormatting.RED + name + 
                                    TextFormatting.GREEN + " был успешно удален!");
                            
                            return SINGLE_SUCCESS;
                        })));
        
        // Clear macros
        builder.then(literal("clear")
                .executes(context -> {
                    MacroManager macroManager = Haru.getInstance().getMacroManager();
                    macroManager.clearList();
                    
                    sendMessage(TextFormatting.GREEN + "Все макросы были удалены.");
                    
                    return SINGLE_SUCCESS;
                }));
        
        // List macros
        builder.then(literal("list")
                .executes(context -> {
                    MacroManager macroManager = Haru.getInstance().getMacroManager();
                    
                    if (macroManager.isEmpty()) {
                        sendMessage(TextFormatting.RED + "Список пустой");
                        return 0;
                    }
                    
                    macroManager.macroList.forEach(macro -> {
                        sendMessage(TextFormatting.WHITE + "Название: " + TextFormatting.GRAY + macro.getName() + 
                                TextFormatting.WHITE + ", Команда: " + TextFormatting.GRAY + macro.getMessage() + 
                                TextFormatting.WHITE + ", Кнопка: " + TextFormatting.GRAY + 
                                KeyboardFunctions.getBind(macro.getKey()));
                    });
                    
                    return SINGLE_SUCCESS;
                }));
    }
}
