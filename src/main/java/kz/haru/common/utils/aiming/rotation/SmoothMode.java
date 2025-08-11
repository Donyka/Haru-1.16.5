package kz.haru.common.utils.aiming.rotation;

import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import kz.haru.common.utils.math.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import static java.lang.Math.*;

public class SmoothMode extends RotationMode {

    public SmoothMode() {
        super("Linear");
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity entity) {
        Rotation rotationDelta = MathUtil.calculateDelta(currentRotation, targetRotation);
        float yawDelta = rotationDelta.yaw;
        float pitchDelta = rotationDelta.pitch;

        float rotationDifference = (float) hypot(abs(yawDelta), abs(pitchDelta));

        float straightLineYaw = abs(yawDelta / rotationDifference) * 180.0f;
        float straightLinePitch = abs(pitchDelta / rotationDifference) * 180.0f;

        return new Rotation(
                currentRotation.yaw + min(max(yawDelta, -straightLineYaw), straightLineYaw),
                currentRotation.pitch + min(max(pitchDelta, -straightLinePitch), straightLinePitch)
        );
    }

    @Override
    public Vector3d randomValue() {
        return new Vector3d(0.0, 0.0, 0.0);
    }
}