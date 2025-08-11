package kz.haru.common.utils.client;

import net.minecraft.util.math.MathHelper;

public class TpsUtil {
    private static final float[] tickRates = new float[20];
    private static int tickIndex = 0;
    private static long timeLastTimeUpdate = -1;
    
    public static void onTimeUpdate() {
        if (timeLastTimeUpdate != -1) {
            float timeElapsed = (float) (System.currentTimeMillis() - timeLastTimeUpdate) / 1000.0F;
            tickRates[tickIndex % tickRates.length] = MathHelper.clamp(20.0F / timeElapsed, 0.0F, 20.0F);
            tickIndex++;
        }
        
        timeLastTimeUpdate = System.currentTimeMillis();
    }
    
    public static float getTickRate() {
        if (tickIndex == 0) return 20.0F;
        
        int numTicks = Math.min(tickRates.length, tickIndex);
        float tickSum = 0.0F;
        
        for (int i = 0; i < numTicks; i++) {
            tickSum += tickRates[i];
        }
        
        return tickSum / numTicks;
    }
    
    public static void reset() {
        for (int i = 0; i < tickRates.length; i++) {
            tickRates[i] = 0.0F;
        }
        tickIndex = 0;
        timeLastTimeUpdate = -1;
    }
}