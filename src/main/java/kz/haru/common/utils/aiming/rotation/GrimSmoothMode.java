package kz.haru.common.utils.aiming.rotation;

import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import kz.haru.common.utils.math.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class GrimSmoothMode extends RotationMode {
    private float lastYawDelta = 0.0f;
    private float lastPitchDelta = 0.0f;
    private int lastPitchChangeDirection = 0;
    private Rotation lastSentAngle = new Rotation(0f, 0f);
    private final Random random = new Random();

    public GrimSmoothMode() {
        super("Grim");
    }

    @Override
    public Rotation limitAngleChange(Rotation currentAngle, Rotation targetAngle, Vector3d vec3d, Entity entity) {
        if (currentAngle.equals(lastSentAngle)) {
            Rotation microJitter = getMicroJitter();
            return new Rotation(currentAngle.yaw + microJitter.yaw, currentAngle.pitch + microJitter.pitch);
        }

        Rotation angleDelta = MathUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.yaw;
        float pitchDelta = angleDelta.pitch;

        yawDelta = normalizeAngle(yawDelta);
        pitchDelta = normalizeAngle(pitchDelta);

        yawDelta = applyHumanError(yawDelta);
        pitchDelta = applyHumanError(pitchDelta);

        if (Math.abs(pitchDelta) < 0.01f) {
            pitchDelta += getMicroJitter().pitch;
        }

        handleDirectionChange(pitchDelta);

        lastYawDelta = yawDelta;
        lastPitchDelta = pitchDelta;
        lastSentAngle = new Rotation(currentAngle.yaw + yawDelta, currentAngle.pitch + pitchDelta);

        return lastSentAngle;
    }

    private float normalizeAngle(float angle) {
        if (angle > 180.25f) {
            return angle - 360f;
        } else if (angle < -180.25f) {
            return angle + 360f;
        } else {
            return angle;
        }
    }

    private float applyHumanError(float value) {
        return value * 0.97f + (random.nextFloat() * 0.06f - 0.03f);
    }

    private Rotation getMicroJitter() {
        return new Rotation(
                (random.nextFloat() * 0.02f - 0.01f),
                (random.nextFloat() * 0.02f - 0.01f)
        );
    }

    private void handleDirectionChange(float pitchDelta) {
        int currentDirection = (int) Math.signum(pitchDelta);
        if (lastPitchChangeDirection != 0 && currentDirection != lastPitchChangeDirection) {
            lastPitchChangeDirection = currentDirection;
        }
    }

    @Override
    public Vector3d randomValue() {
        return new Vector3d(
                (random.nextDouble() * 0.02 - 0.01),
                (random.nextDouble() * 0.02 - 0.01),
                (random.nextDouble() * 0.02 - 0.01)
        );
    }
}
