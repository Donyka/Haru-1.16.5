package kz.haru.implement.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Render3DEvent extends Event {
    private final MatrixStack matrixStack;
    private final float partialTicks;
}
