package kz.haru.common.utils.aiming.rotation.controller;

import kz.haru.common.utils.math.MouseUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class Rotation {
    public float yaw;
    public float pitch;

    public static final Rotation DEFAULT = new Rotation(0f, 0f);

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation adjustSensitivity() {
        double gcd = MouseUtil.getGCD();
        Rotation previousRotation = RotationController.getInstance().getServerRotation();

        float adjustedYaw = adjustAxis(yaw, previousRotation.yaw, gcd);
        float adjustedPitch = adjustAxis(pitch, previousRotation.pitch, gcd);

        return new Rotation(adjustedYaw, MathHelper.clamp(adjustedPitch, -90f, 90f));
    }

    private float adjustAxis(float axisValue, float previousValue, double gcd) {
        float delta = axisValue - previousValue;
        return previousValue + Math.round(delta / gcd) * (float) gcd;
    }

    public Vector3d toVector() {
        float f = pitch * 0.017453292f;
        float g = -yaw * 0.017453292f;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vector3d(i * j, -k, h * j);
    }

    @Override
    public String toString() {
        return "Angle(yaw=" + yaw + ", pitch=" + pitch + ")";
    }

    public record VecRotation(Rotation rotation, Vector3d vec) {
        @Override
        public String toString() {
            return "VecRotation(angle=" + rotation + ", vec=" + vec + ")";
        }
    }
}
