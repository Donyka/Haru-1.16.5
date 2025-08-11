package kz.haru.api.waveycapes.config;


import kz.haru.api.waveycapes.enums.CapeMovement;
import kz.haru.api.waveycapes.enums.CapeStyle;
import kz.haru.api.waveycapes.enums.WindMode;

public class Config {
    public WindMode windMode = WindMode.WAVES;
    public CapeStyle capeStyle = CapeStyle.SMOOTH;
    public CapeMovement capeMovement = CapeMovement.BASIC_SIMULATION;
    public int gravity = 25;
    public int heightMul = 5;
    public int straveMul = 5;
}
