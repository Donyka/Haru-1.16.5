package kz.haru.implement.modules.combat;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.client.Haru;
import kz.haru.common.utils.aiming.RotationUtil;
import kz.haru.common.utils.aiming.rotation.*;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.controller.RotationConfig;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import kz.haru.common.utils.aiming.rotation.builder.RotationMode;
import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.player.attacking.AttackPerpetrator;
import kz.haru.common.utils.player.attacking.SprintManager;
import kz.haru.common.utils.player.attacking.TargetManager;
import kz.haru.common.utils.player.world.PlayerUtil;
import kz.haru.common.utils.task.TaskPriority;
import kz.haru.implement.events.player.updates.PostRotationMovementInputEvent;
import kz.haru.implement.events.player.updates.UpdateEvent;
import kz.haru.implement.modules.movement.MoveFixModule;
import kz.haru.implement.modules.movement.SprintModule;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "Kill Aura", category = Category.COMBAT, desc = "Бьёт меня")
public class KillAuraModule extends Module {
    private final TargetManager targetManager = new TargetManager();
    private final AttackPerpetrator attackPerpetrator = new AttackPerpetrator();

    public final ModeSetting aimMode = new ModeSetting("Aim mode").value("Smooth").addValues("Smooth", "Fixed", "Grim", "Legends Grief", "Spooky Time");
    public final FloatSetting distance = new FloatSetting("Distance").value(3f).range(2.5f, 6f).step(0.1f);
    private final FloatSetting preDistance = new FloatSetting("Pre distance").value(0.3f).range(0f, 3f).step(0.1f);
    private final MultiModeSetting targets = new MultiModeSetting("Targets").addValues("Players", "Mobs", "Animals", "Friends").select("Players");
    private final MultiModeSetting options = new MultiModeSetting("Options").select("Only crits", "Raytrace").addValues("Only crits", "Raytrace", "Break shield", "Un press shield", "Ignore walls");
    private final BooleanSetting smartCrits = new BooleanSetting("Smart crits").value(false).setVisible(() -> options.is("Only crits"));
    private final BooleanSetting sprintReset = new BooleanSetting("Sprint Reset").value(false).setVisible(() -> options.is("Only crits"));
    private final BooleanSetting elytraOverride = new BooleanSetting("Elytra override").value(false);
    private final FloatSetting elytraDistance = new FloatSetting("Elytra distance").value(4f).range(2.5f, 6f).step(0.1f).setVisible(elytraOverride::getValue);
    private final FloatSetting elytraPreDistance = new FloatSetting("Elytra pre distance").value(16f).range(0f, 32f).step(0.1f).setVisible(elytraOverride::getValue);

    public LivingEntity target;
    private Vector3d rotationTarget;
    private long lastRotationUpdateTime;
    private boolean waitingForTarget = false;

    public KillAuraModule() {
        setup(aimMode, distance, preDistance, targets, options, smartCrits,sprintReset,
        elytraOverride, elytraDistance, elytraPreDistance);
    }

    public static KillAuraModule get() {
        return Haru.getInstance().getModuleManager().get(KillAuraModule.class);
    }

    private float getPreDistance() {
        return (mc.player.isElytraFlying() && elytraOverride.getValue()) ? elytraPreDistance.getValue() : preDistance.getValue();
    }

    public float getAttackDistance() {
        return (mc.player.isElytraFlying() && elytraOverride.getValue()) ? elytraDistance.getValue() : distance.getValue();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        target = updateTarget();
        waitingForTarget = false;
    }

    @Override
    public void onDisable() {
        targetManager.releaseTarget();
        target = null;
        rotationTarget = null;
        waitingForTarget = false;
        ElytraTargetModule elytraTargetModule = ElytraTargetModule.get();
        if (elytraTargetModule.isRotatingWithKillAura()) {
            elytraTargetModule.stopRotatingWithKillAura();
        }
        SprintModule.get().setCanSprint(true);
        super.onDisable();
    }

    @EventTarget
    public void onPostRotationMovementInput(PostRotationMovementInputEvent event) {
        if (target != null) {
            ElytraTargetModule elytraTargetModule = ElytraTargetModule.get();
            Vector3d attackVector = getTargetVector(target);
            Rotation rotation = MathUtil.fromVec3d(attackVector.subtract(mc.player.getEyePosition(1.0f)));

            if (useElytraTargetRotations()) {
                Rotation.VecRotation sex = elytraTargetModule.getRotationForKillAura(target);

                rotation = sex.rotation();
                attackVector = sex.vec();
            }

            rotateToTarget(target, attackVector, rotation);
        }
    }

    private boolean useElytraTargetRotations() {
        ElytraTargetModule elytraTargetModule = ElytraTargetModule.get();
        return elytraTargetModule.isEnabled() &&
               mc.player.isElytraFlying() &&
               !mc.player.abilities.isFlying &&
               target != null;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        target = updateTarget();

        if (target != null) {
            attackTarget(target);
        }
    }

    private LivingEntity updateTarget() {
        TargetManager.EntityFilter filter = new TargetManager.EntityFilter(targets.getValue());
        targetManager.searchTargets(mc.world.getAllEntities(), getAttackDistance() + getPreDistance());
        targetManager.validateTarget(filter::isValid);
        return targetManager.getCurrentTarget();
    }

    private void attackTarget(LivingEntity target) {
        AttackPerpetrator.AttackPerpetratorConfigurable configurable = new AttackPerpetrator.AttackPerpetratorConfigurable(
                target,
                RotationController.getInstance().getServerRotation(),
                distance.getValue(),
                options.getValue(),
                smartCrits.getValue(),
                options.is("Ignore walls"),
                getSprintMode()
        );

        if (PlayerUtil.isStoppedByModule(getName())) return;

        if (getTargetVector(target).distanceTo(mc.player.getEyePosition(1.0f)) > getAttackDistance()) return;

        attackPerpetrator.performAttack(configurable);
    }

    private void rotateToTarget(LivingEntity target, Vector3d targetVec, Rotation rotation) {
        RotationConfig configurable = new RotationConfig(getSmoothMode(), false, MoveFixModule.get().isEnabled());

        if (PlayerUtil.isStoppedByModule(getName())) return;
        

        RotationController.getInstance().rotateTo(new Rotation.VecRotation(rotation, targetVec), target, configurable, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    private SprintManager.Mode getSprintMode() {
        if (sprintReset.getValue()) {
            return SprintManager.Mode.LEGIT;
        } else {
            return SprintManager.Mode.DEFAULT;
        }
    }

    private RotationMode getSmoothMode() {
        return switch (aimMode.getValue()) {
            case "Legends Grief" -> new LegendsGriefMode();
            case "Fixed" -> new FixedMode();
            case "Spooky Time" -> new SpookyTimeMode();
            case "Grim" -> new GrimSmoothMode();
            default -> new SmoothMode();
        };
    }

    private Vector3d getTargetVector(LivingEntity target) {
        if (target == null) return Vector3d.ZERO;

        ElytraTargetModule elytraTargetModule = ElytraTargetModule.get();
        if (useElytraTargetRotations()) {
            Vector3d rotationTarget = elytraTargetModule.getLastRotationTarget();
            if (rotationTarget != null) {
                return rotationTarget;
            }
            
            Vector3d optimalPoint = elytraTargetModule.getOptimalAttackPoint(target);
            if (optimalPoint != null) {
                return optimalPoint;
            }
        }

        return getDefaultTargetVector();
    }

    private Vector3d getDefaultTargetVector() {
        return RotationUtil.getSpot(target);
    }
}