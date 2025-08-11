package kz.haru.implement.modules.combat;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.controller.RotationConfig;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import kz.haru.common.utils.player.world.InventoryUtil;
import kz.haru.common.utils.task.TaskPriority;
import kz.haru.implement.events.player.updates.UpdateEvent;
import kz.haru.implement.modules.movement.GuiMoveModule;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

@ModuleRegister(name = "Elytra Target", category = Category.COMBAT, desc = "Улучшает PVP на элитрах")
public class ElytraTargetModule extends Module {

    public final BooleanSetting safe = new BooleanSetting("Safe").value(true);
    public final BooleanSetting alwaysGlide = new BooleanSetting("Always glide").value(false);
    public final BooleanSetting autoDistance = new BooleanSetting("Auto distance").value(true);

    public final BooleanSetting sharpRotations = new BooleanSetting("Sharp rotations").value(false);
    public final BooleanSetting ignoreKillAuraRotation = new BooleanSetting("Ignore KillAura rotation").value(true);
    public final BooleanSetting lookRotation = new BooleanSetting("Look rotation").value(false);
    public final ModeSetting targetPosition = new ModeSetting("Target position").value("Predict").addValues("Predict", "Center", "Eyes");

    public final BooleanSetting prediction = new BooleanSetting("Prediction").value(true);
    public final ModeSetting predictMode = new ModeSetting("Predict mode").value("Simple").addValues("Simple", "WithGravity").setVisible(() -> prediction.getValue());
    public final BooleanSetting glidingOnly = new BooleanSetting("Gliding only").value(true).setVisible(() -> prediction.getValue());
    public final FloatSetting predictMultiplier = new FloatSetting("Predict multiplier").value(2.0f).range(0.5f, 4.0f).step(0.1f).setVisible(() -> prediction.getValue());

    public final BooleanSetting autoFirework = new BooleanSetting("Auto firework").value(true);
    public final ModeSetting fireworkUseMode = new ModeSetting("Firework use mode").value("Normal").addValues("Normal", "Packet").setVisible(() -> autoFirework.getValue());
    public final FloatSetting extraDistance = new FloatSetting("Extra distance").value(50.0f).range(5.0f, 100.0f).step(0.5f).setVisible(() -> autoFirework.getValue());
    public final FloatSetting cooldown = new FloatSetting("Cooldown (ticks)").value(10.0f).range(1.0f, 50.0f).step(1.0f).setVisible(() -> autoFirework.getValue());
    public final FloatSetting slotResetDelay = new FloatSetting("Slot reset delay").value(0f).range(0f, 20f).step(1f).setVisible(() -> autoFirework.getValue() && fireworkUseMode.is("Normal"));

    private Vector3d targetPrediction = null;
    private Vector3d lastTargetVelocity = Vector3d.ZERO;
    private long lastFireworkTime = 0;
    private static final int IDEAL_DISTANCE = 10;
    private static final float BASE_YAW_SPEED = 45.0f;
    private static final float BASE_PITCH_SPEED = 35.0f;

    private boolean isRotatingWithKillAura = false;

    @Getter
    private Rotation lastCalculatedRotation = null;

    @Getter
    private Vector3d lastRotationTarget = null;

    public ElytraTargetModule() {
        setup(
            safe, alwaysGlide, autoDistance,
            sharpRotations, ignoreKillAuraRotation, lookRotation, targetPosition,
            prediction, predictMode, glidingOnly, predictMultiplier,
            autoFirework, fireworkUseMode, extraDistance, cooldown, slotResetDelay
        );
    }

    public static ElytraTargetModule get() {
        return Module.get(ElytraTargetModule.class);
    }

    public boolean canAlwaysGlide() {
        KillAuraModule aura = KillAuraModule.get();
        return alwaysGlide.getValue() && aura.target != null && mc.player.isElytraFlying() && !mc.player.abilities.isFlying;
    }

    public boolean canIgnoreKillAuraRotations() {
        return isEnabled() && ignoreKillAuraRotation.getValue();
    }

