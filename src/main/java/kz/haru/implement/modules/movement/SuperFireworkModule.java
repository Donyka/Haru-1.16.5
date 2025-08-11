package kz.haru.implement.modules.movement;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.player.movement.FireworkEvent;
import kz.haru.implement.modules.combat.KillAuraModule;
import kz.haru.implement.modules.combat.ElytraTargetModule;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import kz.haru.implement.events.render.Render2DEvent;
import kz.haru.client.functions.UpdateFunctions;
import net.minecraft.client.MainWindow;
import java.text.DecimalFormat;
import java.awt.Color;


@ModuleRegister(name = "Super Firework", category = Category.MOVEMENT, desc = "Увеличивает мощность фейерверка при полете на элитре")
public class SuperFireworkModule extends Module {
    private final ModeSetting boostMode = new ModeSetting("Выбор мода").value("Custom").addValues("Custom", "LegendsGrief","RW");
    private final FloatSetting boosting = new FloatSetting("Custom boost").value(0.5f).range(0.1f, 10f).step(0.1f).setVisible(() -> boostMode.is("Custom"));
    private final BooleanSetting verticalBps = new BooleanSetting("Vertical BPS").value(false);
    private final BooleanSetting writeAdvantage = new BooleanSetting("Write advantage").value(true);
    private final BooleanSetting dopBoost = new BooleanSetting("Bust Y").value(true);
    private final BooleanSetting dopBoostt = new BooleanSetting("Bust Y+").value(true).setVisible(() -> dopBoost.getValue());
    private final BooleanSetting distanceBasedSpeed = new BooleanSetting("Distance based speed").value(true);

    private final DecimalFormat df = new DecimalFormat("0.0");

    private long advantageStartTime = 0;
    private boolean hasAdvantage = false;
    private float animationProgress = 0f;
    private float targetAnimationProgress = 0f;


    public SuperFireworkModule() {
        setup(boostMode, boosting,dopBoost,dopBoostt, verticalBps, writeAdvantage, distanceBasedSpeed);
    }

    @EventTarget
    public void onFirework(FireworkEvent event) {
        switch (boostMode.getValue()) {
            case "Custom" -> event.setSpeed(1.5f + boosting.getValue() * 0.01f);
            case "LegendsGrief", "RW" -> event.setSpeed(calculateBoost());
            default -> event.setSpeed(1.5f);
        }
    }

