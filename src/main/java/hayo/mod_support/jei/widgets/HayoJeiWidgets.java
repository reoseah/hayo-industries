package hayo.mod_support.jei.widgets;

import hayo.common.HayoSprites;
import hayo.common.ItemTemplateWithChance;
import hayo.common.StringUtils;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class HayoJeiWidgets {
    public static IDrawable machineWithUpgrade(IGuiHelper guiHelper, ItemLike machine, ItemLike upgrade) {
        var machineDrawable = guiHelper.createDrawableItemLike(machine);
        var upgradeDrawable = guiHelper.createDrawableItemLike(upgrade);

        return new IDrawable() {
            @Override
            public int getWidth() {
                return 16;
            }

            @Override
            public int getHeight() {
                return 16;
            }

            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                machineDrawable.draw(graphics, xOffset, yOffset);

                var poseStack = graphics.pose();
                poseStack.pushMatrix();
                {
                    poseStack.translate(8 + xOffset, 8 + yOffset);
                    poseStack.scale(0.5f, 0.5f);
                    upgradeDrawable.draw(graphics);
                }
                poseStack.popMatrix();
            }
        };
    }

    public static IDrawable itemWithChance(ItemTemplateWithChance templateWithChance) {
        return itemWithChance(templateWithChance.template().create(), templateWithChance.chance());
    }

    public static IDrawable itemWithChance(ItemStack stack, float chance) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return 18;
            }

            @Override
            public int getHeight() {
                return 18;
            }

            @Override
            public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
                HayoSprites.blitSlot(graphics, xOffset, yOffset);

                graphics.fakeItem(stack, xOffset + 1, yOffset + 1);

                var font = Minecraft.getInstance().font;
                var subscriptChance = Component.translatable("hayo.subscript_chance", StringUtils.toSubscriptDigits(chance));
                int chanceWidth = font.width(subscriptChance);
                graphics.text(font, subscriptChance, xOffset + 18 - chanceWidth, yOffset + 18 - font.lineHeight, 0xFFFFFFFF, true);
            }
        };
    }
}
