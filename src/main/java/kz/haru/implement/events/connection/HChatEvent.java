package kz.haru.implement.events.connection;

import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HChatEvent extends Event {
    final String message;
}
