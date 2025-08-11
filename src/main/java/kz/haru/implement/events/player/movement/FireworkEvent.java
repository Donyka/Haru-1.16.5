package kz.haru.implement.events.player.movement;

import kz.haru.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;

@Getter
@Setter
@AllArgsConstructor
public class FireworkEvent extends Event {
    float speed;
    LivingEntity booster;
}