    public Rotation.VecRotation getRotationForKillAura(LivingEntity target) {
        if (!isEnabled() || !mc.player.isElytraFlying() || target == null) {
            return null;
        }

        isRotatingWithKillAura = true;
        
        Vector3d targetPos = calculateRotationTarget(target);
        lastRotationTarget = targetPos;

        Rotation currentRotation = RotationController.getInstance().getCurrentRotation();
        if (currentRotation == null) {
            currentRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        }

        Rotation targetRotation = getRotationToPosition(targetPos);
        Rotation smoothedRotation = processRotation(currentRotation, targetRotation);

        lastCalculatedRotation = smoothedRotation;
        
        return new Rotation.VecRotation(smoothedRotation, targetPos);
    }

    public boolean isRotatingWithKillAura() {
        return isRotatingWithKillAura;
    }

    public Vector3d getOptimalAttackPoint(LivingEntity target) {
        if (target == null) {
            return null;
        }
        
        return calculateRotationTarget(target);
    }

    public void stopRotatingWithKillAura() {
        isRotatingWithKillAura = false;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!mc.player.isElytraFlying()) return;

        KillAuraModule killAura = KillAuraModule.get();
        LivingEntity target = killAura.target;

        if (target == null || !canSeeTarget(target)) {
            resetPredictions();
            return;
        }

        updateTargetPrediction(target);
        
       if (!isRotatingWithKillAura && (!killAura.isEnabled() || !ignoreKillAuraRotation.getValue())) {
            updateRotations(target);
        }

        if (safe.getValue()) {
            AxisAlignedBB playerBox = mc.player.getBoundingBox();
            Vector3d motion = mc.player.getMotion();
            AxisAlignedBB nextBox = playerBox.offset(motion.x, motion.y, motion.z);

            if (!mc.world.hasNoCollisions(nextBox)) {
                mc.player.setMotion(mc.player.getMotion().add(0, 0.1, 0));
            }
        }

