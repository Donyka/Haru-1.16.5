package kz.haru.implement.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Render2DEvent extends Event {
    final MatrixStack matrixStack;
    final float partialTicks;
    final RenderLayer renderLayer;

    public boolean isFirstLayer() {
        return renderLayer == RenderLayer.FIRST;
    }

    public boolean isSecondLayer() {
        return renderLayer == RenderLayer.SECOND;
    }

    public enum RenderLayer {
        FIRST, SECOND
    }
}
