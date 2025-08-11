package kz.haru.implement.events.input;

import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ButtonInputEvent extends Event {
    int action;
    int button;
}
