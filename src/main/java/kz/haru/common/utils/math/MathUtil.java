package kz.haru.common.utils.math;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.Math.hypot;
import static java.lang.Math.toDegrees;
import static net.minecraft.util.math.MathHelper.atan2;

public class MathUtil implements IMinecraft {
    public static double deltaTime() {
        return Minecraft.debugFPS > 0 ? (1.0000 / Minecraft.debugFPS) : 1;
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static double interpolate(double old, double current) {
        return old + (current - old) * mc.getRenderPartialTicks();
    }

    public static Rotation fromVec2f(Vector2f vector2f) {
        return new Rotation(vector2f.y, vector2f.x);
    }

    public static Rotation fromVec3d(Vector3d vector) {
        float yaw = MathHelper.wrapDegrees((float) toDegrees(atan2(vector.z, vector.x)) - 90);
        float pitch = MathHelper.wrapDegrees((float) toDegrees(-atan2(vector.y, hypot(vector.x, vector.z))));
        return new Rotation(yaw, pitch);
    }

    public static Rotation calculateDelta(Rotation start, Rotation end) {
        float deltaYaw = MathHelper.wrapDegrees(end.yaw - start.yaw);
        float deltaPitch = MathHelper.wrapDegrees(end.pitch - start.pitch);
        return new Rotation(deltaYaw, deltaPitch);
    }

    public static float animation(float endPoint, float current, float speed) {
        return current + (endPoint - current) * speed;
    }

    public static float swing(float min, float max, long duration) {
        if (min == max) return 0.0f;

        double time = System.currentTimeMillis();
        double progress = (Math.sin(2 * Math.PI * time / duration) + 1) / 2;
        return min + (max - min) * (float)progress;
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - MathHelper.clamp((float) (deltaTime() * multiple), 0, 1)) * end
                + MathHelper.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }

    public static double lerp(double end, double start, double multiple) {
        return (end + (start - end) * MathHelper.clamp(deltaTime() * multiple, 0, 1));
    }

    public static double round(double sex, double step) {
        double v = (Math.round(sex / step) * step);
        return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
