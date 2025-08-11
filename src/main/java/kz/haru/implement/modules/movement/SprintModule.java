package kz.haru.implement.modules.movement;

import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import lombok.Getter;
import lombok.Setter;

@ModuleRegister(name = "Sprint", category = Category.MOVEMENT, desc = "Спринт. Никакой магии, честно!")
public class SprintModule extends Module {
    public static SprintModule get() {
        return Module.get(SprintModule.class);
    }

    @Getter
    @Setter
    private boolean canSprint = true;
}
