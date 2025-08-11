package kz.haru.common.utils.aiming.rotation;

import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import kz.haru.common.utils.math.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import static java.lang.Math.*;

public class LegendsGriefMode extends RotationMode {
    private float lastYawDelta = 0.0f;
    private float lastPitchDelta = 0.0f;
    private int lastPitchChangeDirection = 0;
    private int ticksSinceSwitchedDirection = 0;

    private float maxYawSpeed = 180.0f;
    private float maxPitchSpeed = 180.0f;

    public LegendsGriefMode() {
        super("Vulcan");
    }

    @Override
    public Rotation limitAngleChange(Rotation currentAngle, Rotation targetAngle, Vector3d vec3d, Entity entity) {
        Rotation angleDelta = MathUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.yaw;
        float pitchDelta = angleDelta.pitch;

        maxPitchSpeed = 60f;
        maxYawSpeed = 120f;

        if ((pitchDelta < 0.0f && this.lastPitchDelta > 0.0f) || (pitchDelta > 0.0f && this.lastPitchDelta < 0.0f)) {
            ticksSinceSwitchedDirection = 0;
        } else {
            ++ticksSinceSwitchedDirection;
        }

        boolean invalid = ticksSinceSwitchedDirection == 0 && Math.abs(pitchDelta) > 5.0f;
        if (invalid) {
            pitchDelta -= 1f;
            pitchDelta *= 0.3f;
            maxPitchSpeed *= 0.4f;
        }

        if (Math.abs(pitchDelta) < 0.05f) {
            pitchDelta -= (float) (Math.random() * 0.05f - 0.225f);
        }

        if (Math.abs(yawDelta - lastYawDelta) < 0.08f) {
            yawDelta -= (float) (Math.random() * 0.15f - 0.125f);
        }

        if (Math.abs(pitchDelta) < 0.01f) {
            pitchDelta -= (float) (Math.random() * 0.01f - 0.005f);
        }

        if (Math.abs(yawDelta) > 180.25f) {
            maxYawSpeed *= 0.8f;
        }

        if (Math.abs(yawDelta) > 15.0f && Math.abs(pitchDelta) < 0.1f) {
            maxYawSpeed *= 0.7f;
        }

        if (Math.abs(yawDelta) < 0.05f && Math.abs(pitchDelta) < 0.05f) {
            maxYawSpeed *= 1.1f;
            maxPitchSpeed *= 1.1f;
        }

        if (yawDelta > 1.25f && lastYawDelta > 1.25f) {
            yawDelta -= lastYawDelta;
            maxYawSpeed *= 3;
        }

        if (Math.abs(yawDelta) > 2.75f && Math.abs(pitchDelta) == 0.0f) {
            maxYawSpeed *= 0.8f;
            maxPitchSpeed *= 1.1f;
        }

        if (Math.abs(yawDelta) > 0.5f && Math.abs(pitchDelta) < 0.05f) {
            maxYawSpeed *= 0.7f;
            maxPitchSpeed *= 1.05f;
        }

        if (Math.abs(yawDelta) > 1.825f && Math.abs(pitchDelta) == 0.0f) {
            maxYawSpeed *= 0.6f;
            maxPitchSpeed *= 0.9f;
        }

        if (Math.abs(yawDelta) > 20.0f && Math.abs(pitchDelta) < 0.1f) {
            maxYawSpeed *= 0.5f;
            maxPitchSpeed *= 1.1f;
        }

        if (Math.abs(yawDelta) > 0.25f && Math.abs(pitchDelta) > 0.25f && Math.abs(pitchDelta) < 20.0f && Math.abs(yawDelta) < 20.0f) {
            maxYawSpeed *= 0.95f;
            maxPitchSpeed *= 0.85f;
        }

        if (Math.abs(yawDelta) > 0.1f && Math.abs(pitchDelta) > 0.1f && Math.abs(yawDelta) < 20.0f && Math.abs(pitchDelta) < 20.0f) {
            maxYawSpeed *= 0.9f;
            maxPitchSpeed *= 0.8f;
        }

        if (Math.abs(yawDelta) > 0.05f && Math.abs(pitchDelta) == 0.0f) {
            maxYawSpeed *= 0.8f;
            maxPitchSpeed *= 0.95f;
        }

        if (Math.abs(yawDelta) > 0.05f && Math.abs(pitchDelta) < 0.05f) {
            maxYawSpeed *= 0.85f;
            maxPitchSpeed *= 1.1f;
        }

        if (Math.abs(yawDelta) > 0.75f && Math.abs(pitchDelta) > 0.75f) {
            maxYawSpeed *= 0.8f;
            maxPitchSpeed *= 0.75f;
        }

        if (Math.abs(yawDelta) > 0.03f && Math.abs(pitchDelta) > 0.03f) {
            maxYawSpeed *= 0.9f;
            maxPitchSpeed *= 0.8f;
        }

        int currentPitchChangeDirection = pitchDelta > 0 ? 1 : -1;
        if (lastPitchChangeDirection != 0 && currentPitchChangeDirection != lastPitchChangeDirection) {
            maxPitchSpeed *= 0.2f;
        }
        lastPitchChangeDirection = currentPitchChangeDirection;

        yawDelta = min(max(yawDelta, -maxYawSpeed), maxYawSpeed);
        pitchDelta = min(max(pitchDelta, -maxPitchSpeed), maxPitchSpeed);

        lastYawDelta = yawDelta;
        lastPitchDelta = pitchDelta;

        maxYawSpeed = 0f;
        maxPitchSpeed = 0f;

        return new Rotation(
                currentAngle.yaw + yawDelta,
                currentAngle.pitch + pitchDelta
        );
    }

    @Override
    public Vector3d randomValue() {
        return new Vector3d(0.0, 0.0, 0.0);
    }
}