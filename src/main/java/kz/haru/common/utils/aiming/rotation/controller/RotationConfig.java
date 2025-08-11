package kz.haru.common.utils.aiming.rotation.controller;

import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import kz.haru.common.utils.aiming.rotation.SmoothMode;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public class RotationConfig {
    private final RotationMode angleSmooth;
    private final boolean changeView;
    private final boolean moveCorrection;

    private final float resetThreshold = 2f;
    private final int ticksUntilReset = 5;

    public static final RotationConfig DEFAULT = new RotationConfig(new SmoothMode(), false, true);

    public RotationConfig(RotationMode angleSmooth, boolean changeView, boolean moveCorrection) {
        this.angleSmooth = angleSmooth;
        this.changeView = changeView;
        this.moveCorrection = moveCorrection;
    }

    public RotationConfig(boolean changeView, boolean moveCorrection) {
        this(new SmoothMode(), changeView, moveCorrection);
    }

    public RotationConfig(boolean changeView) {
        this(new SmoothMode(), changeView, true);
    }

    public RotationPlan createRotationPlan(Rotation rotation, Vector3d vec, Entity entity) {
        return new RotationPlan(rotation, vec, entity, angleSmooth, ticksUntilReset, resetThreshold, moveCorrection);
    }

    public RotationPlan createRotationPlan(Rotation rotation) {
        return new RotationPlan(rotation, null, null, angleSmooth, ticksUntilReset, resetThreshold, moveCorrection);
    }

    public RotationPlan createRotationPlan(Rotation rotation, Vector3d vec, Entity entity, boolean changeLook, boolean moveCorrection) {
        return new RotationPlan(rotation, vec, entity, angleSmooth, ticksUntilReset, resetThreshold, moveCorrection);
    }
}
