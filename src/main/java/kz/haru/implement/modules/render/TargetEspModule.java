package kz.haru.implement.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.client.ClientInfo;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.math.TimerUtil;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.implement.events.player.updates.UpdateEvent;
import kz.haru.implement.events.render.Render3DEvent;
import kz.haru.implement.modules.combat.KillAuraModule;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

@ModuleRegister(name = "Target Esp", category = Category.RENDER, desc = "Ну это же секс!")
public class TargetEspModule extends Module {
    private final AnimationUtil animation = new AnimationUtil(Easing.EXPO_OUT, 500);
    private static final AnimationUtil rotateAnimation = new AnimationUtil(Easing.QUINT_OUT, 500);
    private LivingEntity entity;

    private static float prevAnimationValue = 0.0f;
    private static float currentTargetAnimation = 0.0f;
    private static final TimerUtil timerUtil = new TimerUtil();
    private static float prevRotate = 0f;
    private static float rotate = 0f;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        KillAuraModule aura = KillAuraModule.get();
        LivingEntity auraTarget = aura.target;

        if (auraTarget != null) {
            entity = auraTarget;
        }

        boolean reason = aura.isEnabled() && auraTarget != null;

        animation.run(reason ? 1.0 : 0.0);

        prevAnimationValue = currentTargetAnimation;
        currentTargetAnimation = (float) animation.getValue();

        long dur = 2500;
        long dur2 = 600;
        float rotateAnim = (float) rotateAnimation.getValue();
        rotateAnimation.run(timerUtil.hasReached(dur) ? 1.0 : 0.0);;

        if (timerUtil.hasReached(dur + dur2 * 2)) {
            timerUtil.reset();
        }

        prevRotate = rotate;
        rotate += 10f + (25f * rotateAnim);
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (currentTargetAnimation > 0) {
            renderTextureEsp(entity);
        }
    }

    public static void renderTextureEsp(Entity entity) {
        if (entity == null) return;

        ActiveRenderInfo camera = mc.gameRenderer.getActiveRenderInfo();
        float sexSize = 0.4f * (1f - (float) rotateAnimation.getValue());

        double s = MathUtil.interpolate(entity.prevPosX, entity.getPosX()) - camera.getProjectedView().x;
        double e = MathUtil.interpolate(entity.prevPosY, entity.getPosY()) - camera.getProjectedView().y;
        double x = MathUtil.interpolate(entity.prevPosZ, entity.getPosZ()) - camera.getProjectedView().z;

        float nyaa = (float) MathUtil.interpolate(prevAnimationValue, currentTargetAnimation) + sexSize;

        MatrixStack matrices = new MatrixStack();
        matrices.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));
        matrices.rotate(Vector3f.YP.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(s, (e + entity.getHeight() / 2), x);
        matrices.rotate(Vector3f.YP.rotationDegrees(-camera.getYaw()));
        matrices.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));
        matrices.rotate(Vector3f.ZP.rotationDegrees((float) (-MathUtil.interpolate(prevRotate, rotate))));

        ResourceLocation texture = new ResourceLocation(ClientInfo.clientName.toLowerCase() + "/images/target/target_esp.png");

        int alpha = (int) (255 * currentTargetAnimation);
        int colorTopLeft = ColorUtil.setAlpha(ColorUtil.getClientColor(90), alpha);
        int colorTopRight = ColorUtil.setAlpha(ColorUtil.getClientColor(180), alpha);
        int colorBottomLeft = ColorUtil.setAlpha(ColorUtil.getClientColor(270), alpha);
        int colorBottomRight = ColorUtil.setAlpha(ColorUtil.getClientColor(360), alpha);

        RenderUtil.drawTexture(matrices, texture, nyaa, nyaa, colorTopLeft, colorTopRight, colorBottomLeft, colorBottomRight);
    }

}
