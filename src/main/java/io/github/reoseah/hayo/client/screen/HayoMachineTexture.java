package io.github.reoseah.hayo.client.screen;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;

public class HayoMachineTexture {
    public static final ResourceLocation LOCATION = Hayo.modLocation("textures/gui/container/machine.png");

    public static final int SLOT_X = 0, SLOT_Y = 192, SLOT_SIZE = 18;
    public static final int OUTPUT_SLOT_X = 0, OUTPUT_SLOT_Y = 166, OUTPUT_SLOT_SIZE = 26;
    public static final int UPGRADE_SLOT_X = 0, UPGRADE_SLOT_Y = 210, UPGRADE_SLOT_SIZE = 18;

    public static final int FUEL_X = 48, FUEL_Y = 166, FUEL_SIZE = 14, FUEL_OVERLAY_X = 62;
    public static final int ZAP_X = 48, ZAP_Y = 180, ZAP_SIZE = 14, ZAP_OVERLAY_X = 62;

    public static final int ENERGY_X = 30, ENERGY_Y = 166, ENERGY_WIDTH = 18, ENERGY_HEIGHT = 56;
    public static final int ENERGY_OVERLAY_X = 18, ENERGY_OVERLAY_Y = 192, ENERGY_OVERLAY_WIDTH = 10, ENERGY_OVERLAY_HEIGHT = 48, ENERGY_OVERLAY_OFFSET = 4;

    public static final int TINY_ARROW_X = 0, TINY_ARROW_Y = 228, TINY_ARROW_WIDTH = 9, TINY_ARROW_HEIGHT = 18;
    public static final int TINY_ARROW_LEFT_X = 9;

    public static void blit(GuiGraphics graphics, int x, int y, float u, float v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, x, y, u, v, width, height, 256, 256);
    }

    public static void drawSlot(GuiGraphics graphics, int leftPos, int topPos, Slot slot) {
        drawSlot(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
    }

    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        blit(graphics, x, y, SLOT_X, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
    }

    public static void drawOutputSlot(GuiGraphics graphics, int leftPos, int topPos, Slot slot) {
        drawOutputSlot(graphics, leftPos + slot.x - 5, topPos + slot.y - 5);
    }

    public static void drawOutputSlot(GuiGraphics graphics, int x, int y) {
        blit(graphics, x, y, OUTPUT_SLOT_X, OUTPUT_SLOT_Y, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE);
    }

    public static void drawUpgradeSlot(GuiGraphics graphics, int leftPos, int topPos, Slot slot) {
        drawUpgradeSlot(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
    }

    public static void drawUpgradeSlot(GuiGraphics graphics, int x, int y) {
        blit(graphics, x, y, UPGRADE_SLOT_X, UPGRADE_SLOT_Y, UPGRADE_SLOT_SIZE, UPGRADE_SLOT_SIZE);
    }

    public static void drawFuelMeter(GuiGraphics graphics, int x, int y, int fuelLeft, int fuelTotal) {
        blit(graphics, x, y, FUEL_X, FUEL_Y, FUEL_SIZE, FUEL_SIZE);

        if (fuelLeft > 0 && fuelTotal > 0) {
            var height = Mth.clamp(1 + (FUEL_SIZE - 1) * fuelLeft / fuelTotal, 1, FUEL_SIZE);
            blit(graphics, x, y + FUEL_SIZE - 1 - height, FUEL_OVERLAY_X, FUEL_Y + FUEL_SIZE - 1 - height, FUEL_SIZE, height);
        }
    }

    public static void drawZapMeter(GuiGraphics graphics, int x, int y, int energy, int capacity) {
        blit(graphics, x, y, ZAP_X, ZAP_Y, ZAP_SIZE, ZAP_SIZE);

        if (energy > 0 && capacity > 0) {
            var height = Mth.clamp(1 + (ZAP_SIZE - 1) * energy / capacity, 1, ZAP_SIZE);
            blit(graphics, x, y + ZAP_SIZE - height, ZAP_OVERLAY_X, ZAP_Y + ZAP_SIZE - height, ZAP_SIZE, height);
        }
    }

    public static void drawEnergyStorage(GuiGraphics graphics, int x, int y, int amount, int capacity) {
        blit(graphics, x, y, ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT);

        if (amount > 0 && capacity > 0) {
            var height = Mth.clamp(1 + (ENERGY_OVERLAY_HEIGHT - 1) * amount / capacity, 1, ENERGY_OVERLAY_HEIGHT);
            blit(graphics, x + ENERGY_OVERLAY_OFFSET, y + ENERGY_OVERLAY_OFFSET + ENERGY_OVERLAY_HEIGHT - height, ENERGY_OVERLAY_X, ENERGY_OVERLAY_Y + ENERGY_OVERLAY_HEIGHT - height, ENERGY_OVERLAY_WIDTH, height);
        }
    }

    public enum RecipeArrow {
        DEFAULT(166),
        MACERATOR(182),
        COMPRESSOR(198),
        EXTRACTOR(214);

        public static final int X = 80, OVERLAY_X = 104, HEIGHT = 16, WIDTH = 24;

        public final int y;

        RecipeArrow(int y) {
            this.y = y;
        }
    }

    public static void drawRecipeArrow(GuiGraphics graphics, int x, int y, RecipeArrow arrowType, int currentProgress, int totalProgress) {
        blit(graphics, x, y, RecipeArrow.X, arrowType.y, RecipeArrow.WIDTH, RecipeArrow.HEIGHT);

        if (currentProgress > 0 && totalProgress > 0) {
            int length = Mth.clamp(1 + RecipeArrow.WIDTH * currentProgress / totalProgress, 0, RecipeArrow.WIDTH);
            blit(graphics, x, y, RecipeArrow.OVERLAY_X, arrowType.y, length, RecipeArrow.HEIGHT);
        }
    }
}
