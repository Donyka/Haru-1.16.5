package kz.haru.implement.screen.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.implement.screen.clickgui.components.build.Component;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MouseUtil;
import kz.haru.common.utils.render.RenderUtil;
import net.minecraft.util.math.vector.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class ModeComponent extends Component {
    private final ModeSetting setting;
    private final float tilePadding = 3f;
    private final float tileRound = 4f;
    private final List<TilePosition> tilePositions = new ArrayList<>();
    private final List<Float> alphaAnimations = new ArrayList<>();

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
        setHeight(24f);

        for (String value : setting.getPossibleValues()) {
            alphaAnimations.add(setting.is(value) ? 250f : 100f);
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float padding = 3f * scale;
        float textY = getY();
        float tileHeight = 12f * scale;

        Fonts.medium.drawText(stack, setting.getName(), getX(), textY, ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())), 7f * scale);

        tilePositions.clear();
        float currentX = getX();
        float currentY = getY() + 8f * scale;

        for (String value : setting.getPossibleValues()) {
            float tileSize = 6.5f;
            float textWidth = Fonts.regular.getWidth(value, tileSize * scale);
            float tileWidth = textWidth + (tileSize + 0.5f) * scale;

            if (currentX + tileWidth > getX() + getWidth()) {
                currentX = getX();
                currentY += tileHeight + padding / 2f;
            }

            tilePositions.add(new TilePosition(value, currentX, currentY, tileWidth, tileHeight));

            boolean selected = setting.is(value);

            int index = setting.getPossibleValues().indexOf(value);
            float targetAlpha = selected ? 250f : 100f;

            while (alphaAnimations.size() <= index) {
                alphaAnimations.add(selected ? 250f : 100f);
            }

            float currentAlpha = alphaAnimations.get(index);
            currentAlpha += (targetAlpha - currentAlpha) * 0.1f;
            alphaAnimations.set(index, currentAlpha);

            //int bgColor = selected ? ColorUtil.getClientColor((int)currentAlpha * getAlpha()) : ColorUtil.rgb(45, 45, 45, (int) (255 * getAlpha()));
            int textColor = selected ? ColorUtil.rgb(255, 255, 255, (int) (255 * getAlpha())) : ColorUtil.rgb(170, 170, 170, (int) (255 * getAlpha()));

            int alpha2 = (int) (currentAlpha * getAlpha());
            Vector4i color = new Vector4i(ColorUtil.getClientColor(90, alpha2), ColorUtil.getClientColor(90, alpha2), ColorUtil.getClientColor(180, alpha2), ColorUtil.getClientColor(180, alpha2));
            Vector4i color2 = new Vector4i(ColorUtil.rgb(45, 45, 45, (int) (255 * getAlpha())), ColorUtil.rgb(45, 45, 45, (int) (255 * getAlpha())), ColorUtil.rgb(45, 45, 45, (int) (255 * getAlpha())), ColorUtil.rgb(45, 45, 45, (int) (255 * getAlpha())));
            Vector4i bgColor = selected ? color : color2;


            RenderUtil.drawRound(currentX, currentY, tileWidth, tileHeight, new Vector4f(2f * scale,2f * scale,2f * scale,2f * scale), bgColor);
            Fonts.regular.drawText(stack, value, currentX + (tileWidth - textWidth) / 2, currentY + (tileHeight - tileSize * scale) / 2 + scale / 2f, textColor, tileSize * scale);

            currentX += tileWidth + padding / 2f;
        }

        float totalHeight = (currentY - getY()) + tileHeight + padding;
        setHeight(totalHeight);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        super.mouseClick(mouseX, mouseY, mouse);
        if(mouse == 0) {
            for (TilePosition tile : tilePositions) {
                if (MouseUtil.isHovered(mouseX, mouseY, tile.x, tile.y, tile.width, tile.height)) {
                    setting.value(tile.value);
                    return;
                }
            }
        }
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }

    private record TilePosition(String value, float x, float y, float width, float height) { }
}