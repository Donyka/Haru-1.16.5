package kz.haru.implement.modules.player;

import com.mojang.authlib.GameProfile;
import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.FloatSetting;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.common.utils.aiming.rotation.controller.Rotation;
import kz.haru.common.utils.aiming.rotation.controller.RotationController;
import kz.haru.common.utils.aiming.rotation.controller.RotationPlan;
import kz.haru.common.utils.math.TimerUtil;
import kz.haru.common.utils.player.movement.MoveUtil;
import kz.haru.implement.events.minecraft.MTickEvent;
import kz.haru.implement.events.player.movement.MovingEvent;
import kz.haru.implement.events.player.updates.PlayerTickEvent;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

@ModuleRegister(name = "Free Camera", category = Category.PLAYER, desc = "Глазастый")
public class FreeCameraModule extends Module {
    private final FloatSetting speedH = new FloatSetting("Speed H").value(1f).range(0.1f, 5f).step(0.1f);
    private final FloatSetting speedV = new FloatSetting("Speed V").value(0.5f).range(0.1f, 5f).step(0.1f);

    public FreeCameraModule() {
        setup(speedH, speedV);
    }
    
    public static FreeCameraModule get() {
        return Module.get(FreeCameraModule.class);
    }

    public ClientFakePlayer fakePlayer = null;
    private final TimerUtil timerUtil = new TimerUtil();

    @EventTarget
    public void onTick(MTickEvent event) {
        if (fakePlayer != null) {
            mc.player.setMotion(Vector3d.ZERO);
        }
    }

    @EventTarget
    public void onMoving(MovingEvent event) {
        if (fakePlayer != null) {
            mc.player.setMotion(Vector3d.ZERO);

            RotationPlan plan = RotationController.getInstance().getCurrentRotationPlan();
            Rotation rotation = RotationController.getInstance().getRotation();

            if (plan != null) {
                mc.player.rotationYaw = rotation.yaw;
                mc.player.rotationPitch = rotation.pitch;
            }

            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onLiving(PlayerTickEvent event) {
        if (fakePlayer != null) {
            mc.player.moveForward = 0f;
            mc.player.moveStrafing = 0f;
            mc.player.setMotion(Vector3d.ZERO);
            mc.player.movementInput.jump = mc.player.movementInput.sneaking = mc.player.movementInput.forwardKeyDown = mc.player.movementInput.backKeyDown = mc.player.movementInput.leftKeyDown = mc.player.movementInput.rightKeyDown = false;

            fakePlayer.noClip = true;
            fakePlayer.setOnGround(false);
            fakePlayer.abilities.isFlying = true;

            MoveUtil.setMotion(speedH.getValue(), fakePlayer);

            if (timerUtil.hasReached(100)) {
                if (mc.gameSettings.keyBindJump.pressed) {
                    fakePlayer.motion.y = speedV.getValue();
                }

                if (mc.gameSettings.keyBindSneak.pressed) {
                    fakePlayer.motion.y = -speedV.getValue();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);

        forceRemove();
        mc.setRenderViewEntity(mc.player);

        timerUtil.reset();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        sexyVoid();

        timerUtil.reset();
        super.onEnable();
    }

    public void sexyVoid() {
        mc.player.setMotion(Vector3d.ZERO);
        mc.player.movementInput = new MovementInput() {
            @Override
            public void tickMovement(boolean slowDown) {
                this.moveStrafe = 0;
                this.moveForward = 0;
                this.forwardKeyDown = false;
                this.backKeyDown = false;
                this.leftKeyDown = false;
                this.rightKeyDown = false;
                this.jump = false;
                this.sneaking = false;
            }
        };

        forceInit();
    }

    private void forceInit() {
        fakePlayer = new ClientFakePlayer(5221);
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.inventory = mc.player.inventory;
        mc.world.addEntity(5221, fakePlayer);
        fakePlayer.setHealth(mc.player.getHealth());
        fakePlayer.spawn();
        mc.setRenderViewEntity(fakePlayer);
    }

    private void forceRemove() {
        mc.world.removeEntityFromWorld(5221);
        fakePlayer = null;
    }

    public static class ClientFakePlayer extends ClientPlayerEntity implements IMinecraft {
        private static final ClientPlayNetHandler netHandler = new ClientPlayNetHandler(IMinecraft.mc, IMinecraft.mc.currentScreen, IMinecraft.mc.getConnection().getNetworkManager(), new GameProfile(UUID.randomUUID(), IMinecraft.mc.player.getScoreboardName())) {
            @Override
            public void sendPacket(IPacket<?> packetIn) {
                super.sendPacket(packetIn);
            }
        };

        public ClientFakePlayer(int id) {
            super(IMinecraft.mc, IMinecraft.mc.world, netHandler, IMinecraft.mc.player.getStats(), IMinecraft.mc.player.getRecipeBook(), false, false);

            setEntityId(id);
            movementInput = new MovementInputFromOptions(IMinecraft.mc.gameSettings);
        }

        public void spawn() {
            if (world != null) {
                IMinecraft.mc.player.setMotion(Vector3d.ZERO);
                IMinecraft.mc.player.movementInput = new MovementInput() {
                    @Override
                    public void tickMovement(boolean slowDown) {
                        this.moveStrafe = 0;
                        this.moveForward = 0;
                        this.forwardKeyDown = false;
                        this.backKeyDown = false;
                        this.leftKeyDown = false;
                        this.rightKeyDown = false;
                        this.jump = false;
                        this.sneaking = false;
                    }
                };
                world.addEntity(this);
            }
        }

        @Override
        public void livingTick() {
            IMinecraft.mc.player.setMotion(Vector3d.ZERO);
            IMinecraft.mc.player.movementInput = new MovementInput() {
                @Override
                public void tickMovement(boolean slowDown) {
                    this.moveStrafe = 0;
                    this.moveForward = 0;
                    this.forwardKeyDown = false;
                    this.backKeyDown = false;
                    this.leftKeyDown = false;
                    this.rightKeyDown = false;
                    this.jump = false;
                    this.sneaking = false;
                }
            };
            super.livingTick();
        }

        @Override
        public void rotateTowards(double yaw, double pitch) {
            super.rotateTowards(yaw, pitch);
        }
    }
}
