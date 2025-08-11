package kz.haru.implement.events.render;

import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RemovalsEvent extends Event {
    private final CancelRender cancelRender;

    public enum CancelRender {
        GRASS, HURT_CAMERA, PARTICLES, FIRE, TOTEM
    }
}
