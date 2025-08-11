package kz.haru.implement.screen.hud;

import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.ModeSetting;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.draggable.DraggableSettingsMenu;
import kz.haru.common.utils.math.Vector4i;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.render.Render2DEvent;
import kz.haru.implement.modules.combat.KillAuraModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

public class TargetHudDraggable extends ElementDraggable {
    private final ModeSetting style = new ModeSetting("Style").value("Default").addValues("Default",  "Compact");
    private final BooleanSetting textn = new BooleanSetting("Text").value(true).setVisible(() -> style.is("Default"));
    private final BooleanSetting armor = new BooleanSetting("Armor").value(false);
    private final BooleanSetting armorBackground = new BooleanSetting("Armor Background").value(false);

    private final DraggableSettingsMenu settingsMenu;
    private float showArmorSlideAnim = 0f;
    private float armorBackgroundSlideAnim = 0f;
    float healthAnimation = 0.0f;

    public TargetHudDraggable(Draggable draggable) {
        super(draggable);
        draggable.addSettings(style,textn, armor, armorBackground);
        this.settingsMenu = new DraggableSettingsMenu(draggable);
    }

    private LivingEntity target = null;
    private final AnimationUtil globalAnimation = new AnimationUtil(Easing.BACK_OUT, 300);

    @Override
    public void render(Render2DEvent event) {
        super.render(event);

        updateTarget();

        double currentAnimValue = globalAnimation.getValue();

        boolean isTargetVisible = (float) globalAnimation.getValue() > 0.01;
        if (!isTargetVisible || target == null) return;

        String text = target.getName().getString();
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float x = getDraggable().getX();
        float y = getDraggable().getY();
        float headSize = 23f * scale;
        float fontSize = 8f * scale;
        float gap = 3f * scale;
        float minWidth = 100f * scale;

        float width = Math.max(minWidth, Fonts.bold.getWidth(text, fontSize) + gap * 4f + headSize);
        float height = headSize + gap * 2f;
        float anim = (float) Math.min(Math.max(globalAnimation.getValue(), 0f), 1f);

        float hurtBlow = RenderUtil.getHurt(target) * 2f;
        float[] faceProperties = new float[]{x + gap + hurtBlow, y + gap + hurtBlow, headSize - hurtBlow * 2f};

        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();

        healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);


        RenderUtil.scaleStart(x + width / 2f, y + height / 2, (float) globalAnimation.getValue());

        if (style.is("Default")) {
            RenderUtil.drawElementClientRect(x, y, width, height, anim, 6f);
        } else if (style.is("Compact")) {
            RenderUtil.drawElementClientRect(x, y, width, height / 1.8f, anim, 6f);
        }

        if (style.is("Default")) {
            RenderUtil.drawEntityFace(faceProperties[0], faceProperties[1], faceProperties[2], anim, 4f * scale, 1f / 8f, 1f / 8f, target);
        }

        float widthhp = style.is("Default") ? 65 * healthAnimation : 92.5f * healthAnimation;
        float xhp = style.is("Default") ? 29 : 4f;
        float yhp = style.is("Default") ? 17 : 3.5f;

        float barWidth = style.is("Default") ? 65 : 92.5f;
        float barX = style.is("Default") ?  (x + xhp * scale) + 2 : (x + xhp * scale) - 0.5f;
        float barY = style.is("Default") ? (y + yhp * scale) - 3 : (y + yhp * scale);


        RenderUtil.drawRound(barX, barY, barWidth * scale, 10 * scale, 2, 0.05f, ColorUtil.rgb(15, 15, 15, (int) (200 * anim)));
        RenderUtil.drawRound(barX, barY, widthhp * scale, 10 * scale, new Vector4f(2,2,2,2), 0.05f, new Vector4i(ColorUtil.getClientColor(0, (int) (200 * anim)), ColorUtil.getClientColor(90, (int) (200 * anim))));

        if (style.is("Default")) {
            Fonts.bold.drawText(event.getMatrixStack(), text, x + gap * 2f + headSize + 3,
                    y + gap + scale, ColorUtil.rgb(255, 255, 255, (int) (255 * anim)), fontSize);
        } else {
            float textWidth = Fonts.bold.getWidth(text, fontSize);
            float centeredX = x + (width - textWidth) / 2f;
            Fonts.bold.drawText(event.getMatrixStack(), text, centeredX, y + gap + 0.5f + scale,
                    ColorUtil.rgb(255, 255, 255, (int) (255 * anim)), fontSize);

        }

        if (textn.getValue() && style.is("Default")) {
            Fonts.bold.drawText(event.getMatrixStack(), "HP: " + String.valueOf(target.getHealth()), x + gap * 2f + headSize + 5, y + gap + scale + 11.55f * scale, ColorUtil.rgb(255, 255, 255, (int) (255 * anim)), fontSize);
        }

        RenderUtil.scaleStop();

        showArmorSlideAnim = MathUtil.animation(armor.getValue() ? 1f : 0f, showArmorSlideAnim, 0.2f);
        armorBackgroundSlideAnim = MathUtil.animation(armorBackground.getValue() && armor.getValue() ? 1f : 0f, armorBackgroundSlideAnim, 0.2f);

