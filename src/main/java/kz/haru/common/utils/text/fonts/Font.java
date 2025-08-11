package kz.haru.common.utils.text.fonts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.shader.ShaderUtil;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static kz.haru.common.utils.text.TextUtil.getColorFromCode;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;

public class Font implements IMinecraft {
    ShaderUtil shader = ShaderUtil.getTextShader();

    private final MsdfFont font;

    public Font(String name) {
        font = MsdfFont.builder().withAtlas(name + ".png").withData(name + ".json").build();
    }

    public void drawText(MatrixStack stack, String text, float x, float y, int color, float size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.init();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", 0f);
        shader.setUniform("Outline", 0);
        shader.setUniform("OutlineThickness", 0f);

        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);
        shader.setUniform("color", ColorUtil.rgba(color));

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, 0, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        Tessellator.getInstance().draw();

        this.font.unbind();
        shader.unload();
    }

    public void drawText(MatrixStack stack, ITextComponent text, float x, float y, float size, int alpha) {
        float offset = 0;
        String fullText = text.getString();
        if (fullText.contains("§")) {
            String[] parts = fullText.split("(?<=§[0-9a-fk-or])");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    int color = Color.WHITE.getRGB();
                    if (part.startsWith("§") && part.length() > 1) {
                        char code = part.charAt(1);
                        color = getColorFromCode(code, alpha);
                        part = part.substring(2);
                    }
                    drawText(stack, part, x + offset, y, ColorUtil.setAlpha(color, alpha), size);
                    offset += getWidth(part, size);
                }
            }
        } else {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());
            int color = text.getStyle().getColor() == null ? Color.WHITE.getRGB() : text.getStyle().getColor().getColor();
            drawText(stack, draw, x + offset, y, ColorUtil.setAlpha(color, alpha), size);
        }
    }

    public float getWidth(ITextComponent text, float size) {
        float offset = 0;
        for (ITextComponent it : text.getSiblings()) {

            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();
                offset += getWidth(draw, size);
            }
            if (it.getSiblings().size() <= 1) {
                String draw = TextFormatting.getTextWithoutFormattingCodes(it.getString());
                offset += getWidth(draw, size);
            }
        }
        if (text.getSiblings().isEmpty()) {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());
            offset += getWidth(draw, size);
        }
        return offset;
    }

    public void drawTextBuilding(MatrixStack stack, String text, float x, float y, int color, float size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.init();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", 0f);
        shader.setUniform("Outline", 0);
        shader.setUniform("OutlineThickness", 0f);
        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);
        shader.setUniform("color", ColorUtil.rgba(color));

        this.font.bind();
        GlStateManager.enableBlend();
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, 0, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        this.font.unbind();
        shader.unload();
    }

    public void drawCenteredText(MatrixStack stack, String text, float x, float y, int color, float size) {
        drawText(stack, text, x - getWidth(text, size) / 2f, y, color, size);
    }

    public void drawCenteredText(MatrixStack stack, ITextComponent text, float x, float y, float size) {
        drawText(stack, text, x - getWidth(text, size) / 2f, y, size, 255);
    }


    public void drawCenteredTextWithOutline(MatrixStack stack, String text, float x, float y, int color, float size) {
        drawTextWithOutline(stack, text, x - getWidth(text, size) / 2f, y, color, size, 0.05f);
    }

    public void drawCenteredTextEmpty(MatrixStack stack, String text, float x, float y, int color, float size) {
        drawEmpty(stack, text, x - getWidth(text, size) / 2f, y, size, color, 0);
    }

    public void drawCenteredTextEmptyOutline(MatrixStack stack, String text, float x, float y, int color, float size) {
        drawEmptyWithOutline(stack, text, x - getWidth(text, size) / 2f, y, size, color, 0);
    }

    public void drawCenteredText(MatrixStack stack, String text, float x, float y, int color, float size, float thickness) {
        drawText(stack, text, x - getWidth(text, size, thickness) / 2f, y, color, size, thickness);
    }

    public void drawText(MatrixStack stack, String text, float x, float y, int color, float size, float thickness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.init();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", thickness);
        shader.setUniform("color", ColorUtil.rgba(color));
        shader.setUniform("Outline", 0);
        shader.setUniform("OutlineThickness", 0f);
        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        Tessellator.getInstance().draw();

        this.font.unbind();
        shader.unload();
    }

    public void drawTextWithOutline(MatrixStack stack, String text, float x, float y, int color, float size, float thickness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.init();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", 0.5f);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", thickness);
        shader.setUniform("color", ColorUtil.rgba(color));
        shader.setUniform("Outline", 1);
        shader.setUniform("OutlineThickness", 0.2f);
        shader.setUniform("OutlineColor", 0f, 0f, 0f, 1f);

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
        Tessellator.getInstance().draw();

        this.font.unbind();
        shader.unload();
    }

    public void init(float thickness, float smoothness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        FontData.AtlasData atlas = this.font.getAtlas();
        shader.init();
        shader.setUniform("Sampler", 0);
        shader.setUniform("EdgeStrength", smoothness);
        shader.setUniform("TextureSize", atlas.width(), atlas.height());
        shader.setUniform("Range", atlas.range());
        shader.setUniform("Thickness", thickness);
        shader.setUniform("Outline", 0);
        shader.setUniform("OutlineThickness", 0f);

        shader.setUniform("OutlineColor", 1f, 1f, 1f, 1f);

        this.font.bind();
        GlStateManager.enableBlend();
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
    }

    public void drawEmpty(MatrixStack stack, String text, float x, float y, float size, int color, float thickness) {
        shader.setUniform("color", ColorUtil.rgba(color));
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
    }

    public void drawEmptyWithOutline(MatrixStack stack, String text, float x, float y, float size, int color, float thickness) {
        shader.setUniform("Outline", 1);
        shader.setUniform("OutlineThickness", 0.2f);

        shader.setUniform("OutlineColor", 0f, 0f, 0f, 1f);
        shader.setUniform("color", ColorUtil.rgba(color));
        this.font.applyGlyphs(stack.getLast().getMatrix(), Tessellator.getInstance().getBuffer(), size, text, thickness, x, y + font.getMetrics().baselineHeight() * size, 0, 255, 255, 255, 255);
    }

    public void end() {
        Tessellator.getInstance().draw();
        this.font.unbind();

        shader.unload();
    }

    public float getWidth(String text, float size) {
        return font.getWidth(text, size);
    }

    public float getWidth(String text, float size, float thickness) {
        return font.getWidth(text, size, thickness);
    }

    public float getHeight(float size) {
        return size;
    }

}
