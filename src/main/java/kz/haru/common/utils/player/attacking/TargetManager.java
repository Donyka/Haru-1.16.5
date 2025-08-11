package kz.haru.common.utils.player.attacking;

import kz.haru.api.system.friends.FriendManager;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.aiming.RotationUtil;
import kz.haru.implement.modules.player.FreeCameraModule;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
public class TargetManager implements IMinecraft {
    private LivingEntity currentTarget;
    private Stream<LivingEntity> potentialTargets;

    public void lockTarget(LivingEntity target) {
        if (currentTarget == null) {
            currentTarget = target;
        }
    }

    public void releaseTarget() {
        currentTarget = null;
    }

    public void validateTarget(Predicate<LivingEntity> predicate) {
        findFirstMatch(predicate).ifPresent(this::lockTarget);

        if (currentTarget != null && !predicate.test(currentTarget)) {
            releaseTarget();
        }
    }

    public void searchTargets(Iterable<Entity> entities, float maxDistance) {
        if (isTargetOutOfRange(maxDistance)) {
            releaseTarget();
        }

        potentialTargets = createStreamFromEntities(entities, maxDistance);
    }

    private boolean isTargetOutOfRange(float maxDistance) {
        return currentTarget != null && RotationUtil.getSpot(currentTarget).distanceTo(mc.player.getEyePosition(1.0f)) > maxDistance;
    }

    private Stream<LivingEntity> createStreamFromEntities(Iterable<Entity> entities, float maxDistance) {
        return StreamSupport.stream(entities.spliterator(), false)
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> {
                    Vector3d spot = RotationUtil.getSpot(entity);
                    return mc.player.getEyePosition(1.0f).distanceTo(spot) <= maxDistance;
                })
                .sorted((entity1, entity2) -> {
                    Vector3d spot1 = RotationUtil.getSpot(entity1);
                    Vector3d spot2 = RotationUtil.getSpot(entity2);
                    return Double.compare(
                            mc.player.getEyePosition(1.0f).distanceTo(spot1),
                            mc.player.getEyePosition(1.0f).distanceTo(spot2)
                    );
                });
    }

    private Optional<LivingEntity> findFirstMatch(Predicate<LivingEntity> predicate) {
        return potentialTargets.filter(predicate).findFirst();
    }

    public static class EntityFilter implements IMinecraft {
        private final List<String> targetSettings;

        public EntityFilter(List<String> targetSettings) {
            this.targetSettings = targetSettings;
        }

        public boolean isValid(LivingEntity entity) {
            if (isLocalPlayer(entity)) return false;
            if (isInvalidHealth(entity)) return false;
            if (isFakePlayer(entity)) return false;
            if (isFriendPlayer(entity.getName().getString())) return false;

            return isValidEntityType(entity);
        }

        private boolean isFriendPlayer(String name) {
            return FriendManager.isFriend(name);
        }

        private boolean isLocalPlayer(LivingEntity entity) {
            return entity == mc.player;
        }

        private boolean isInvalidHealth(LivingEntity entity) {
            return !entity.isAlive() || entity.getHealth() <= 0;
        }

        private boolean isFakePlayer(Entity entity) {
            if (entity == null) {
                return false;
            }
            
            return entity == FreeCameraModule.get().fakePlayer;
        }

        private boolean isValidEntityType(LivingEntity entity) {
            if (entity instanceof PlayerEntity) {
                return targetSettings.contains("Players");
            } else if (entity instanceof AnimalEntity) {
                return targetSettings.contains("Animals");
            } else if (entity instanceof MobEntity) {
                return targetSettings.contains("Mobs");
            } else if (isFriendPlayer(entity.getName().getString())) {
                return targetSettings.contains("Friends");
            }
            return false;
        }
    }
}
