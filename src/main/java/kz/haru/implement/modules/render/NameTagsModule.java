package kz.haru.implement.modules.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.api.event.EventTarget;
import kz.haru.api.module.Category;
import kz.haru.api.module.Module;
import kz.haru.api.module.ModuleRegister;
import kz.haru.api.module.setting.settings.BooleanSetting;
import kz.haru.api.module.setting.settings.MultiModeSetting;
import kz.haru.api.system.friends.FriendManager;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.math.MathUtil;
import kz.haru.common.utils.math.ProjectionUtil;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.implement.events.render.Render2DEvent;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ModuleRegister(name = "Name Tags", category = Category.RENDER, desc = "Выводит информацию о сущности")
public class NameTagsModule extends Module {
    private final MultiModeSetting targets = new MultiModeSetting("Targets").select("Players", "Self").addValues("Players", "Self", "Mobs", "Animals");
    private final MultiModeSetting elements = new MultiModeSetting("Elements").select("Items").addValues("Items", "Potions");
    private final BooleanSetting enchants = new BooleanSetting("Enchants").value(false).setVisible(() -> elements.is("Items"));

    public NameTagsModule() {
        setup(targets, elements, enchants);
    }
    
    public static NameTagsModule get() {
        return Module.get(NameTagsModule.class);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (mc.player == null || mc.world == null || event.isSecondLayer()) return;
        for (Entity entity : mc.world.getAllEntities()) {
            if (
                entity == mc.player && mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON && targets.is("Self") ||
                entity instanceof PlayerEntity && entity != mc.player && targets.is("Players") ||
                entity instanceof MobEntity && targets.is("Mobs") ||
                entity instanceof AnimalEntity && targets.is("Animals")
            ) {
                double x = MathUtil.interpolate(entity.prevPosX, entity.getPosX());
                double y = MathUtil.interpolate(entity.prevPosY, entity.getPosY());
                double z = MathUtil.interpolate(entity.prevPosZ, entity.getPosZ());

                AxisAlignedBB box = entity.getBoundingBox();
                double sizeX = box.maxX - box.minX;
                double sizeY = box.maxY - box.minY;
                double sizeZ = box.maxZ - box.minZ;

                AxisAlignedBB aabb = new AxisAlignedBB(
                        x - sizeX / 2,
                        y,
                        z - sizeZ / 2,
                        x + sizeX / 2,
                        y + sizeY,
                        z + sizeZ / 2
                );

                float minX = Float.MAX_VALUE;
                float minY = Float.MAX_VALUE;
                float maxX = Float.MIN_VALUE;
                float maxY = Float.MIN_VALUE;

                for (int i = 0; i < 8; i++) {
                    double cornerX = (i % 2 == 0) ? aabb.minX : aabb.maxX;
                    double cornerY = ((i / 2) % 2 == 0) ? aabb.minY : aabb.maxY;
                    double cornerZ = ((i / 4) % 2 == 0) ? aabb.minZ : aabb.maxZ;

                    Vector3d cornerVec = new Vector3d(cornerX, cornerY, cornerZ);
                    Vector2f projected = ProjectionUtil.project(cornerVec);

                    minX = Math.min(minX, projected.x);
                    minY = Math.min(minY, projected.y);
                    maxX = Math.max(maxX, projected.x);
                    maxY = Math.max(maxY, projected.y);
                }

                float nameTagX = (minX + maxX) / 2;
                float nameTagY = minY - 15f;

                renderTag(event.getMatrixStack(), nameTagX, nameTagY, entity);
            }
        }
    }

    private void renderTag(MatrixStack ms, float x, float y, Entity entity) {
        ITextComponent text = entity.getName();

        if (entity instanceof LivingEntity living) {
            int totalHealth = (int) (living.getHealth() + living.getAbsorptionAmount());
            ITextComponent healthText = new StringTextComponent(" [" + totalHealth + "HP]");

            ITextComponent name = (living.getCustomName() != null ? living.getCustomName() : living.getDisplayName());

            text = new StringTextComponent("").append(name).append(healthText);

            if (elements.is("Items")) {
                renderItems(ms, living, (int) x, (int) y);
            }
        }

        int color = !FriendManager.isFriend(entity.getName().getString()) ? ColorUtil.rgb(11, 11, 11, 190) : ColorUtil.rgb(104, 184, 92, 140);
        float gap = 2f;
        float size = 8f;
        float textWidth = Fonts.medium.getWidth(text, size);
        RenderUtil.drawRound(x - textWidth / 2f - gap, y - gap, textWidth + gap * 2f, size + gap * 2f, 1f, color);
        Fonts.medium.drawText(ms, text, x - textWidth / 2f, y, size, 255);
    }

    private void renderItems(MatrixStack matrixStack, LivingEntity entity, int x, int y) {
        int itemSize = 8;
        int itemPadding = 9;
        float fontScale = 6.5f;

        float fontHeight = Fonts.bold.getHeight(fontScale);

        List<ItemStack> itemsToRender = new ArrayList<>();

        ItemStack mainHandItem = entity.getHeldItemMainhand();
        if (!mainHandItem.isEmpty()) {
            itemsToRender.add(mainHandItem);
        }

        for (ItemStack armorItem : entity.getArmorInventoryList()) {
            if (!armorItem.isEmpty()) {
                itemsToRender.add(armorItem);
            }
        }

        ItemStack offHandItem = entity.getHeldItemOffhand();
        if (!offHandItem.isEmpty()) {
            itemsToRender.add(offHandItem);
        }

        int totalWidth = (itemsToRender.size() * (itemSize + itemPadding));
        float gap = 1.5f;
        x -= totalWidth / 2;
        y -= 5;

        if (!itemsToRender.isEmpty())
            RenderUtil.drawRound(x - gap, y - 15 - gap, totalWidth + gap * 2f - 1f, itemSize * 2f + gap * 2f, 1f, ColorUtil.rgb(11, 11, 11, 190));

        for (ItemStack item : itemsToRender) {
            if (item.isEmpty()) continue;

            mc.getItemRenderer().renderItemAndEffectIntoGUI(item, x, y - 15);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, item, x, y - 15, null);

            if (item.isEnchanted() && enchants.getValue()) {
                int enchantTextY = (int) (y - fontHeight - 18);

                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(item);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int enchantmentLevel = entry.getValue();

                    if (enchantmentLevel < 1 || !enchantment.canApply(item)) continue;

                    String enchantmentKey = Registry.ENCHANTMENT.getKey(enchantment).toString();
                    String enchantmentName = enchantmentKey.substring(enchantmentKey.indexOf(":") + 1);
                    String shortEnchantmentName = enchantmentName.substring(0, 1).toUpperCase() + (enchantmentName.length() > 1 ? enchantmentName.substring(1, 2) : "");
                    String enchantmentText = shortEnchantmentName + enchantmentLevel;

                    int color = -1;
                    if (shortEnchantmentName.contains("Pr") && enchantmentLevel >= 5) color = ColorUtil.rgb(255, 84, 84);
                    if (shortEnchantmentName.contains("Sh") && enchantmentLevel >= 6) color = ColorUtil.rgb(255, 84, 84);
                    if (shortEnchantmentName.contains("Va")) color = ColorUtil.rgb(200, 30, 30);

                    Fonts.bold.drawText(matrixStack, enchantmentText, x, enchantTextY, color, fontScale);

                    enchantTextY -= (int) fontHeight;
                }
            }

            x += itemSize + itemPadding;
        }
    }
}