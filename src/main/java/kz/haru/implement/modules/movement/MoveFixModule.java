package kz.haru.implement.modules.movement;

import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;

@ModuleRegister(name = "Move Fix", category = Category.MOVEMENT, desc = "Корректрирует движение")
public class MoveFixModule extends Module {
    public static MoveFixModule get() {
        return Module.get(MoveFixModule.class);
    }
}
