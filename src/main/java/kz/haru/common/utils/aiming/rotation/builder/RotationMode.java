package kz.haru.common.utils.aiming.rotation.builder;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public abstract class RotationMode implements IMinecraft {
    private final String name;

    public RotationMode(String name) {
        this.name = name;
    }

    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation) {
        return limitAngleChange(currentRotation, targetRotation, null, null);
    }

    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d) {
        return limitAngleChange(currentRotation, targetRotation, vec3d, null);
    }

    public abstract Rotation limitAngleChange(
            Rotation currentRotation,
            Rotation targetRotation,
            Vector3d vec3d,
            Entity entity
    );

    public abstract Vector3d randomValue();
}