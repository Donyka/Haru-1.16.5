package kz.haru.implement.modules.movement;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import kz.haru.common.utils.aiming.rotation.controller.RotationPlan;
import kz.haru.implement.events.player.movement.SlowingEvent;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;

import static java.lang.Math.*;

@ModuleRegister(name = "No Slow", category = Category.MOVEMENT, desc = "Спешишь чоли")
public class NoSlowModule extends Module {

    public final ModeSetting mode = new ModeSetting("Mode").value("Grim").addValues("Grim", "Fixed");


    public NoSlowModule() {
        setup(mode);
    }

    private float transitionProgress = 0f;

    @EventTarget
    public void onFuckingMe(SlowingEvent event) {
        if ( mode.is("Fixed")) {
            if (mc.player.isHandActive() && mc.player.getItemInUseCount() > 1 && mc.player.isOnGround() && mc.player.hurtTime < 1 && !mc.player.isPotionActive(Effects.SPEED)) {
                event.setCancelled(true);
                transitionProgress = Math.min(transitionProgress + 0.15f, 1f);

                Rotation rotation = RotationController.getInstance().getRotation();
                RotationPlan rotationPlan = RotationController.getInstance().getCurrentRotationPlan();

                float yaw = rotationPlan != null ? rotation.yaw : mc.player.rotationYaw;
                float normalizedYaw = (yaw % 360 + 360) % 360;
                float sectorAngle = (normalizedYaw % 90) / 90f;
                float diagonalFactor = (float) pow(Math.min(sectorAngle, 1f - sectorAngle) * 2f, 2);
                diagonalFactor = Math.max(0.1f, Math.min(diagonalFactor, 1.0f));

                if (diagonalFactor > 0.78f) {
                    diagonalFactor = 1f;
                }

                float boostingFactor = diagonalFactor < 0.2f ? 0 : diagonalFactor;

                float boosting = 0.5f + 0.04f * boostingFactor;
                float speed = ((0.171f + 0.031f * diagonalFactor) * boosting) * transitionProgress;

                float forwardSpeed = (mc.player.moveStrafing != 0.0f ? 0.73f : 1f);

                if (mc.player.moveForward < 0) {
                    forwardSpeed = (mc.player.moveStrafing != 0.0f ? 0.4f : 0.63f);
                }

                float strafeSpeed = 0.5f * transitionProgress;

                double yawRad = toRadians(mc.player.rotationYaw + 90);
                double motionX = (cos(yawRad) * mc.player.moveForward + sin(yawRad) * (mc.player.moveStrafing * strafeSpeed)) * forwardSpeed;
                double motionZ = (sin(yawRad) * mc.player.moveForward - cos(yawRad) * (mc.player.moveStrafing * strafeSpeed)) * forwardSpeed;

                mc.player.setMotion(
                        motionX * speed,
                        mc.player.getMotion().y,
                        motionZ * speed
                );
            } else {
                transitionProgress = 0f;
            }
        } else {
            if ((mc.player.getHeldItemOffhand().getUseAction() != UseAction.BLOCK || mc.player.getActiveHand() != Hand.MAIN_HAND) && (mc.player.getHeldItemOffhand().getUseAction() != UseAction.EAT || mc.player.getActiveHand() != Hand.MAIN_HAND)) {
                if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
                    event.setCancelled(true);
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                } else {
                    event.setCancelled(true);
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                }
            }
        }
    }
}
