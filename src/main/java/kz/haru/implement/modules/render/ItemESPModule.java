package kz.haru.implement.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.ProjectionUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.render.Render2DEvent;
import kz.haru.common.utils.math.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.vector.Vector2f;

@ModuleRegister(name = "Item ESP", category = Category.RENDER, desc = "Отображает предметы")
public class ItemESPModule extends Module {
    private final BooleanSetting showBackground = new BooleanSetting("Show background").value(true);

    public ItemESPModule() {
        setup(showBackground);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (mc.player == null || mc.world == null || event.isSecondLayer()) return;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                renderItemESP(event.getMatrixStack(), itemEntity);
            }
        }
    }

    private void renderItemESP(MatrixStack ms, ItemEntity entity) {
        float xInterpol = (float) MathUtil.interpolate(entity.getPosX(), entity.prevPosX, mc.getRenderPartialTicks());
        float yInterpol = (float) (MathUtil.interpolate(entity.getPosY(), entity.prevPosY, mc.getRenderPartialTicks()) + entity.getHeight() + 0.2);
        float zInterpol = (float) MathUtil.interpolate(entity.getPosZ(), entity.prevPosZ, mc.getRenderPartialTicks());
        Vector2f pos = ProjectionUtil.project(xInterpol, yInterpol, zInterpol);

        String itemName = entity.getItem().getDisplayName().getString();
        String itemCount = entity.getItem().getCount() > 1 ? " x" + entity.getItem().getCount() : "";

        String displayText = itemName + itemCount;

        double distance = mc.player.getPositionVec().distanceTo(entity.getPositionVec());
        float scale = Math.max(0.5f, 1.0f - (float)(distance * 0.005f));
        float fontSize = 8f * scale;

        float textWidth = Fonts.bold.getWidth(displayText, fontSize);
        float x = pos.x - textWidth / 2f;
        float y = pos.y;

        if (showBackground.getValue()) {
            RenderUtil.drawRound(pos.x - textWidth / 2f - 2 * scale, y - 2 * scale, textWidth + 4 * scale, 14 * scale, 1f, ColorUtil.rgb(11, 11, 11, 140));
        }

        Fonts.bold.drawText(ms, displayText, x, y + 0.5f * scale, -1, fontSize);
    }
}