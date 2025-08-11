package kz.haru.common.utils.aiming.rotation;

import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public class FixedMode extends RotationMode {
    public FixedMode() {
        super("NOT BYPASS AND SEX ROTATION");
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity entity) {
        return targetRotation;
    }

    @Override
    public Vector3d randomValue() {
        return Vector3d.ZERO;
    }
}
