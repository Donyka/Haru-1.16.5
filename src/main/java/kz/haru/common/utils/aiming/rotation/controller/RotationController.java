package kz.haru.common.utils.aiming.rotation.controller;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Module;
import kz.haru.client.Haru;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.task.TaskPriority;
import kz.haru.common.utils.task.TaskProcessor;
import kz.haru.implement.events.connection.HPacketEvent;
import kz.haru.implement.events.player.movement.MovementInputEvent;
import kz.haru.implement.events.player.updates.PostRotationMovementInputEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.abs;

@Getter
@Setter
public class RotationController implements IMinecraft {
    @Getter
    private static RotationController instance;

    private RotationPlan lastRotationPlan;
    private final TaskProcessor<RotationPlan> rotationPlanTaskProcessor = new TaskProcessor<>();
    private Rotation currentRotation;
    private Rotation previousRotation;
    private Rotation serverRotation = Rotation.DEFAULT;

    public RotationController() {
        instance = this;
        Haru.getInstance().getEventManager().register(this);
    }

    private void setRotation(Rotation value) {
        previousRotation = (value == null) ? (currentRotation != null ? currentRotation : mc.player != null ? new Rotation(mc.player.rotationYaw, mc.player.rotationPitch) : Rotation.DEFAULT) : currentRotation;
        currentRotation = value;
    }

    public Rotation getRotation() {
        if (mc.player == null) return Rotation.DEFAULT;
        return currentRotation != null ? currentRotation : MathUtil.fromVec2f(new Vector2f(mc.player.rotationPitch, mc.player.rotationYaw));
    }

    public Rotation getPreviousRotation() {
        if (mc.player == null) return Rotation.DEFAULT;
        return previousRotation != null ? previousRotation : MathUtil.fromVec2f(new Vector2f(mc.player.rotationPitch, mc.player.rotationYaw));
    }

    public RotationPlan getCurrentRotationPlan() {
        return rotationPlanTaskProcessor.fetchActiveTaskValue() != null ? rotationPlanTaskProcessor.fetchActiveTaskValue() : lastRotationPlan;
    }

    public void rotateTo(Rotation.VecRotation vecRotation, LivingEntity entity, RotationConfig configurable, TaskPriority taskPriority, Module provider) {
        rotateTo(configurable.createRotationPlan(vecRotation.rotation(), vecRotation.vec(), entity), taskPriority, provider);
    }

    public void rotateTo(Rotation rotation, RotationConfig configurable, TaskPriority taskPriority, Module provider) {
        rotateTo(configurable.createRotationPlan(rotation), taskPriority, provider);
    }

    private void rotateTo(RotationPlan plan, TaskPriority taskPriority, Module provider) {
        rotationPlanTaskProcessor.addTask(new TaskProcessor.Task<>(plan.getTicksUntilReset(), taskPriority.getPriority(), provider, plan));
    }

    private void update() {
        RotationPlan activePlan = getCurrentRotationPlan();
        if (activePlan == null) {
            return;
        }

        Rotation playerRotation = MathUtil.fromVec2f(new Vector2f(mc.player.rotationPitch, mc.player.rotationYaw));

        if (rotationPlanTaskProcessor.fetchActiveTaskValue() == null) {
            double differenceFromCurrentToPlayer = computeRotationDifference(serverRotation, playerRotation);
            if (differenceFromCurrentToPlayer < activePlan.getResetThreshold()) {
                if (currentRotation != null) {
                    mc.player.rotationYaw = currentRotation.yaw + computeAngleDifference(mc.player.rotationYaw, currentRotation.yaw);
                    mc.player.rotationPitch = currentRotation.pitch + computeAngleDifference(mc.player.rotationPitch, currentRotation.pitch);
                }
                setRotation(null);
                lastRotationPlan = null;
                return;
            }
        }

        Rotation newRotation = activePlan.nextRotation(currentRotation != null ? currentRotation : playerRotation, rotationPlanTaskProcessor.fetchActiveTaskValue() == null).adjustSensitivity();
        setRotation(newRotation);

        lastRotationPlan = activePlan;

        rotationPlanTaskProcessor.tick(1);
    }

    private double computeRotationDifference(Rotation a, Rotation b) {
        return hypot(abs(computeAngleDifference(a.yaw, b.yaw)), abs((a.pitch - b.pitch)));
    }

    private float computeAngleDifference(float a, float b) {
        return MathHelper.wrapDegrees(a - b);
    }

    @EventTarget
    public void onMovementInput(MovementInputEvent event) {
        Haru.getInstance().getEventManager().callEvent(new PostRotationMovementInputEvent());
        update();
    }

    @EventTarget
    public void onPacketSend(HPacketEvent event) {
        if (event.isSend()) {
            Rotation rotation;
            if (event.getPacket() instanceof CPlayerPacket.RotationPacket packet) {
                rotation = new Rotation(packet.getYaw(1f), packet.getPitch(1f));
            } else if (event.getPacket() instanceof CPlayerPacket.PositionRotationPacket packet) {
                rotation = new Rotation(packet.getYaw(1f), packet.getPitch(1f));
            } else if (event.getPacket() instanceof SPlayerPositionLookPacket packet) {
                rotation = new Rotation(packet.getYaw(), packet.getPitch());
            } else {
                return;
            }

            if (!event.isCancelled()) {
                serverRotation = rotation;
            }
        }
    }
}
