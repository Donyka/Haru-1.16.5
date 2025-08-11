package kz.haru.common.utils.player.movement;

import kz.haru.common.interfaces.IMinecraft;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MoveUtil implements IMinecraft {
    public static boolean isMoving() {
        return mc.player.moveForward != 0f || mc.player.moveStrafing != 0f;
    }

    public static boolean isAboveWater() {
        PlayerEntity player = mc.player;
        World world = mc.world;
        if (player == null || world == null) return false;

        return player.isInWater() || world.getBlockState(player.getPosition().down()).getBlock() == Blocks.WATER;
    }

    public static boolean isInWeb() {
        PlayerEntity player = mc.player;
        if (player == null) return false;

        AxisAlignedBB playerBox = player.getBoundingBox();
        BlockPos playerPosition = player.getPosition();

        return getNearbyBlockPositions(playerPosition).stream().anyMatch(pos -> isBlockCobweb(playerBox, pos));
    }

    private static boolean isBlockCobweb(AxisAlignedBB playerBox, BlockPos blockPos) {
        AxisAlignedBB blockBox = new AxisAlignedBB(blockPos);
        return playerBox.intersects(blockBox) && mc.world.getBlockState(blockPos).getBlock() == Blocks.COBWEB;
    }

    private static List<BlockPos> getNearbyBlockPositions(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = center.getX() - 2; x <= center.getX() + 2; x++) {
            for (int y = center.getY() - 1; y <= center.getY() + 4; y++) {
                for (int z = center.getZ() - 2; z <= center.getZ() + 2; z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return positions;
    }

    public static void setMotion(double motion, ClientPlayerEntity player) {
        double forward = player.movementInput.moveForward;
        double strafe = player.movementInput.moveStrafe;

        float yaw = player.rotationYaw;

        if (forward == 0 && strafe == 0) {
            player.motion.x = 0;
            player.motion.z = 0;
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            player.motion.x = forward * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f)) + strafe * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f));
            player.motion.z = forward * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f)) - strafe * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f));
        }
    }
}
