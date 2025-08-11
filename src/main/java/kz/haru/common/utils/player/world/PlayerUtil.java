package kz.haru.common.utils.player.world;

import kz.haru.implement.modules.movement.GuiMoveModule;


public class PlayerUtil {
    public static boolean isStoppedByModule(String name) {
        GuiMoveModule guiMoveModule = GuiMoveModule.get();

        return guiMoveModule.slowed && !guiMoveModule.getName().equals(name) || InventoryUtil.isSlowed;
    }


}
