package kz.haru.common.utils.player.attacking;

import kz.haru.common.interfaces.IMinecraft;

public class ClickScheduler implements IMinecraft {
    private long delay = 0;
    private long lastClickTime = System.currentTimeMillis();
    private float lastCooldownProgress = 1.0f;

    public boolean isCooldownComplete() {
        float currentCooldown = mc.player != null ? mc.player.getCooledAttackStrength(0.5f) : 1.0f;
        long currentTime = System.currentTimeMillis();
        long timeSinceLastClick = currentTime - lastClickTime;

        boolean wasComplete = timeSinceLastClick >= delay && currentCooldown > 0.9f;
        lastCooldownProgress = currentCooldown;
        return wasComplete;
    }

    public boolean hasTicksElapsedSinceLastClick(int ticks) {
        return lastClickPassed() >= (ticks * 50L);
    }

    public long lastClickPassed() {
        return System.currentTimeMillis() - lastClickTime;
    }

    public void recalculate(long delay) {
        lastClickTime = System.currentTimeMillis();
        lastCooldownProgress = 0.0f;
        this.delay = delay;
    }

    public boolean isOneTickBeforeAttack() {
        long ticksSinceLastAttack = lastClickPassed() / 50L;
        return ticksSinceLastAttack >= 19 && ticksSinceLastAttack <= 21;
    }
}
