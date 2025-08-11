package kz.haru.implement.modules.movement;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.controller.RotationConfig;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import kz.haru.common.utils.aiming.rotation.controller.RotationPlan;
import kz.haru.common.utils.aiming.rotation.SmoothMode;
import kz.haru.common.utils.player.movement.MoveUtil;
import kz.haru.common.utils.task.TaskPriority;
import kz.haru.implement.events.player.updates.UpdateEvent;

@ModuleRegister(name = "Flight", category = Category.MOVEMENT, desc = "Аирдропом из окна")
public class FlightModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode").value("Grim Elytra").addValues("Motion", "Grim Elytra");
    private final FloatSetting speedH = new FloatSetting("Horizontal speed").value(1f).range(0.1f, 5f).step(0.1f).setVisible(() -> mode.is("Motion"));
    private final FloatSetting speedV = new FloatSetting("Vertical speed").value(1f).range(0.1f, 5f).step(0.1f).setVisible(() -> mode.is("Motion"));

    public FlightModule() {
        setup(mode, speedH, speedV);
    }

    private long speedRampStartTime = 0;
    private boolean isSpeedRamping = false;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        switch (mode.getValue()) {
            case "Motion" -> {
                MoveUtil.setMotion(speedH.getValue(), mc.player);

                if (mc.gameSettings.keyBindJump.pressed) {
                    mc.player.motion.y = speedV.getValue();
                } else if (mc.gameSettings.keyBindSneak.pressed) {
                    mc.player.motion.y = -speedV.getValue();
                } else {
                    mc.player.motion.y = 0.0f;
                }
            }

            case "Grim Elytra" -> {
                if (mc.player.isElytraFlying() && (mc.player.motion.y > 0.08 || mc.player.fallDistance > 0.1f) && (mc.player.motion.x <= 0.01 && mc.player.motion.z <= 0.01)) {
                    RotationConfig rotationConfig = new RotationConfig(new SmoothMode(), false, true);

                    mc.player.motion.z = 0.0;
                    mc.player.motion.x = 0.0;

                    Rotation rotation = RotationController.getInstance().getRotation();
                    RotationPlan configurable = RotationController.getInstance().getCurrentRotationPlan();
                    float pitch = configurable != null ? rotation.pitch : mc.player.rotationPitch;

                    boolean validPitch = mc.player.rotationPitch >= -30.0f && mc.player.rotationPitch <= 30.0f;

                    if (!isSpeedRamping) {
                        speedRampStartTime = System.currentTimeMillis();
                        isSpeedRamping = true;
                    }

                    long rampDuration = 100L;
                    long elapsed = System.currentTimeMillis() - speedRampStartTime;
                    float progress = Math.min(elapsed / (float)rampDuration, 1f);
                    double currentBaseSpeed = (0.05 * progress);

                    double maxAddedSpeed = 0.06;
                    double maxVerticalSpeed = 1.11;

                    float normalizedPitch = pitch / 90f;
                    double speedAddition = maxAddedSpeed * normalizedPitch * normalizedPitch;

                    double speed = currentBaseSpeed + speedAddition;
                    mc.player.motion.y += speed;

                    if (mc.player.motion.y >= maxVerticalSpeed) {
                        mc.player.motion.y = maxVerticalSpeed;
                    }

                    if (!validPitch) {
                        RotationController.getInstance().rotateTo(new Rotation(mc.player.rotationYaw, 0f), rotationConfig, TaskPriority.CRITICAL_FOR_USER_PROTECTION, this);
                    }
                } else {
                    isSpeedRamping = false;
                }
            }
        }
    }
}
