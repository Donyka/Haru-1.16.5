package kz.haru.implement.modules.render;

import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.client.Haru;
import kz.haru.implement.screen.clickgui.ClickGUIScreen;
import org.lwjgl.glfw.GLFW;

@ModuleRegister(name = "Click GUI", category = Category.RENDER, bind = GLFW.GLFW_KEY_RIGHT_SHIFT, desc = "Меню этого бездарного чита")
public class ClickGUIModule extends Module {
    public static final BooleanSetting onlyOneOpenedModule = new BooleanSetting("Only one opened module").value(false);

    public static final BooleanSetting descriptions = new BooleanSetting("Descriptions").value(true);

    public static final BooleanSetting background = new BooleanSetting("Background").value(true);
    public static final FloatSetting backgroundAlpha = new FloatSetting("Background alpha").value(150f).range(10f, 255f).step(1f).setVisible(() -> background.getValue());

    public ClickGUIModule() {
        setup(onlyOneOpenedModule,
                descriptions, background, backgroundAlpha
        );
    }
    
    public static ClickGUIModule get() {
        return Module.get(ClickGUIModule.class);
    }

    @Override
    public void onDisable() {
        ClickGUIScreen.isExit = true;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(Haru.getInstance().getClickGuiScreen());
        ClickGUIScreen.isExit = false;
        super.onEnable();
    }
}
