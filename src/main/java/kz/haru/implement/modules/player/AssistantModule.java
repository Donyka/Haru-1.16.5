package kz.haru.implement.modules.player;

import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BindSetting;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.math.ProjectionUtil;
import kz.haru.common.utils.player.world.InventoryUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.connection.HPacketEvent;
import kz.haru.implement.events.input.ButtonInputEvent;
import kz.haru.implement.events.render.Render2DEvent;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@ModuleRegister(name = "Assistant", category = Category.PLAYER, desc = "Помогает типа")
public class AssistantModule extends Module {
    private final MultiModeSetting functions = new MultiModeSetting("Functions").addValues("Hotkeys", "Timers");
    private final Supplier<Boolean> isHotkeysEnabled = () -> functions.is("Hotkeys");
    private final BooleanSetting trapPlastCombo = new BooleanSetting("Trap and plast").value(true).setVisible(isHotkeysEnabled);
    private final Map<Item, BindSetting> keyBindings = new HashMap<>();
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<Pair<Long, Vector3d>> consumables = new ArrayList<>();
    
    private static class Pair<K, V> {
        private final K first;
        private final V second;
        
        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }
        
        public K getFirst() {
            return first;
        }
        
        public V getSecond() {
            return second;
        }
    }

    public AssistantModule() {
        keyBindings.put(Items.ENDER_EYE, new BindSetting("Disorientation key").value(-1));
        keyBindings.put(Items.NETHERITE_SCRAP, new BindSetting("Trap key").value(-1));
        keyBindings.put(Items.SUGAR, new BindSetting("Clear dust key").value(-1));
        keyBindings.put(Items.FIRE_CHARGE, new BindSetting("Fire whirl key").value(-1));
        keyBindings.put(Items.DRIED_KELP, new BindSetting("Plast key").value(-1));
        keyBindings.put(Items.PHANTOM_MEMBRANE, new BindSetting("Divine aura key").value(-1));
        
        for (BindSetting bindSetting : keyBindings.values()) {
            bindSetting.setVisible(isHotkeysEnabled);
        }
        
        setup(functions, trapPlastCombo, 
              keyBindings.get(Items.ENDER_EYE), 
              keyBindings.get(Items.NETHERITE_SCRAP), 
              keyBindings.get(Items.SUGAR), 
              keyBindings.get(Items.FIRE_CHARGE), 
              keyBindings.get(Items.DRIED_KELP), 
              keyBindings.get(Items.PHANTOM_MEMBRANE));
    }

    // чиста на так подрочить

    @EventTarget
    public void onPacket(HPacketEvent event) {
        if (!functions.is("Timers")) return;

        IPacket<?> packet = event.getPacket();

        if (packet instanceof SPlaySoundEffectPacket) {
            SPlaySoundEffectPacket soundPacket = (SPlaySoundEffectPacket) packet;
            
            if (soundPacket.getSound().getName().getPath().equals("block.piston.contract")) {
                consumables.add(new Pair<>(System.currentTimeMillis() + 15000, Vector3d.copyCentered(new BlockPos(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()))));
            } else if (soundPacket.getSound().getName().getPath().equals("block.anvil.place")) {
                BlockPos soundPos = new BlockPos(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ());
                long delay = 250;
                
                scheduler.schedule(() -> {
                    getCube(soundPos, 4, 4).stream()
                            .filter(pos -> getDistance(soundPos, pos) > 2 && mc.world.getBlockState(pos).getBlock().equals(Blocks.COBBLESTONE))
                            .min(Comparator.comparing(pos -> getDistance(soundPos, pos)))
                            .ifPresent(pos -> {
                                long andesiteCount = getCube(pos, 1, 1).stream()
                                        .filter(pos2 -> mc.world.getBlockState(pos2).getBlock().equals(Blocks.ANDESITE))
                                        .count();
                                
                                if (andesiteCount == 16 || andesiteCount == 9 || andesiteCount == 10) {
                                    int time = andesiteCount == 16 ? 60000 : 20000;
                                    consumables.add(new Pair<>(System.currentTimeMillis() + time - delay, 
                                            Vector3d.copyCentered(pos).add(0, andesiteCount == 16 ? -0.5 : 0, 0)));
                                }
                            });
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!functions.is("Timers")) return;

        consumables.removeIf(cons -> (double) (cons.getFirst() - System.currentTimeMillis()) / 1000 <= 0);
        
        for (Pair<Long, Vector3d> cons : consumables) {
            Vector3d position = cons.getSecond();
            Vector2f screenPos = ProjectionUtil.project(position);

            double time = round((double) (cons.getFirst() - System.currentTimeMillis()) / 1000, 1);
            String text = time + "s";
            float scale = UpdateFunctions.getInstance().getScaleFactor();
            float size = 7f * scale;

            float posX = screenPos.x - (Fonts.regular.getWidth(text, size) / 2f);
            float posY = screenPos.y;

            RenderUtil.drawRound(posX - 1, posY - 1, Fonts.regular.getWidth(text, size) + 2, Fonts.regular.getHeight(size) + 2, 2f * scale, 0x80000000);
            Fonts.regular.drawText(event.getMatrixStack(), text, posX, posY, -1, size);
        }
    }

    @EventTarget
    public void onButtonInput(ButtonInputEvent event) {
        if (!isHotkeysEnabled.get() || mc.currentScreen != null) return;

        for (Map.Entry<Item, BindSetting> entry : keyBindings.entrySet()) {
            if (event.getButton() == entry.getValue().getValue() && event.getAction() != 0) {
                throwItem(entry.getKey());
                if (entry.getKey() == Items.NETHERITE_SCRAP && trapPlastCombo.getValue() && 
                        InventoryUtil.findItem(entry.getKey(), false) != -1 && !mc.player.getCooldownTracker().hasCooldown(entry.getKey())) {
                    scheduler.schedule(() -> throwItem(Items.DRIED_KELP, mc.player.rotationYaw, -90), 750, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private void throwItem(Item item) {
        throwItem(item, mc.player.rotationYaw, mc.player.rotationPitch);
    }

    private void throwItem(Item item, float yaw, float pitch) {
        int slot = InventoryUtil.findItem(item, false);
        if (slot == -1) return;
        
        float oldYaw = mc.player.rotationYaw;
        float oldPitch = mc.player.rotationPitch;
        
        mc.player.rotationYaw = yaw;
        mc.player.rotationPitch = pitch;
        
        if (mc.player.getHeldItemOffhand().getItem() == item) {
            InventoryUtil.useItem(Hand.OFF_HAND);
        } else if (mc.player.getHeldItemMainhand().getItem() == item) {
            InventoryUtil.useItem(Hand.MAIN_HAND);
        } else {
            int invSlot = InventoryUtil.findItem(item, false);
            int hbSlot = InventoryUtil.findItem(item, true);
            int prevSlot = mc.player.inventory.currentItem;

            int toSlot = InventoryUtil.findEmptySlot();
            if (toSlot == -1) {
                toSlot = InventoryUtil.findBestSlotInHotBar();
            }
            
            if (hbSlot != -1) {
                InventoryUtil.swapToSlot(hbSlot);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                InventoryUtil.swapToSlot(prevSlot);
            } else {
                InventoryUtil.swapSlots(invSlot, toSlot);
                InventoryUtil.swapToSlot(toSlot);
                InventoryUtil.useItem(Hand.MAIN_HAND);
                InventoryUtil.swapToSlot(mc.player.inventory.currentItem);
                InventoryUtil.swapSlots(toSlot, invSlot);
            }
        }
        
        mc.player.rotationYaw = oldYaw;
        mc.player.rotationPitch = oldPitch;
    }

    private double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private double getDistance(BlockPos pos1, BlockPos pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double dz = pos1.getZ() - pos2.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private List<BlockPos> getCube(BlockPos center, int xRadius, int yRadius) {
        List<BlockPos> sphere = new ArrayList<>();
        for (int x = -xRadius; x <= xRadius; x++) {
            for (int y = -yRadius; y <= yRadius; y++) {
                for (int z = -xRadius; z <= xRadius; z++) {
                    sphere.add(center.add(x, y, z));
                }
            }
        }
        return sphere;
    }
}
