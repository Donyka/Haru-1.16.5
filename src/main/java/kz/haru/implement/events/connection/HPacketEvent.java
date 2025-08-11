package kz.haru.implement.events.connection;

import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.IPacket;

@Getter
@Setter
@AllArgsConstructor
public class HPacketEvent extends Event {
    final IPacket<?> packet;
    final ConnectionType connectionType;

    public boolean isSend() {
        return connectionType == ConnectionType.SEND;
    }

    public boolean isReceive() {
        return connectionType == ConnectionType.RECEIVE;
    }

    public enum ConnectionType {
        SEND, RECEIVE
    }
}
