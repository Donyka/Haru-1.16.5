package kz.haru.common.utils.draggable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import kz.haru.api.module.Module;
import kz.haru.api.module.setting.Setting;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.math.MouseUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.round;

@Getter
@Setter
public class Draggable implements IMinecraft {

    @Expose @SerializedName("name") private final String name;
    @Expose @SerializedName("x") private float x;
    @Expose @SerializedName("y") private float y;

    private float startX = 0f;
    private float startY = 0f;
    private boolean dragging = false;
    private boolean settingsShown = false;
    
    private float width = 0f;
    private float height = 0f;

    private final Module module;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Draggable(Module module, String name, float initialX, float initialY) {
        this.module = module;
        this.name = name;
        this.x = roundToHalf(initialX);
        this.y = roundToHalf(initialY);
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public void onDraw() {
        if (!(mc.currentScreen instanceof ChatScreen) || !dragging) {
            setDragging(false);
            return;
        }

        x = MathUtil.animation(normaliseX() - startX, x, 0.15f);
        y = MathUtil.animation(normaliseY() - startY, y, 0.15f);
    }

    public void onClick(int button) {
        if (!isHovering()) {
            if (button == 0 && settingsShown) {
                settingsShown = false;
            }
            return;
        }

        if (button == 0) {
            if (DraggableManager.getDraggables().values().stream().anyMatch(Draggable::isDragging)) {
                return;
            }
            dragging = true;
            startX = normaliseX() - x;
            startY = normaliseY() - y;
        } else if (button == 1) {
            settingsShown = !settingsShown;
        }
    }

    public boolean isHovering() {
        float mouseX = normaliseX();
        float mouseY = normaliseY();
        return mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height);
    }

    private float normaliseX() {
        return (float) (mc.mouseHelper.getMouseX() / window.getGuiScaleFactor());
    }

    private float normaliseY() {
        return (float) (mc.mouseHelper.getMouseY() / window.getGuiScaleFactor());
    }

    public void onRelease(int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    private float roundToHalf(float value) {
        return (float) (round(value * 2) / 2.0);
    }
}