    private float calculateBoost() {
        boolean inTarget = false;
        KillAuraModule aura = KillAuraModule.get();
        ElytraTargetModule elytraTarget = ElytraTargetModule.get();
        Vector3d targetVector = null;
        if (aura.target != null) {
            AxisAlignedBB targetBB = aura.target.getBoundingBox();
            Vector3d bestCandidate = new Vector3d(
                    (targetBB.minX + targetBB.maxX) / 2.0,
                    (targetBB.minY + targetBB.maxY) / 2.0,
                    (targetBB.minZ + targetBB.maxZ) / 2.0
            );

            if (aura.target.isElytraFlying() && elytraTarget.isEnabled() && elytraTarget.prediction.getValue()) {
                Vector3d predictedPos = elytraTarget.getPredictedPosition(aura.target);
                if (predictedPos != null) {
                    bestCandidate = predictedPos;
                }

                if (distanceBasedSpeed.getValue()) {
                    // к фантому хитбоксу добавляем движение таргета умноженое на 0.5
                    bestCandidate.add(aura.target.getMotion().scale(0.5));
                }
            }

            targetVector = bestCandidate;

            if (mc.player.getEyePosition(1.0F).distanceTo(bestCandidate) < 2.9f) {
                inTarget = true;
            }
        }

        Rotation rotation = RotationController.getInstance().getCurrentRotation();
        float yaw = rotation != null ? rotation.yaw : mc.player.rotationYaw;
        float pitch = rotation != null ? rotation.pitch : mc.player.rotationPitch;

        float speed = 1.615f;
        int[] yawAngles = {45, 135, 225, 315};
        int[][] yawRanges = {
                {13, 14, 18, 19, 20, 22, 23, 25, 26, 29},
                {13, 14, 17, 18, 19, 23, 24, 28},
                {13, 14, 17, 18, 19, 23, 24, 28},
                {13, 14, 17, 18, 19, 23, 24, 28}
        };
        float[][] yawSpeeds;
        if (boostMode.is("LegendsGrief")) {
            yawSpeeds = new float[][]{
                    {2.0255f,2.065f, 2.045f, 2.035f, 2.025f, 1.965f, 1.78f, 1.76f, 1.73f, 1.72f, 1.7f},
                    {2.0255f, 2.065f, 2.045f, 2.035f, 2.025f, 1.965f, 1.77f, 1.75f, 1.7f},
                    {2.0255f, 2.065f, 2.045f, 2.035f, 2.025f, 1.965f, 1.77f, 1.75f, 1.7f},
                    {2.0255f, 2.065f, 2.045f, 2.035f, 2.025f, 1.965f, 1.77f, 1.75f, 1.7f}
//BACKUP
                    //{2.025f, 2.065f, 1.91f, 1.85f, 1.83f, 1.8f, 1.78f, 1.76f, 1.73f, 1.72f, 1.7f},
                    //{2.025f, 2.065f, 1.91f, 1.85f, 1.82f, 1.8f, 1.77f, 1.75f, 1.7f},
                    //{2.025f, 2.065f, 1.91f, 1.85f, 1.82f, 1.8f, 1.77f, 1.75f, 1.7f},
                    //{2.025f, 2.065f, 1.91f, 1.85f, 1.82f, 1.8f, 1.77f, 1.75f, 1.7f}
            };
        } else {
            yawSpeeds = new float[][]{
                    {1.805f, 1.805f, 1.87f, 1.85f, 1.83f, 1.8f, 1.78f, 1.76f, 1.73f, 1.72f, 1.7f},
                    {1.805f, 1.805f, 1.87f, 1.85f, 1.82f, 1.8f, 1.77f, 1.75f, 1.7f},
                    {1.805f, 1.805f, 1.87f, 1.85f, 1.82f, 1.8f, 1.77f, 1.75f, 1.7f},
                    {1.805f, 1.805f, 1.87f, 1.85f, 1.82f, 1.8f, 1.77f, 1.75f, 1.7f}
            };
        }

        for (int i = 0; i < yawAngles.length; i++) {
            int currentYaw = yawAngles[i];
            int[] ranges = yawRanges[i];
            float[] speeds = yawSpeeds[i];

            if (isYawInRange(yaw, currentYaw, ranges[0])) {
                if (pitch >= 1 && pitch <= 90) {
                    speed = (speeds[0]);
                } else {
                    speed = (speeds[1]);
                }
            }

            for (int j = 1; j < ranges.length; j++) {
                if (isYawInRange(yaw, currentYaw, ranges[j]) && !isYawInRange(yaw, currentYaw, ranges[j - 1])) {
                    speed = (speeds[j + 1]);
                }
            }
        }

        if (pitch <= -30 || pitch >= 30) {
            speed = 1.615f;
        }
        if (pitch <= -80 || pitch >= 80) {
            speed = 1.715f;
        }
        boolean isDiagonalYaw = isYawInRange(yaw, 45.0f, 20.0f) ||
                isYawInRange(yaw, 135.0f, 20.0f) ||
                isYawInRange(yaw, 225.0f, 20.0f) ||
                isYawInRange(yaw, 315.0f, 20.0f);
        if (!isDiagonalYaw && ((pitch >= 15.0f && pitch <= 35.0f) || (pitch <= -15.0f && pitch >= -35.0f))) {
            speed = 1.7f;}
        if (dopBoost.getValue()){


            if (((pitch >= 35.0f && pitch <= 60f) || (pitch <= -35.0f && pitch >= -52.0f))) {
                if (dopBoostt.getValue()) {
                    speed = 2.15f;
                }
                else{
                    speed = 1.95f;
                }
            }}

        float targetSpeed = 1.5f;

        if (distanceBasedSpeed.getValue() && targetVector != null) {
            // по мере приблежения уменьшаем скорость
            float distanceFactor = (float) (mc.player.getEyePosition(1.0f).distanceTo(targetVector) / aura.getAttackDistance());
            targetSpeed = Math.max(1.3f, speed * distanceFactor); // минимум 1.3
        }

        return inTarget ? targetSpeed : speed;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || mc.player == null || event.isFirstLayer()) return;

        if (!mc.player.isElytraFlying()) {
            hasAdvantage = false;
            advantageStartTime = 0;
            targetAnimationProgress = 0f;
            return;
        }

        double xDiff = mc.player.getPosX() - mc.player.prevPosX;
        double zDiff = mc.player.getPosZ() - mc.player.prevPosZ;
        double horizontalBps = Math.sqrt(xDiff * xDiff + zDiff * zDiff) * 20;

        double yDiff = mc.player.getPosY() - mc.player.prevPosY;
        double playerSpeed = Math.sqrt(xDiff * xDiff + zDiff * zDiff + yDiff * yDiff) * 20;