        getDraggable().setWidth(width);
        getDraggable().setHeight(height);

        if (armor.getValue()) {
            renderArmorItems(event, x, y - 5, width, scale, anim);
        }

        settingsMenu.render(event.getMatrixStack());
    }

    private void renderArmorItems(Render2DEvent event, float x, float y, float width, float scale, float anim) {
        float headSize = 23f * scale;
        float gap = 3f * scale;
        float height = headSize + gap * 2f;

        float itemSize = 20f * scale;
        float overlapFactor = 0.35f;

        float armorBarWidth = itemSize * 6 - (itemSize * overlapFactor * 5);
        float armorBarHeight = itemSize;
        float armorBarX = x + (width - armorBarWidth) / 1.75f;

        float armorOffsetY = -10f * scale;
        float armorBarY = y + armorOffsetY;
        float armorBarAnim = showArmorSlideAnim * anim;

        if (armorBarAnim <= 0.01f) return;

        RenderUtil.scaleStart(x + width / 2f, armorBarY + armorBarHeight / 2f, armorBarAnim);

        ItemStack[] armorItems = new ItemStack[4];
        int index = 0;

        for (ItemStack itemStack : target.getArmorInventoryList()) {
            armorItems[index++] = itemStack;
        }

        ItemStack mainHandItem = target.getHeldItemMainhand();
        ItemStack offHandItem = target.getHeldItemOffhand();

        ItemStack[] allItems = new ItemStack[6];
        allItems[0] = mainHandItem;
        allItems[1] = armorItems[3];
        allItems[2] = armorItems[2];
        allItems[3] = armorItems[1];
        allItems[4] = armorItems[0];
        allItems[5] = offHandItem;

        boolean hasItems = false;
        for (ItemStack item : allItems) {
            if (!item.isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            RenderUtil.scaleStop();
            return;
        }

        float bgAnim = armorBackgroundSlideAnim * anim;

        if (bgAnim > 0.01f) {
            float bgPadding = 3f * scale;

            float bgX = armorBarX - bgPadding;
            float bgY = armorBarY - bgPadding;

            float totalBgWidth = armorBarWidth + bgPadding * 2;
            float totalBgHeight = armorBarHeight + bgPadding * 2;

            boolean overlapWithTargetHud = false;
            float targetHudTop = y;
            float visibleHeight = Math.max(0, targetHudTop - bgY);

            RenderUtil.drawRound(bgX, bgY + 5, totalBgWidth, visibleHeight, 4f * scale, ColorUtil.rgb(15, 15, 15, (int)(200 * bgAnim)));

        }

        for (int i = 0; i < 6; i++) {
            float itemX = armorBarX + i * (itemSize - (itemSize * overlapFactor));
            float itemY = armorBarY;

            if (!allItems[i].isEmpty()) {
                if (allItems[i].isDamaged()) {
                    float durability = 1.0f - ((float) allItems[i].getDamage() / (float) allItems[i].getMaxDamage());

                    float durabilityBarWidth = itemSize * 0.35f;
                    float durabilityPadding = (itemSize - durabilityBarWidth) / 10.5f;

                    float durabilityBarHeight = 1f * scale;
                    float durabilityBarY = itemY - durabilityBarHeight - 1f * scale;

                    RenderUtil.drawHealthBar(itemX + durabilityPadding, durabilityBarY, durabilityBarWidth, durabilityBarHeight, 0.5f * scale, anim, 3, ColorUtil.getClientColor());

                    int durabilityBarType;
                    if (durability > 0.7f) {
                        durabilityBarType = 3;
                    } else if (durability > 0.3f) {
                        durabilityBarType = 2;
                    } else {
                        durabilityBarType = 1;
                    }

                    RenderUtil.drawHealthBar(itemX + durabilityPadding, durabilityBarY, durabilityBarWidth * durability, durabilityBarHeight, 0.5f * scale, anim, durabilityBarType, ColorUtil.rgb(0, 255, 0, (int)(200 * anim)));
                }

                RenderSystem.pushMatrix();
                RenderSystem.scalef(1.0f, 1.0f, 1.0f);
                RenderSystem.translatef(0f, 0f, 200.0f + i);
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, anim);

                Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(allItems[i], (int)itemX, (int)itemY);

                RenderSystem.popMatrix();
            }
        }

        RenderUtil.scaleStop();
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        settingsMenu.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        settingsMenu.mouseReleased(mouseX, mouseY, button);
    }

    private void updateTarget() {
        KillAuraModule killAuraModule = KillAuraModule.get();
        boolean isChatOpened = Minecraft.getInstance().currentScreen instanceof ChatScreen;

        if (killAuraModule.target != null) target = killAuraModule.target;

        if (isChatOpened) target = Minecraft.getInstance().player;

        boolean forceAnimation = killAuraModule.target != null || isChatOpened;
        globalAnimation.setDuration(forceAnimation ? 250 : 350);
        globalAnimation.run(forceAnimation ? 1.0 : 0.0);
        globalAnimation.setEasing(forceAnimation ? Easing.BACK_OUT : Easing.BACK_IN);
    }
}
