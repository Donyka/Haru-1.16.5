package kz.haru.common.utils.math;

import kz.haru.common.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class ProjectionUtil implements IMinecraft {
    public static Vector2f project(Vector3d vec) {
        return project(vec.x, vec.y, vec.z);
    }

    public static Vector2f project(double x, double y, double z) {
        Vector3d cameraPos = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraOrientation = mc.getRenderManager().getCameraOrientation().copy();
        cameraOrientation.conjugate();

        Vector3f vector3f = new Vector3f((float) (cameraPos.x - x), (float) (cameraPos.y - y), (float) (cameraPos.z - z));
        vector3f.transform(cameraOrientation);

        if (mc.gameSettings.viewBobbing) {
            if (mc.getRenderViewEntity() instanceof PlayerEntity player) {
                calculateViewBobbing(player, vector3f);
            }
        }

        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return calculateScreenPosition(vector3f, fov);
    }

    private static void calculateViewBobbing(PlayerEntity playerentity, Vector3f result3f) {
        float walked = playerentity.distanceWalkedModified;
        float f = walked - playerentity.prevDistanceWalkedModified;
        float f1 = -(walked + f * mc.getRenderPartialTicks());
        float f2 = MathHelper.lerp(mc.getRenderPartialTicks(), playerentity.prevCameraYaw, playerentity.cameraYaw);

        Quaternion quaternion = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
        quaternion.conjugate();
        result3f.transform(quaternion);

        Quaternion quaternion1 = new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Vector3f bobTranslation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F), (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
        bobTranslation.setY(-bobTranslation.getY());
        result3f.add(bobTranslation);
    }

    private static Vector2f calculateScreenPosition(Vector3f result3f, double fov) {
        float halfHeight = mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (result3f.getZ() * (float) Math.tan(Math.toRadians(fov / 2.0F)));
        if (result3f.getZ() < 0.0F) {
            return new Vector2f(-result3f.getX() * scaleFactor + mc.getMainWindow().getScaledWidth() / 2.0F, mc.getMainWindow().getScaledHeight() / 2.0F - result3f.getY() * scaleFactor);
        }
        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public static Vector3d interpolate(Entity entity, float partialTicks) {
        double posX = MathUtil.lerp(entity.lastTickPosX, entity.getPosX(), partialTicks);
        double posY = MathUtil.lerp(entity.lastTickPosY, entity.getPosY(), partialTicks);
        double posZ = MathUtil.lerp(entity.lastTickPosZ, entity.getPosZ(), partialTicks);
        return new Vector3d(posX, posY, posZ);
    }
}
