package kz.haru.common.utils.math;

import lombok.Getter;

@Getter
public class TimerUtil {
    private long lastTime = System.currentTimeMillis();

    public boolean hasReached(long time) {
        return System.currentTimeMillis() - lastTime >= time;
    }

    public void setLastMS(long newValue) {
        lastTime = System.currentTimeMillis() + newValue;
    }
    public void reset() {
        lastTime = System.currentTimeMillis();
    }
}
