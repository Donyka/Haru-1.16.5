package kz.haru.implement.events.connection;

import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;

@Getter
@AllArgsConstructor
public class CloseScreenEvent extends Event {
    final Screen screen;
    final int windowId;
}
