package kz.haru.common.utils.aiming;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.implement.modules.combat.KillAuraModule;
import kz.haru.implement.modules.combat.ElytraTargetModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class RotationUtil implements IMinecraft {
    public static float[] getRotations(Vector3d targetPos) {
        Vector3d eyesPos = mc.player.getEyePosition(1.0f);

        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;
        double dist = MathHelper.sqrt((float) (diffX * diffX + diffZ * diffZ));

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
        float pitch = (float) Math.toDegrees(-Math.atan2(diffY, dist));

        yaw = MouseUtil.applyGCD(yaw, mc.player.rotationYaw);
        pitch = MouseUtil.applyGCD(pitch, mc.player.rotationPitch);

        return new float[]{yaw, pitch};
    }

    public static Vector3d getSpot(Entity entity) {
        if (entity == null) {
            return new Vector3d(0.0, 0.0, 0.0);
        }

        ElytraTargetModule elytraTargetModule = ElytraTargetModule.get();
        KillAuraModule killAuraModule = KillAuraModule.get();

        if (entity instanceof LivingEntity target) {
            if (mc.player.isElytraFlying() && elytraTargetModule.isEnabled()) {
                Vector3d rotationTarget = elytraTargetModule.getLastRotationTarget();
                if (rotationTarget != null && target.equals(killAuraModule.target)) {
                    return rotationTarget;
                }
                
                if (target.equals(killAuraModule.target)) {
                    Vector3d optimalPoint = elytraTargetModule.getOptimalAttackPoint(target);
                    if (optimalPoint != null) {
                        return optimalPoint;
                    }
                }
                
                if (elytraTargetModule.prediction.getValue()) {
                    Vector3d predictedPos = elytraTargetModule.getPredictedPosition(target);
                    if (predictedPos != null) {
                        return predictedPos;
                    }
                }
            }

            float offset = 0f;
            if (killAuraModule.isEnabled() && killAuraModule.target != null && killAuraModule.aimMode.is("Spooky Time")) {
                offset = 0.4f;
            }

            Vector3d spoting = new Vector3d(
                    MathHelper.clamp(mc.player.getEyePosition(1.0f).x, entity.getBoundingBox().minX + offset, entity.getBoundingBox().maxX - offset),
                    MathHelper.clamp(mc.player.getEyePosition(1.0f).y, entity.getBoundingBox().minY, entity.getBoundingBox().maxY),
                    MathHelper.clamp(mc.player.getEyePosition(1.0f).z, entity.getBoundingBox().minZ + offset, entity.getBoundingBox().maxZ - offset)
            );

            if (mc.player.isElytraFlying()) {
                if (elytraTargetModule.isEnabled()) {
                    Vector3d predictedPos = elytraTargetModule.getPredictedPosition(target);
                    if (predictedPos != null) {
                        return predictedPos;
                    }
                }

                return target.getPositionVec().add(0.0, MathHelper.clamp(target.getPosY() - target.getHeight(), 0.0, target.getHeight() / 2.0f), 0.0);
            }

            return spoting;
        } else {
            return new Vector3d(
                    MathHelper.clamp(mc.player.getEyePosition(1.0f).x, entity.getBoundingBox().minX, entity.getBoundingBox().maxX),
                    MathHelper.clamp(mc.player.getEyePosition(1.0f).y, entity.getBoundingBox().minY, entity.getBoundingBox().maxY),
                    MathHelper.clamp(mc.player.getEyePosition(1.0f).z, entity.getBoundingBox().minZ, entity.getBoundingBox().maxZ)
            );
        }
    }
}
