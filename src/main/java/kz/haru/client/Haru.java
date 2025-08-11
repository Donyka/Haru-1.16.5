package kz.haru.client;

import eu.donyka.rpc.DiscordRPC;
import kz.haru.api.command.CommandManager;
import kz.haru.api.event.EventManager;
import kz.haru.api.module.ModuleManager;
import kz.haru.api.system.macro.MacroManager;
import kz.haru.api.system.staffs.StaffManager;
import kz.haru.api.waveycapes.WaveyCapesBase;
import kz.haru.common.config.clickgui.ConfigClickGui;
import kz.haru.api.system.friends.FriendManager;
import kz.haru.common.config.modules.ConfigModules;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.modules.misc.BaritoneModule;
import kz.haru.implement.screen.alts.AltManager;
import kz.haru.implement.screen.alts.AltManagerScreen;
import kz.haru.implement.screen.clickgui.ClickGUIScreen;
import kz.haru.common.utils.draggable.DraggableManager;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import kz.haru.api.via.ViaMCP;

import java.io.File;
import java.io.IOException;

@Getter
public class Haru {
    @Getter private static Haru instance;

    @Getter private static final String clientConfigsPath = new File(System.getProperty("user.dir"), "saves/New World/DIM1/data/saves/misc").getAbsolutePath();
    @Getter private static final String clientModulesConfigPath = new File(System.getProperty("user.dir"), "saves/New World/DIM1/data/saves/configs").getAbsolutePath();

    private final EventManager eventManager = new EventManager();
    private final ModuleManager moduleManager = new ModuleManager();
    private final MacroManager macroManager = new MacroManager();
    private final CommandManager commandManager = new CommandManager();
    private final WaveyCapesBase waveyCapes = new WaveyCapesBase();

    private ClickGUIScreen clickGuiScreen;
    private AltManagerScreen altManagerScreen;

    ViaMCP viaMCP;

    public Haru() {
        openLinks();
        Fonts.init();
        init();
    }

    public void init() {
        instance = this;

        initVia();
        initCape();
        initRPC();
        initClientRotations();
        initClientFolders();
        initModules();
        initGui();
        initClientSettings();
        initCommands();
        initMacros();
    }

    public static void unload() {
        DiscordRPC.stopRPC();
        StaffManager.saveStaffs();
        FriendManager.saveFriends();
        ConfigModules.saveModules("autoConfig");
        DraggableManager.saveDraggables();
    }

    public static void sendMessage(String message) {
        ITextComponent text = (new StringTextComponent(TextFormatting.LIGHT_PURPLE + "Haru" + TextFormatting.DARK_GRAY + " >> " + TextFormatting.RESET + message));
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 0);
    }

    private void initModules() {
        moduleManager.init();
    }

    private void initRPC() {
        DiscordRPC.startRPC();
    }

    private void initCommands() {
        commandManager.init();
    }

    private void initMacros() {
        macroManager.init();
    }

    private void initCape() {
        waveyCapes.init();
    }

    private void initVia() { viaMCP = new ViaMCP(); }

    private void initClientRotations() {
        new RotationController();
    }

    private void initClientFolders() {
        File configsFolder = new File(clientConfigsPath);
        if (!configsFolder.exists()) {
            if (configsFolder.mkdirs()) {
                System.out.println("Папка конфигураций создана: " + clientConfigsPath);
            } else {
                System.err.println("Не удалось создать папку конфигураций: " + clientConfigsPath);
            }
        }

        File modulesFolder = new File(clientModulesConfigPath);
        if (!modulesFolder.exists()) {
            if (modulesFolder.mkdirs()) {
                System.out.println("Папка модулей создана: " + clientModulesConfigPath);
            } else {
                System.err.println("Не удалось создать папку модулей: " + clientModulesConfigPath);
            }
        }
    }

    private void initClientSettings() {
        UpdateFunctions.getInstance().init();
        AltManager.getInstance().loadAlts();
        StaffManager.loadStaffs();
        FriendManager.loadFriends();
        ConfigModules.loadModules("autoConfig");
        DraggableManager.loadDraggables();
        ConfigClickGui.loadPanelPositions();
    }

    private void openLinks() {
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("rundll32 url.dll,FileProtocolHandler https://t.me/harudlc");
            rt.exec("rundll32 url.dll,FileProtocolHandler https://discord.gg/5F9fZWVRnx");
            rt.exec("rundll32 url.dll,FileProtocolHandler https://fakecrime.bio/donyka");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGui() {
        clickGuiScreen = new ClickGUIScreen(ITextComponent.getTextComponentOrEmpty("Click Gui"));
        altManagerScreen = new AltManagerScreen(ITextComponent.getTextComponentOrEmpty("Alt Manager"));
    }


    public static boolean baritoneAviable() {
        return instance.getModuleManager().get(BaritoneModule.class).isEnabled();
    }
}