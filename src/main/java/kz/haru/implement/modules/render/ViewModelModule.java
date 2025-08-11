package kz.haru.implement.modules.render;

import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;

@ModuleRegister(name = "View Model", category = Category.RENDER, desc = "Рукожоп")
public class ViewModelModule extends Module {
    public final FloatSetting rightX = new FloatSetting("Right X").value(-0.1f).range(-2f, 2f).step(0.1f);
    public final FloatSetting rightY = new FloatSetting("Right Y").value(-0.1f).range(-2f, 2f).step(0.1f);
    public final FloatSetting rightZ = new FloatSetting("Right Z").value(0f).range(-2f, 2f).step(0.1f);
    public final FloatSetting leftX = new FloatSetting("Left X").value(0.1f).range(-2f, 2f).step(0.1f);
    public final FloatSetting leftY = new FloatSetting("Left Y").value(-0.1f).range(-2f, 2f).step(0.1f);
    public final FloatSetting leftZ = new FloatSetting("Left Z").value(0f).range(-2f, 2f).step(0.1f);

    public ViewModelModule() {
        setup(rightX, rightY, rightZ, leftX, leftY, leftZ);
    }

    public static ViewModelModule get() {
        return get(ViewModelModule.class);
    }
}
