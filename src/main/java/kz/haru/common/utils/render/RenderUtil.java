package kz.haru.common.utils.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.interfaces.IAccess;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.common.utils.shader.ShaderUtil;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

public class RenderUtil implements IMinecraft, IAccess {
    private final static ShaderUtil roundShader = ShaderUtil.getRoundShader();
    private final static ShaderUtil outlineShader = ShaderUtil.getOutlineShader();
    private final static ShaderUtil roundTextShader = ShaderUtil.getRoundTextureShader();

    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, 0f);
        GlStateManager.scalef(scale, scale, 1f);
        GlStateManager.translatef(-x, -y, 0f);
    }

    public static void scaleStop() {
        GlStateManager.popMatrix();
    }

    public static void drawElementClientRect(float x, float y, float width, float height, float alpha, String text) {
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float smooth = 10f * scale;
        //drawRound(x - smooth / 2f, y - smooth / 2f, width + smooth, height + smooth, smooth / 1.3f, smooth, ColorUtil.rgb(15, 15, 15, (int) (120 * alpha)));
        drawRound(x, y, width, height, 2f * scale, ColorUtil.rgb(15, 15, 15, (int) (200 * alpha)));
    }

    public static void drawElementClientRect(float x, float y, float width, float height, float alpha, float radius) {
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float smooth = 10f * scale;
        //drawRound(x - smooth / 2f, y - smooth / 2f, width + smooth, height + smooth, radius * scale + smooth / 1.3f, smooth, ColorUtil.rgb(15, 15, 15, (int) (120 * alpha)));
        drawRound(x, y, width, height, radius * scale, ColorUtil.rgb(15, 15, 15, (int) (200 * alpha)));
    }

    public static void drawRound(float x, float y, float width, float height, float radius, int color) {
        drawRound(x, y, width, height, new Vector4f(radius), 1f, new Vector4i(color));
    }

    public static void drawRound(float x, float y, float width, float height, Vector4f radius, int color) {
        drawRound(x, y, width, height, radius, 1f, new Vector4i(color));
    }

    public static void drawRound(float x, float y, float width, float height, float radius, float smoothness, int color) {
        drawRound(x, y, width, height, new Vector4f(radius), smoothness, new Vector4i(color));
    }

    public static void drawRound(float x, float y, float width, float height, Vector4f radius, float smoothness, int color) {
        drawRound(x, y, width, height, radius, smoothness, new Vector4i(color));
    }

    public static void drawRound(float x, float y, float width, float height, float radius, int color, float smooth) {
        drawRound(x, y, width, height, new Vector4f(radius), smooth, new Vector4i(color));
    }

    public static void drawRound(float x, float y, float width, float height, Vector4f radius, Vector4i color) {
        drawRound(x, y, width, height, radius, 0.05f, color);
    }


    public static void drawRound(float x, float y, float width, float height, Vector4f radius, float smoothness, Vector4i color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        float smoothOffset = (smoothness - 1f) * 0.01f;
        float adjustedX = x + smoothOffset;
        float adjustedY = y + smoothOffset;
        float adjustedWidth = width - smoothOffset * 2f;
        float adjustedHeight = height - smoothOffset * 2f;

        roundShader.init();

        roundShader.setUniformf("location", (float) (adjustedX * mc.getMainWindow().getGuiScaleFactor()), (float) ((mc.getMainWindow().getHeight() - (adjustedHeight * mc.getMainWindow().getGuiScaleFactor())) - (adjustedY * mc.getMainWindow().getGuiScaleFactor())));
        roundShader.setUniformf("rectSize", adjustedWidth * mc.getMainWindow().getGuiScaleFactor(), adjustedHeight * mc.getMainWindow().getGuiScaleFactor());
        roundShader.setUniformf("radius", radius.getX() * 2, radius.getY() * 2, radius.getZ() * 2, radius.getW() * 2);
        roundShader.setUniformf("smoothness", smoothness);

        roundShader.setUniform("color1", ColorUtil.rgba(color.getX()));
        roundShader.setUniform("color2", ColorUtil.rgba(color.getY()));
        roundShader.setUniform("color3", ColorUtil.rgba(color.getZ()));
        roundShader.setUniform("color4", ColorUtil.rgba(color.getW()));
        ShaderUtil.drawQuads(adjustedX, adjustedY, adjustedWidth, adjustedHeight);

        roundShader.unload();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawEntityFace(float x, float y, float size, float alpha, float radius, float u, float v, final Entity target) {
        try {
            if (target instanceof AbstractClientPlayerEntity player) {
                final ResourceLocation skin = player.getLocationSkin();

                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                scaleStart(x + size / 2f, y + size / 2f, 1.1f);
                GL11.glColor4f(1, 1 - getHurt(player), 1 - getHurt(player), 1);

                mc.getTextureManager().bindTexture(skin);

                roundTextShader.init();
                roundTextShader.setUniform("location", x * 2.0f, window.getHeight() - size * 2.0f - y * 2.0f);
                roundTextShader.setUniform("size", size * 2.0f, size * 2.0f);
                roundTextShader.setUniform("texture", 0);
                roundTextShader.setUniform("radius", radius * 2.0f);
                roundTextShader.setUniform("alpha", alpha);
                roundTextShader.setUniform("u", u);
                roundTextShader.setUniform("v", v);
                roundTextShader.setUniform("w", 0.125f);
                roundTextShader.setUniform("h", 0.125f);
                ShaderUtil.drawQuads(x, y, size, size);
                roundTextShader.unload();

                drawRound(x, y, size, size, radius, ColorUtil.rgb(255, 0, 0, (int) (120 * getHurt(player) * alpha)));
                scaleStop();
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glPopMatrix();
            } else {
                scaleStart(x + size / 2f, y + size / 2f, 1.1f);
                drawRound(x, y, size, size, radius, ColorUtil.rgb(11, 11, 11, (int) (190 * alpha)));
                scaleStop();
            }
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawTexture(MatrixStack matrices, ResourceLocation texture, float width, float height, int colorTL, int colorTR, int colorBL, int colorBR) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        mc.getTextureManager().bindTexture(texture);
        matrices.translate(-width / 2, -height / 2, -0.01);
        Matrix4f matrix = matrices.getLast().getMatrix();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(matrix, 0f, height, 0f).tex(0f, 1f).color(colorTL).endVertex();
        buffer.pos(matrix, width, height, 0f).tex(1f, 1f).color(colorTR).endVertex();
        buffer.pos(matrix, width, 0f, 0f).tex(1f, 0f).color(colorBL).endVertex();
        buffer.pos(matrix, 0f, 0f, 0f).tex(0f, 0f).color(colorBR).endVertex();
        Tessellator.getInstance().draw();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
    }

    public static float getHurt(LivingEntity entity) {
        return (entity.hurtTime - (entity.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
    }

    /**
     * Draws a health bar with gradient effect
     * @param x X position of the bar
     * @param y Y position of the bar
     * @param width Total width of the bar
     * @param height Height of the bar
     * @param radius Corner radius
     * @param alpha Transparency (0-1)
     * @param barType Type of bar: 0 = background, 1 = health, 2 = absorption, 3 = custom client color
     * @param customColor Custom color for barType 3 (RGB format)
     */
    public static void drawHealthBar(float x, float y, float width, float height, float radius, float alpha, int barType, int customColor) {
        Vector4f cornerRadius;
        Vector4i colors;

        // Set appropriate radius based on bar type
        if (barType == 0) { // Background
            cornerRadius = new Vector4f(radius, radius, radius, radius);
        } else {
            cornerRadius = new Vector4f(radius, radius, radius, radius);
        }

        // Set appropriate colors based on bar type
        if (barType == 0) { // Background - now using a very dark blue instead of black
            int darkBlueBackground = ColorUtil.rgb(15, 30, 60, (int)(190 * alpha));
            colors = new Vector4i(darkBlueBackground, darkBlueBackground, darkBlueBackground, darkBlueBackground);
        } else if (barType == 1) { // Health
            int darkBlue = ColorUtil.rgb(30, 70, 130, (int)(255 * alpha));     // Dark blue (left)
            int brightBlue = ColorUtil.rgb(65, 140, 255, (int)(255 * alpha));  // Blue (right)
            colors = new Vector4i(darkBlue, brightBlue, brightBlue, darkBlue);
        } else if (barType == 2) { // Absorption
            int brightGold = ColorUtil.rgb(255, 215, 0, (int)(255 * alpha));    // Gold (left)
            int darkGold = ColorUtil.rgb(180, 150, 0, (int)(255 * alpha));      // Dark gold (right)
            colors = new Vector4i(brightGold, darkGold, darkGold, brightGold);
        } else { // Client color or custom
            int darkColor = ColorUtil.manipulateColor(customColor, 0.7f);  // Darker version (70% brightness)
            int brightColor = customColor;

            // Apply alpha
            darkColor = ColorUtil.applyAlpha(darkColor, (int)(255 * alpha));
            brightColor = ColorUtil.applyAlpha(brightColor, (int)(255 * alpha));

            colors = new Vector4i(darkColor, brightColor, brightColor, darkColor);
        }

        // Draw the bar
        drawRound(x, y, width, height, cornerRadius, 1.0f, colors);
    }

    /**
     * Draws a health bar with gradient effect and custom corner radius
     * @param x X position of the bar
     * @param y Y position of the bar
     * @param width Total width of the bar
     * @param height Height of the bar
     * @param cornerRadius Custom corner radius for each corner (topLeft, topRight, bottomRight, bottomLeft)
     * @param alpha Transparency (0-1)
     * @param barType Type of bar: 0 = background, 1 = health, 2 = absorption, 3 = custom client color
     * @param customColor Custom color for barType 3 (RGB format)
     */
    public static void drawHealthBar(float x, float y, float width, float height, Vector4f cornerRadius, float alpha, int barType, int customColor) {
        Vector4i colors;

        // Set appropriate colors based on bar type
        if (barType == 0) { // Background - now using a very dark blue instead of black
            int darkBlueBackground = ColorUtil.rgb(15, 30, 60, (int)(190 * alpha));
            colors = new Vector4i(darkBlueBackground, darkBlueBackground, darkBlueBackground, darkBlueBackground);
        } else if (barType == 1) { // Health
            int darkBlue = ColorUtil.rgb(30, 70, 130, (int)(255 * alpha));     // Dark blue (left)
            int brightBlue = ColorUtil.rgb(65, 140, 255, (int)(255 * alpha));  // Blue (right)
            colors = new Vector4i(darkBlue, brightBlue, brightBlue, darkBlue);
        } else if (barType == 2) { // Absorption
            int brightGold = ColorUtil.rgb(255, 215, 0, (int)(255 * alpha));    // Gold (left)
            int darkGold = ColorUtil.rgb(180, 150, 0, (int)(255 * alpha));      // Dark gold (right)
            colors = new Vector4i(brightGold, darkGold, darkGold, brightGold);
        } else { // Client color or custom
            int darkColor = ColorUtil.manipulateColor(customColor, 0.7f);  // Darker version (70% brightness)
            int brightColor = customColor;

            // Apply alpha
            darkColor = ColorUtil.applyAlpha(darkColor, (int)(255 * alpha));
            brightColor = ColorUtil.applyAlpha(brightColor, (int)(255 * alpha));

            colors = new Vector4i(darkColor, brightColor, brightColor, darkColor);
        }

        // Draw the bar
        drawRound(x, y, width, height, cornerRadius, 1.0f, colors);
    }
}