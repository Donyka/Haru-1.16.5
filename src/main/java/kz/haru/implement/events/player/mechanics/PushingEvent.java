package kz.haru.implement.events.player.mechanics;

import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushingEvent extends Event {
    private final PushingType pushingType;

    public enum PushingType {
        BLOCK, WATER, ENTITY
    }
}
