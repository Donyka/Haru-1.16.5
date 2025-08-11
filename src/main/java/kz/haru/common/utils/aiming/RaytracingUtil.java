package kz.haru.common.utils.aiming;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;

import java.util.function.Predicate;

public class RaytracingUtil implements IMinecraft {
    public static BlockRayTraceResult raycastForWall(double range, Rotation rotation, boolean includeFluids) {
        Entity entity = mc.getRenderViewEntity();

        if (entity == null) return null;

        Vector3d start = entity.getEyePosition(1.0f);
        Vector3d rotationVec = rotation.toVector();
        Vector3d end = start.add(rotationVec.scale(range));

        RayTraceContext.FluidMode fluidMode = includeFluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE;
        RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, fluidMode, entity);

        return mc.world.rayTraceBlocks(context);
    }

    public static EntityRayTraceResult raytraceEntity(double range, Rotation rotation, boolean ignoreWalls, Predicate<Entity> filter) {
        Entity entity = mc.getRenderViewEntity();

        if (entity == null) return null;

        Vector3d start = entity.getEyePosition(1.0f);
        Vector3d directionVec = rotation.toVector();
        Vector3d end = start.add(directionVec.scale(range));
        AxisAlignedBB box = entity.getBoundingBox().expand(directionVec.scale(range)).grow(1f);
        EntityRayTraceResult entityHit = ProjectileHelper.rayTraceEntities(entity, start, end, box, e -> !e.isSpectator() && filter.test(e), range * range);

        if (ignoreWalls) return entityHit;

        if (entityHit != null) {
            Vector3d entityPos = entityHit.getEntity().getPositionVec();
            double distanceToEntity = start.distanceTo(entityPos);
            BlockRayTraceResult blockHit = raycastForWall(distanceToEntity, rotation, false);

            if (blockHit != null && isBlockCloser(start, blockHit.getHitVec(), entityHit.getHitVec())) {
                return null;
            }
        }

        return entityHit;
    }

    private static boolean isBlockCloser(Vector3d start, Vector3d blockPos, Vector3d entityPos) {
        return start.squareDistanceTo(blockPos) < start.squareDistanceTo(entityPos);
    }
}