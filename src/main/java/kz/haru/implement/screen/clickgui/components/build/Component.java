package kz.haru.implement.screen.clickgui.components.build;

import kz.haru.implement.screen.clickgui.Panel;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Component implements IComponent {
    private float x, y, width, height, alpha;
    private Panel panel;
    private final AnimationUtil visibleAnim = new AnimationUtil(Easing.EXPO_OUT, 350);

    public boolean isVisible() {
        return true;
    }
}