        {
            double displayBps = verticalBps.getValue() ? playerSpeed : horizontalBps;
            String bpsText = df.format(displayBps) + " BPS";

            MainWindow window = mc.getMainWindow();
            float scale = UpdateFunctions.getInstance().getScaleFactor();
            float fontSize = 6.0f * scale;

            float textWidth = Fonts.bold.getWidth(bpsText, fontSize);
            float x = window.getScaledWidth() / 2f - textWidth / 2f;
            float y = window.getScaledHeight() / 2f + 13.5f;
            Rotation rotation = RotationController.getInstance().getCurrentRotation();
            float pitch = rotation != null ? rotation.pitch : mc.player.rotationPitch;
            float yaw = rotation != null ? rotation.yaw : mc.player.rotationYaw;
            boolean yawGreen = isYawInRange(yaw, 45.0f, 14.0f)
                    || isYawInRange(yaw, 135.0f, 14.0f)
                    || isYawInRange(yaw, 225.0f, 14.0f)
                    || isYawInRange(yaw, 315.0f, 14.0f);
            if (((pitch >= 35.0f && pitch <= 60f) || (pitch <= -35.0f && pitch >= -52.0f))) {
                int color = new Color(30, 255, 30).getRGB();
                Fonts.bold.drawText(event.getMatrixStack(), bpsText, x, y, color, fontSize);
            }
            else if(yawGreen){
                int color = new Color(60, 190, 255).getRGB();
                Fonts.bold.drawText(event.getMatrixStack(), bpsText, x, y, color, fontSize);
            }
            else{
                Fonts.bold.drawText(event.getMatrixStack(), bpsText, x, y, -1, fontSize);
            }}

        if (writeAdvantage.getValue()) {
            KillAuraModule aura = KillAuraModule.get();
            if (aura.target != null && aura.target.isElytraFlying()) {
                double targetXDiff = aura.target.getPosX() - aura.target.prevPosX;
                double targetZDiff = aura.target.getPosZ() - aura.target.prevPosZ;
                double targetYDiff = aura.target.getPosY() - aura.target.prevPosY;
                double targetSpeed = Math.sqrt(targetXDiff * targetXDiff + targetZDiff * targetZDiff + targetYDiff * targetYDiff) * 20;

                if (playerSpeed > targetSpeed && targetSpeed > 1.2f) {
                    if (!hasAdvantage) {
                        hasAdvantage = true;
                        advantageStartTime = System.currentTimeMillis();
                    }

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - advantageStartTime >= 2000) {
                        targetAnimationProgress = 1.0f;

                        String advantageText = "Перегоняю!";
                        MainWindow window = mc.getMainWindow();
                        float scale = UpdateFunctions.getInstance().getScaleFactor();
                        float fontSize = 7.0f * scale;

                        float textWidth = Fonts.bold.getWidth(advantageText, fontSize);
                        float x = window.getScaledWidth() / 2f - textWidth / 2f;
                        float y = window.getScaledHeight() / 2f + 18f;

                        animationProgress += (targetAnimationProgress - animationProgress) * 0.1f;

                        float pulse = (float) Math.sin(System.currentTimeMillis() / 300.0) * 0.2f + 0.8f;
                        int green = (int) (180 * pulse + 75);
                        int color = new Color(30, green, 30).getRGB();

                        float animScale = 0.8f + animationProgress * 0.2f;
                        fontSize *= animScale;

                        Fonts.bold.drawText(event.getMatrixStack(), advantageText, x, y, color, fontSize);
                    }
                } else {
                    hasAdvantage = false;
                    advantageStartTime = 0;
                    targetAnimationProgress = 0f;
                }
            } else {
                hasAdvantage = false;
                advantageStartTime = 0;
                targetAnimationProgress = 0f;
            }
        }

        if (targetAnimationProgress == 0f && animationProgress > 0.01f) {
            animationProgress *= 0.9f;
        }
    }

    public static boolean isYawInRange(float yaw, float firstValue, float radiusValue) {
        yaw = (yaw % 360 + 360) % 360;
        firstValue = (firstValue % 360 + 360) % 360;

        float minValue = (firstValue - radiusValue + 360) % 360;
        float maxValue = (firstValue + radiusValue) % 360;

        if (minValue < maxValue) {
            return yaw >= minValue && yaw <= maxValue;
        } else {
            return yaw >= minValue || yaw <= maxValue;
        }
    }

    private boolean isValidTarget() {
        KillAuraModule aura = KillAuraModule.get();
        ElytraTargetModule elytraTarget = ElytraTargetModule.get();
        return mc.player != null && mc.player.isElytraFlying() && elytraTarget.isEnabled() && aura.isEnabled() && aura.target != null;
    }

    private void renderTracking(Render2DEvent event) {
        // ... existing code ...
        KillAuraModule aura = KillAuraModule.get();
        // ... existing code ...
    }
}
