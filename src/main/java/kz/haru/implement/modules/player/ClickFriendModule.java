package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BindSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.api.system.friends.FriendManager;
import kz.haru.client.functions.KeyboardFunctions;
import kz.haru.common.utils.player.world.InventoryUtil;
import kz.haru.implement.events.minecraft.MTickEvent;
import kz.haru.implement.modules.movement.GuiMoveModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

@ModuleRegister(name = "Click Friend", category = Category.PLAYER, desc = "Добовляет в друзья по бинду")
public class ClickFriendModule extends Module {
    private final BindSetting throwKey = new BindSetting("Key").value(-1);

    public ClickFriendModule() {
        setup(throwKey);
    }

    private boolean isThrowed;

    @EventTarget
    public void onTick(MTickEvent event) {
        clickFriendLogic();
    }

    private void clickFriendLogic() {

        if (!KeyboardFunctions.isPressed(throwKey.getValue())) {
            isThrowed = false;
        }


        if (KeyboardFunctions.isPressed(throwKey.getValue()) && !isThrowed && mc.pointedEntity instanceof PlayerEntity) {
            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();



            if (FriendManager.isFriend(playerName)) {
                FriendManager.removeFriend(playerName);
                printStatus(playerName, true);
            } else {
                FriendManager.addFriend(playerName);
                printStatus(playerName, false);
            }

            isThrowed = true;

        }
    }


    void printStatus(String name, boolean remove) {
        if (remove) sendMessage(name + " удалён из друзей");
        else sendMessage(name + " добавлен в друзья");
    }

}
