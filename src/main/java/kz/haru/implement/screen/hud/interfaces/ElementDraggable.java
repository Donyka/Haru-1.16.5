package kz.haru.implement.screen.hud.interfaces;

import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.draggable.DraggableSettingsMenu;
import kz.haru.implement.events.render.Render2DEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ElementDraggable implements IElementDraggable {
    private final Draggable draggable;
    private final DraggableSettingsMenu settingsMenu;
    
    public ElementDraggable(Draggable draggable) {
        this.draggable = draggable;
        this.settingsMenu = new DraggableSettingsMenu(draggable);
    }
    
    @Override
    public void render(Render2DEvent event) {
        settingsMenu.render(event.getMatrixStack());
    }
    
    @Override
    public void update() {}
    
    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        settingsMenu.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        settingsMenu.mouseReleased(mouseX, mouseY, button);
    }
}
