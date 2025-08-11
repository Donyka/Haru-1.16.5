package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.common.utils.aiming.RotationUtil;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.controller.RotationConfig;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import kz.haru.common.utils.task.TaskPriority;
import kz.haru.implement.events.player.updates.UpdateEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleRegister(name = "Auto Pilot", category = Category.PLAYER, desc = "Автоматически указаывает на ценные предметы")
public class AutoPilotModule extends Module {
    private final MultiModeSetting targetItems = new MultiModeSetting("Target items").addValues("Player heads", "Elytra", "Spawn eggs");
    private final BooleanSetting clientLook = new BooleanSetting("Client look").value(false);
    private final FloatSetting searchRange = new FloatSetting("Search range").value(20f).range(1f, 100f).step(1f);

    public AutoPilotModule() {
        setup(targetItems, clientLook, searchRange);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        Entity targetEntity = findTargetEntity();
        if (targetEntity != null) {
            Vector3d targetPos = targetEntity.getPositionVec().add(0, targetEntity.getHeight() / 2, 0);
            float[] rotations = RotationUtil.getRotations(targetPos);

            if (clientLook.getValue()) {
                mc.player.rotationYaw = rotations[0];
                mc.player.rotationPitch = rotations[1];
            } else {
                RotationConfig config = new RotationConfig(true, true);

                RotationController.getInstance().rotateTo(new Rotation(rotations[0], rotations[1]), config, TaskPriority.HIGH_IMPORTANCE_2, this);
            }
        }
    }

    private Entity findTargetEntity() {
        if (mc.player == null || mc.world == null) return null;

        AxisAlignedBB searchBox = new AxisAlignedBB(mc.player.getPosX() - searchRange.getValue(), mc.player.getPosY() - 2, mc.player.getPosZ() - searchRange.getValue(), mc.player.getPosX() + searchRange.getValue(), mc.player.getPosY() + 2, mc.player.getPosZ() + searchRange.getValue());

        List<ItemEntity> validItems = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ItemEntity && entity.getBoundingBox().intersects(searchBox) && isValidItem((ItemEntity)entity)) {
                validItems.add((ItemEntity)entity);
            }
        }

        return validItems.stream().min(Comparator.comparingDouble(entity -> mc.player.getDistanceSq(entity))).orElse(null);
    }

    private boolean isValidItem(ItemEntity entity) {
        Item item = entity.getItem().getItem();
        return (targetItems.is("Elytra") && item == Items.ELYTRA) ||
                (targetItems.is("Player heads") && item == Items.PLAYER_HEAD) ||
                (targetItems.is("Spawn eggs") && item instanceof SpawnEggItem);
    }
}
