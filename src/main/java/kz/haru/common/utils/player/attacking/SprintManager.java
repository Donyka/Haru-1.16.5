package kz.haru.common.utils.player.attacking;

import kz.haru.common.interfaces.IMinecraft;
import kz.haru.implement.modules.movement.SprintModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.client.CEntityActionPacket;

@Getter
@Setter
public class SprintManager implements IMinecraft {
    private Mode currentMode = Mode.DEFAULT;
    private boolean isStopSprintPacketSent = false;
    private boolean shouldUseLegitSprintReset = false;

    public void preAttack() {
        if (currentMode == Mode.DEFAULT) {
            if (mc.player.serverSprintState) {
                mc.player.setSprinting(false);
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                isStopSprintPacketSent = true;
            }
        } else if (currentMode == Mode.LEGIT) {
            SprintModule.get().setCanSprint(false);
            mc.gameSettings.keyBindSprint.setPressed(false);
            mc.player.setSprinting(false);
        }
    }

    public void postAttack() {
        if (currentMode == Mode.DEFAULT) {
            if (isStopSprintPacketSent && !mc.player.serverSprintState) {
                mc.player.setSprinting(true);
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                isStopSprintPacketSent = false;
            }
        } else if (currentMode == Mode.LEGIT) {
            SprintModule.get().setCanSprint(true);
        }
    }

    public enum Mode {
        DEFAULT, LEGIT, NONE
    }
}
