package kz.haru.common.utils.aiming.rotation.controller;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import kz.haru.common.utils.math.MathUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@Setter
public class RotationPlan implements IMinecraft {
    private final Rotation rotation;
    private final Vector3d vec3d;
    private final Entity entity;
    private final RotationMode angleSmooth;
    private final int ticksUntilReset;
    private final float resetThreshold;
    private final boolean moveCorrection;

    public RotationPlan(Rotation rotation, Vector3d vec3d, Entity entity, RotationMode angleSmooth,
                        int ticksUntilReset, float resetThreshold,
                        boolean moveCorrection) {
        this.rotation = rotation;
        this.vec3d = vec3d;
        this.entity = entity;
        this.angleSmooth = angleSmooth;
        this.ticksUntilReset = ticksUntilReset;
        this.resetThreshold = resetThreshold;
        this.moveCorrection = moveCorrection;
    }

    public Rotation nextRotation(Rotation fromRotation, boolean isResetting) {
        if (isResetting) {
            return angleSmooth.limitAngleChange(fromRotation, MathUtil.fromVec2f(new Vector2f(mc.player.rotationPitch, mc.player.rotationYaw)));
        } else {
            return angleSmooth.limitAngleChange(fromRotation, rotation, vec3d, entity);
        }
    }
}
