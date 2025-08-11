package kz.haru.implement.screen.hud.interfaces;

import kz.haru.implement.events.render.Render2DEvent;

public interface IElementDraggable {
    default void render(Render2DEvent event) {}
    default void update() {}
    default void mouseClicked(float mouseX, float mouseY, int button) {}
    default void mouseReleased(float mouseX, float mouseY, int button) {}
}
