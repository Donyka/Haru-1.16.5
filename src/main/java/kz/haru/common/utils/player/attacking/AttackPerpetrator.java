package kz.haru.common.utils.player.attacking;

import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class AttackPerpetrator {
    public final AttackHandler attackHandler = new AttackHandler();

    public void performAttack(AttackPerpetratorConfigurable configurable) {
        attackHandler.sprintManager.setCurrentMode(configurable.mode);
        attackHandler.sprintManager.setShouldUseLegitSprintReset(attackHandler.clickScheduler.isOneTickBeforeAttack());
        attackHandler.handleAttack(configurable);
    }

    public static class AttackPerpetratorConfigurable {
        public final LivingEntity target;
        public final Rotation rotation;
        public final float distance;
        public final boolean raytraceEnabled;
        public final boolean onlyCrits;
        public final boolean breakShield;
        public final boolean onlySpaceCrits;
        public final boolean unPressShield;
        public final boolean ignoreWalls;
        public final SprintManager.Mode mode;

        public AttackPerpetratorConfigurable(
                LivingEntity target,
                Rotation rotation,
                float distance,
                boolean raytraceEnabled,
                boolean onlyCrits,
                boolean breakShield,
                boolean onlySpaceCrits,
                boolean unPressShield,
                boolean ignoreWalls,
                SprintManager.Mode mode
        ) {
            this.target = target;
            this.rotation = rotation;
            this.distance = distance;
            this.raytraceEnabled = raytraceEnabled;
            this.onlyCrits = onlyCrits;
            this.breakShield = breakShield;
            this.unPressShield = unPressShield;
            this.onlySpaceCrits = onlySpaceCrits;
            this.ignoreWalls = ignoreWalls;
            this.mode = mode;
        }

        public AttackPerpetratorConfigurable(
                LivingEntity target,
                Rotation rotation,
                float distance,
                List<String> options,
                boolean onlySpaceCrits,
                boolean ignoreWalls,
                SprintManager.Mode mode
        ) {
            this(
                    target,
                    rotation,
                    distance,
                    options.contains("Raytrace"),
                    options.contains("Only crits"),
                    options.contains("Break shield"),
                    onlySpaceCrits,
                    options.contains("Un press shield"),
                    options.contains("Ignore walls"),
                    mode
            );
        }
    }
}
