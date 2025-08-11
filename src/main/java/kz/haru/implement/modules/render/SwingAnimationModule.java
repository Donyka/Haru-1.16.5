package kz.haru.implement.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

@ModuleRegister(name = "Swing Animation", category = Category.RENDER, desc = "Драчит балду")
public class SwingAnimationModule extends Module {
    public final ModeSetting mode = new ModeSetting("Mode").value("Smooth").addValues("Back", "Smooth Down", "Block", "Smooth");
    public final FloatSetting power = new FloatSetting("Power").value(4f).range(1f, 10f).step(0.1f);
    public final FloatSetting speed = new FloatSetting("Speed").value(8f).range(1f, 10f).step(0.1f);
    public final FloatSetting scale = new FloatSetting("Scale").value(1f).range(0.1f, 2f).step(0.1f);

    public SwingAnimationModule() {
        setup(mode, power, speed, scale);
    }

    public static SwingAnimationModule get() {
        return get(SwingAnimationModule.class);
    }

    public void animationProcess(MatrixStack stack, float swingProgress, Runnable runnable) {
        float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);
        float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

        switch (mode.getValue()) {
            case "Swipe Back" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0.1f, -0.1);
                stack.rotate(Vector3f.YP.rotationDegrees(60));
                stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                stack.rotate(Vector3f.YP.rotationDegrees((sin2 * sin1) * -5));
                stack.rotate(Vector3f.XP.rotationDegrees(-10 - (power.getValue() * 10) * anim));
                stack.rotate(Vector3f.XP.rotationDegrees(-60));
            }

            case "Back" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.4f, 0.1f, -0.5);
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - (power.getValue() * 10) * anim));
            }

            case "Smooth Down" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.0, 0, 0);
                stack.rotate(Vector3f.YP.rotationDegrees(15 * anim));

                stack.rotate(Vector3f.ZP.rotationDegrees(-60 * anim));
                stack.rotate(Vector3f.XP.rotationDegrees((-45 - (power.getValue())) * anim));
            }

            case "Block" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.4f, 0, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                stack.rotate(Vector3f.ZP.rotationDegrees(-30));
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - (power.getValue() * 10) * anim));
            }
            default -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                runnable.run();
            }
        }
    }
}
