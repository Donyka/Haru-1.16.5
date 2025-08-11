package kz.haru.implement.screen.hud;

import kz.haru.client.functions.UpdateFunctions;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.render.Render2DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PotionDraggable extends ElementDraggable {
    public PotionDraggable(Draggable draggable) {
        super(draggable);
    }

    private final AnimationUtil globalAnimation = new AnimationUtil(Easing.EXPO_OUT, 300);
    private final AnimationUtil heightAnimation = new AnimationUtil(Easing.EXPO_OUT, 100);
    private final AnimationUtil widthAnimation = new AnimationUtil(Easing.EXPO_OUT, 100);

    @Override
    public void render(Render2DEvent event) {
        super.render(event);

        if (Minecraft.getInstance().gameSettings.showDebugInfo) return;

        boolean show = Minecraft.getInstance().currentScreen instanceof ChatScreen;
        
        if (Minecraft.getInstance().player != null) {
            Collection<EffectInstance> effects = Minecraft.getInstance().player.getActivePotionEffects();
            if (!effects.isEmpty()) {
                show = true;
            }
        }

        String name = "Potion";
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 7.5f * scale;
        float nameWidth = Fonts.bold.getWidth(name, fontSize);
        float gap = 3f * scale;
        float width = nameWidth + gap * 4f;
        float height = fontSize + gap * 2f;
        float x = getDraggable().getX();
        float y = getDraggable().getY();

        RenderUtil.drawElementClientRect(x, y, (float) widthAnimation.getValue(), (float) heightAnimation.getValue(), (float) globalAnimation.getValue(), "");
        Fonts.bold.drawCenteredText(event.getMatrixStack(), name, (float) (x + widthAnimation.getValue() / 2f), y + fontSize / 2f - 0.5f * scale, ColorUtil.rgb(255, 255, 255, (int) (255 * globalAnimation.getValue())), fontSize);

        y += fontSize + gap;
        height += gap;

        if (Minecraft.getInstance().player != null) {
            Collection<EffectInstance> effects = Minecraft.getInstance().player.getActivePotionEffects();
            
            if (!effects.isEmpty()) {
                List<EffectInstance> sortedEffects = new ArrayList<>(effects);
                sortedEffects.sort(Comparator.comparing(effect -> I18n.format(effect.getEffectName())));
                
                for (EffectInstance effect : sortedEffects) {

                    String potionName = I18n.format(effect.getEffectName());
                    int amplifier = effect.getAmplifier();
                    String amplifierStr = amplifier > 0 ? " " + (amplifier + 1) : "";
                    String text = potionName + amplifierStr;
                    
                    String duration = EffectUtils.getPotionDurationString(effect, 1.0F);
                    float textWidth = Fonts.bold.getWidth(text, fontSize);
                    float durationWidth = Fonts.bold.getWidth(duration, fontSize);
                    float localWidth = textWidth + gap * 4f + durationWidth;

                    if (localWidth > width) {
                        width = localWidth;
                    }

                    int potionColor = effect.getPotion().getLiquidColor();
                    
                    Fonts.bold.drawText(event.getMatrixStack(), text, x + gap, y + fontSize / 2f, 
                            ColorUtil.rgb(255, 255, 255, (int) (255 * globalAnimation.getValue())), fontSize);
                    
                    Fonts.bold.drawText(event.getMatrixStack(), duration, 
                            (float) (x - gap + widthAnimation.getValue() - durationWidth), 
                            y + fontSize / 2f, 
                            ColorUtil.rgb(255, 255, 255, (int) (255 * globalAnimation.getValue())), fontSize);
                    
                    y += fontSize;
                    height += fontSize;
                }
            }
        }

        heightAnimation.run(height);
        widthAnimation.run(width);
        globalAnimation.run(show ? 1.0 : 0.0);
        getDraggable().setWidth(width);
        getDraggable().setHeight(height);
    }
}