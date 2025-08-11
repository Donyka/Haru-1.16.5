package kz.haru.implement.modules.render;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.ColorSetting;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.implement.screen.hud.*;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.draggable.DraggableManager;
import kz.haru.implement.events.render.Render2DEvent;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleRegister(name = "Interface", category = Category.RENDER, desc = "Интерфейс этого бездарного чита")
public class InterfaceModule extends Module {
    public final MultiModeSetting elements = new MultiModeSetting("Elements")
            .addValues("Watermark", "Keybinds","Potion", "Target hud", "Ping", "TPS", "Coords", "Staff list")
            .select("Watermark");
    public final FloatSetting hudScale = new FloatSetting("Interface scale").value(100f).range(60f, 150f).step(1f).runValue(false);

    public final BooleanSetting singleColor = new BooleanSetting("Single Color").value(false);

    public final ColorSetting clientColor = new ColorSetting("Color").value(ColorUtil.rgb(100, 150, 255));
    public final ColorSetting clientColorNext = new ColorSetting("Second Color").value(ColorUtil.rgb(255, 100, 150)).setVisible(() -> !singleColor.getValue());
    ;


    // *************************************************************************************** //

    @Getter
    private final List<ElementDraggable> elementDraggables = new ArrayList<>();

    private final Draggable watermarkDrag = DraggableManager.installDraggable(this, "Watermark", 3f, 3f);
    private final WatermarkDraggable watermarkRenderer = new WatermarkDraggable(watermarkDrag);

    private final Draggable keybindsDrag = DraggableManager.installDraggable(this, "Keybinds", 3f, 30f);
    private final KeybindsDraggable keybindsRenderer = new KeybindsDraggable(keybindsDrag);

    private final Draggable potionDrag = DraggableManager.installDraggable(this, "Potion", 30f, 30f);
    private final PotionDraggable potionRenderer = new PotionDraggable(potionDrag);

    private final Draggable targetHudDrag = DraggableManager.installDraggable(this, "Target info", 30f, 30f);
    private final TargetHudDraggable targetHudDraggable = new TargetHudDraggable(targetHudDrag);
    
    private final Draggable pingDrag = DraggableManager.installDraggable(this, "Ping", 3f, 50f);
    private final PingDraggable pingRenderer = new PingDraggable(pingDrag);

    private final Draggable coordsDrag = DraggableManager.installDraggable(this, "Coords", 3f, 90f);
    private final CoordsDraggable cordsRenderer = new CoordsDraggable(coordsDrag);
    
    private final Draggable tpsDrag = DraggableManager.installDraggable(this, "TPS", 3f, 70f);
    private final TpsDraggable tpsRenderer = new TpsDraggable(tpsDrag);

    private final Draggable staffListDrag = DraggableManager.installDraggable(this, "StaffList", 30f, 70f);
    private final StaffListDraggable staffListRenderer = new StaffListDraggable(staffListDrag);

    public InterfaceModule() {
        setup(elements, hudScale,singleColor, clientColor, clientColorNext);

        elementDraggables.addAll(Arrays.asList(staffListRenderer, tpsRenderer, watermarkRenderer, keybindsRenderer,potionRenderer, pingRenderer, targetHudDraggable));
    }
    
    public static InterfaceModule get() {
        return Module.get(InterfaceModule.class);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (event.isFirstLayer()) return;

        if (elements.is("Staff list")) {
            staffListRenderer.render(event);
            staffListRenderer.update();
        }
        if (elements.is("Watermark")) {
            watermarkRenderer.render(event);
        }
        if (elements.is("Keybinds")) {
            keybindsRenderer.render(event);
        }
        if (elements.is("Potion")) {
            potionRenderer.render(event);
        }
        if (elements.is("Target hud")) {
            targetHudDraggable.render(event);
        }
        if (elements.is("Ping")) {
            pingRenderer.render(event);
        }
        if (elements.is("Coords")) {
            cordsRenderer.render(event);
        }
        if (elements.is("TPS")) {
            tpsRenderer.render(event);
        }
    }
}