        if (autoFirework.getValue()) {
            handleAutoFirework(target);
        }
    }

    private void handleAutoFirework(LivingEntity target) {
        long currentTime = System.currentTimeMillis();
        long cooldownTime = (long) (cooldown.getValue() * 50);

        if (currentTime - lastFireworkTime < cooldownTime) {
            return;
        }

        double distance = mc.player.getDistance(target);
        double extraDistanceRule = extraDistance.getValue();

        if (distance > extraDistanceRule) {
            useFirework();
            lastFireworkTime = currentTime;
        }
    }

    private void useFirework() {
        if (fireworkUseMode.is("Normal")) {
            useFireworkNormal();
        } else if (fireworkUseMode.is("Packet")) {
            useFireworkPacket();
        }
    }
    
    private void useFireworkNormal() {
        if (mc.player.getHeldItemMainhand().getItem() == Items.FIREWORK_ROCKET) {
            InventoryUtil.useItem(Hand.MAIN_HAND);
            return;
        }

        if (mc.player.getHeldItemOffhand().getItem() == Items.FIREWORK_ROCKET) {
            InventoryUtil.useItem(Hand.OFF_HAND);
            return;
        }

        if (GuiMoveModule.slownessIsEnabled()) {
            InventoryUtil.applySlowness(150, this::fireworkAction);
        } else {
            fireworkAction();
        }
    }
    
    private void useFireworkPacket() {
        int prevSlot = mc.player.inventory.currentItem;
        int hSlot = InventoryUtil.findItem(Items.FIREWORK_ROCKET, true);
        
        if (mc.player.getHeldItemOffhand().getItem() == Items.FIREWORK_ROCKET) {
            mc.player.connection.sendPacket(new net.minecraft.network.play.client.CPlayerTryUseItemPacket(Hand.OFF_HAND));
            return;
        }
        
        if (hSlot != -1) {
            if (hSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = hSlot;
                mc.player.connection.sendPacket(new net.minecraft.network.play.client.CHeldItemChangePacket(hSlot));
            }
            
            mc.player.connection.sendPacket(new net.minecraft.network.play.client.CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            
            if (hSlot != prevSlot) {
                mc.player.inventory.currentItem = prevSlot;
                mc.player.connection.sendPacket(new net.minecraft.network.play.client.CHeldItemChangePacket(prevSlot));
            }
        }
    }

    private void fireworkAction() {
        int prevSlot = mc.player.inventory.currentItem;
        int hSlot = InventoryUtil.findItem(Items.FIREWORK_ROCKET, true);
        int invSlot = InventoryUtil.findItem(Items.FIREWORK_ROCKET, false);

        int toSlot = InventoryUtil.findEmptySlot();
        if (toSlot == -1) {
            toSlot = InventoryUtil.findBestSlotInHotBar();
        }

        if (hSlot != -1) {
            InventoryUtil.swapToSlot(hSlot);
            InventoryUtil.useItem(Hand.MAIN_HAND);

            if (slotResetDelay.getValue() > 0) {
                try {
                    Thread.sleep((long)(slotResetDelay.getValue() * 50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            InventoryUtil.swapToSlot(prevSlot);
        } else if (invSlot != -1) {
            InventoryUtil.swapSlots(invSlot, toSlot);
            InventoryUtil.swapToSlot(toSlot);
            InventoryUtil.useItem(Hand.MAIN_HAND);

            if (slotResetDelay.getValue() > 0) {
                try {
                    Thread.sleep((long)(slotResetDelay.getValue() * 50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            InventoryUtil.swapToSlot(prevSlot);
            InventoryUtil.swapSlots(toSlot, invSlot);
        }
    }

    private boolean canSeeTarget(LivingEntity target) {
        return target != null && mc.player.canEntityBeSeen(target);
    }

    private void updateRotations(LivingEntity target) {
        if (target == null) {
            return;
        }

        Vector3d targetPos = calculateRotationTarget(target);

        Rotation currentRotation = RotationController.getInstance().getCurrentRotation();
        if (currentRotation == null) {
            currentRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        }

        Rotation targetRotation = getRotationToPosition(targetPos);
        Rotation smoothedRotation = processRotation(currentRotation, targetRotation);

        RotationController.getInstance().rotateTo(
            smoothedRotation,
            new RotationConfig(lookRotation.getValue(), true),
            TaskPriority.HIGH_IMPORTANCE_2,
            this
        );
    }

    private Vector3d calculateRotationTarget(LivingEntity target) {
        Vector3d basePos;

        if (targetPosition.is("Eyes")) {
            basePos = target.getEyePosition(1.0f);
        } else if (targetPosition.is("Center")) {
            basePos = target.getPositionVec().add(0, target.getHeight() / 2.0, 0);
        } else {
            basePos = getPredictedPosition(target);
        }

        Vector3d randomOffset = getRandomDirectionVector().mul(4.0, 4.0, 4.0);
        Vector3d targetPos = basePos.add(randomOffset);

        if (autoDistance.getValue()) {
            Vector3d direction = targetPos.subtract(mc.player.getPositionVec()).normalize();
            double distanceSq = mc.player.getPositionVec().squareDistanceTo(targetPos);

            if (distanceSq < IDEAL_DISTANCE * IDEAL_DISTANCE) {
                double distanceDiff = IDEAL_DISTANCE - Math.sqrt(distanceSq);
                targetPos = targetPos.add(direction.mul(distanceDiff, distanceDiff, distanceDiff));
            }
        }

        return targetPos;
    }

    private Rotation getRotationToPosition(Vector3d position) {
        Vector3d playerPos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        double diffX = position.x - playerPos.x;
        double diffY = position.y - playerPos.y;
        double diffZ = position.z - playerPos.z;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        return new Rotation(yaw, pitch);
    }

    private Rotation processRotation(Rotation currentRotation, Rotation targetRotation) {
        float yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw);
        float pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch);

        float baseYawSpeed = BASE_YAW_SPEED * (sharpRotations.getValue() ? 1.5f : 1.0f);
        float basePitchSpeed = BASE_PITCH_SPEED * (sharpRotations.getValue() ? 1.5f : 1.0f);

        long currentTime = System.currentTimeMillis();
        boolean shouldBoost = Math.sin(currentTime / 300.0) > 0.8;
        boolean isTargetBehind = Math.abs(yawDifference) > 90.0f;

        float speedMult = shouldBoost ? 2.0f : 1.2f;
        float smoothBoost = shouldBoost ? (float)(Math.sin((currentTime % 360) / 300.0f * Math.PI) * 0.8f + 1.2f) : 1.2f;
        float backTargetMult = isTargetBehind ? (float)(2.2f * Math.sin(currentTime / 150.0) * 0.2 + 1.0) : 1.2f;

        float speed = speedMult * smoothBoost;
        float yawSpeed = baseYawSpeed * speed * backTargetMult;
        float pitchSpeed = basePitchSpeed * speed;

        float microAdjustment = (float)(Math.sin(currentTime / 80.0) * 0.08 + Math.cos(currentTime / 120.0) * 0.05);

        float moveYaw = MathHelper.clamp(yawDifference, -yawSpeed, yawSpeed);
        float movePitch = MathHelper.clamp(pitchDifference, -pitchSpeed, pitchSpeed);

        if (Math.sqrt(yawDifference * yawDifference + pitchDifference * pitchDifference) < 5.0f) {
            moveYaw += microAdjustment * 0.2f;
            movePitch += microAdjustment * 0.8f;
        }

        return new Rotation(
            normalizeAngle(currentRotation.yaw + moveYaw),
            MathHelper.clamp(currentRotation.pitch + movePitch, -90.0f, 90.0f)
        );
    }

    private float getAngleDifference(float target, float current) {
        float diff = normalizeAngle(target - current);
        if (diff > 180) diff -= 360;
        else if (diff < -180) diff += 360;
        return diff;
    }

    private float normalizeAngle(float angle) {
        angle = angle % 360.0f;
        if (angle < 0) angle += 360.0f;
        return angle;
    }

    private void updateTargetPrediction(LivingEntity target) {
        Vector3d currentPos = target.getPositionVec();
        Vector3d prevPos = new Vector3d(target.prevPosX, target.prevPosY, target.prevPosZ);
        Vector3d velocity = currentPos.subtract(prevPos);

        lastTargetVelocity = velocity;

        if (!prediction.getValue() || (glidingOnly.getValue() && !target.isElytraFlying())) {
            targetPrediction = currentPos;
            return;
        }

        double multiplier = predictMultiplier.getValue();

        if (predictMode.is("Simple")) {
            targetPrediction = currentPos.add(velocity.mul(multiplier, multiplier, multiplier));
        } else if (predictMode.is("WithGravity")) {
            targetPrediction = currentPos.add(velocity.mul(multiplier, multiplier, multiplier))
                .subtract(0, 0.5 * 0.05 * multiplier * multiplier, 0);
        }
    }

    public Vector3d getPredictedPosition(LivingEntity target) {
        if (!isEnabled() || target == null) {
            return target.getPositionVec();
        }

        if (prediction.getValue() && targetPrediction != null) {
            return targetPrediction;
        } else if (targetPosition.is("Eyes")) {
            return target.getEyePosition(1.0f);
        } else if (targetPosition.is("Center")) {
            return target.getPositionVec().add(0, target.getHeight() / 2.0, 0);
        }

        return target.getPositionVec();
    }

    private Vector3d getRandomDirectionVector() {
        double timeOffset = System.currentTimeMillis() / 1000.0;
        double randOffset = ThreadLocalRandom.current().nextDouble() - 0.5;

        return new Vector3d(
            Math.sin(timeOffset * 1.8) * 0.04 + randOffset * 0.02,
            Math.sin(timeOffset * 2.2) * 0.03 + randOffset * 0.015,
            Math.cos(timeOffset * 1.8) * 0.04 + randOffset * 0.02
        );
    }

    private void resetPredictions() {
        targetPrediction = null;
        lastTargetVelocity = Vector3d.ZERO;
        lastCalculatedRotation = null;
        lastRotationTarget = null;
        isRotatingWithKillAura = false;
    }

    @Override
    public void onDisable() {
        resetPredictions();
        super.onDisable();
    }
} 