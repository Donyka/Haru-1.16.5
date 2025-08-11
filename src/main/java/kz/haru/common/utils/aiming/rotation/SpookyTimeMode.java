package kz.haru.common.utils.aiming.rotation;

import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import kz.haru.common.utils.math.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import static java.lang.Math.*;
import static java.lang.Math.abs;

public class SpookyTimeMode extends RotationMode {
    public SpookyTimeMode() {
        super("SpookyTime");
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity target) {
        Rotation rotationDelta = MathUtil.calculateDelta(currentRotation, targetRotation);
        float yawDelta = rotationDelta.yaw;
        float pitchDelta = rotationDelta.pitch;

        float mainBypass = (float) Math.sin(System.currentTimeMillis() / 150.0) * 15.0f + (float) (Math.random() * 3.0 - 1.5);
        float rotationDifference = (float) hypot(abs(yawDelta), abs(pitchDelta));

        float maxChangeYaw = abs(yawDelta / rotationDifference) * 60.0f;
        float maxChangePitch = 8.0f; // RARE PITCH UPDATE OR LOW SPEED FOR BYPASS BTW

        return new Rotation(
                currentRotation.yaw + min(max(yawDelta + mainBypass, -maxChangeYaw), maxChangeYaw),
                currentRotation.pitch + min(max(pitchDelta, -maxChangePitch), maxChangePitch)
        );
    }

    @Override
    public Vector3d randomValue() {
        return Vector3d.ZERO;
    }
}