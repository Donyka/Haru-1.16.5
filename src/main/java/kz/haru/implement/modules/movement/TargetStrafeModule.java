package kz.haru.implement.modules.movement;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.implement.events.player.updates.UpdateEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "Target Strafe", category = Category.MOVEMENT, desc = "Ускоряет передвижение при нахождении рядом с целью")
public class TargetStrafeModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode").value("Grim").addValues("Grim", "Acceleration", "Collision");
    private final FloatSetting speed = new FloatSetting("Speed").value(1.09f).range(1f, 1.2f).step(0.01f).setVisible(() -> mode.is("Grim"));
    private final FloatSetting radius = new FloatSetting("Radius").value(2.1f).range(1f, 3f).step(0.01f).setVisible(() -> mode.is("Grim"));

    public TargetStrafeModule() {
        setup(mode, speed, radius);
    }
    
    public static TargetStrafeModule get() {
        return Module.get(TargetStrafeModule.class);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mode.is("Acceleration")) {
            if (mc.player.isOnGround()) {
                mc.player.setMotion(
                    mc.player.getMotion().getX() * (1.0f + 0.35f),
                    mc.player.getMotion().getY(),
                    mc.player.getMotion().getZ() * (1.0f + 0.35f)
                );
            }
        } else if (mode.is("Collision")) {
            AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.1);
            int armorStands = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
            boolean canBoost = armorStands > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
            
            if (canBoost && !mc.player.isOnGround()) {
                mc.player.jumpMovementFactor = armorStands > 1 ? 1.0F / (float)armorStands : 0.16F;
            }
        } else if (mode.is("Grim")) {
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (mc.player == entity) continue;

                if (mc.player.getDistance(entity) <= radius.getValue()) {
                    if (!mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown() && 
                        !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindBack.isKeyDown()) {
                        continue;
                    }

                    Vector3d motion = mc.player.getMotion();
                    motion = new Vector3d(
                        motion.x * speed.getValue(),
                        motion.y,
                        motion.z * speed.getValue()
                    );
                    mc.player.setMotion(motion);
                    break;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
