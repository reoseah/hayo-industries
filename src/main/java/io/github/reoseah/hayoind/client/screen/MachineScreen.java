package io.github.reoseah.hayoind.client.screen;

import io.github.reoseah.hayoind.Hayo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public abstract class MachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public static final ResourceLocation TEXTURE = Hayo.modLocation("textures/gui/container/machine.png");

    public static final int SLOT_X = 0, SLOT_Y = 192, SLOT_SIZE = 18;
    public static final int OUTPUT_SLOT_X = 0, OUTPUT_SLOT_Y = 166, OUTPUT_SLOT_SIZE = 26;
    public static final int UPGRADE_SLOT_X = 0, UPGRADE_SLOT_Y = 210, UPGRADE_SLOT_SIZE = 18;

    public static final int ENERGY_X = 30, ENERGY_Y = 166, ENERGY_WIDTH = 18, ENERGY_HEIGHT = 56;
    public static final int ENERGY_OVERLAY_X = 18, ENERGY_OVERLAY_Y = 192, ENERGY_OVERLAY_WIDTH = 10, ENERGY_OVERLAY_HEIGHT = 48;

    public static final int TINY_ARROW_X = 0, TINY_ARROW_Y = 228, TINY_ARROW_WIDTH = 9, TINY_ARROW_HEIGHT = 18, TINY_ARROW_LEFT_X = 9;

    public static final int FUEL_X = 48, FUEL_Y = 166, FUEL_SIZE = 14, FUEL_OVERLAY_X = 62;
    public static final int ZAP_X = 48, ZAP_Y = 180, ZAP_SIZE = 14, ZAP_OVERLAY_X = 62;

    public MachineScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        blitGuiTexture(graphics, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected static void blitGuiTexture(GuiGraphics graphics, int x, int y, float u, float v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, width, height, 256, 256);
    }

    protected void drawSlot(GuiGraphics graphics, Slot slot) {
        drawSlot(graphics, this.leftPos + slot.x - 1, this.topPos + slot.y - 1);
    }

    protected static void drawSlot(GuiGraphics graphics, int x, int y) {
        blitGuiTexture(graphics, x, y, SLOT_X, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
    }

    protected void drawOutputSlot(GuiGraphics graphics, Slot slot) {
        drawOutputSlot(graphics, this.leftPos + slot.x - 5, this.topPos + slot.y - 5);
    }

    protected static void drawOutputSlot(GuiGraphics graphics, int x, int y) {
        blitGuiTexture(graphics, x, y, OUTPUT_SLOT_X, OUTPUT_SLOT_Y, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE);
    }

    protected void drawUpgradeSlot(GuiGraphics graphics, Slot slot) {
        drawUpgradeSlot(graphics, this.leftPos + slot.x - 1, this.topPos + slot.y - 1);
    }

    protected static void drawUpgradeSlot(GuiGraphics graphics, int x, int y) {
        blitGuiTexture(graphics, x, y, UPGRADE_SLOT_X, UPGRADE_SLOT_Y, UPGRADE_SLOT_SIZE, UPGRADE_SLOT_SIZE);
    }

    protected static void drawFuelMeter(GuiGraphics graphics, int x, int y, int fuelLeft, int fuelTotal) {
        blitGuiTexture(graphics, x, y, FUEL_X, FUEL_Y, FUEL_SIZE, FUEL_SIZE);
        if (fuelLeft > 0 && fuelTotal > 0) {
            var height = Mth.clamp(1 + (FUEL_SIZE - 1) * fuelLeft / fuelTotal, 1, FUEL_SIZE);
            blitGuiTexture(graphics, x, y + FUEL_SIZE - 1 - height, FUEL_OVERLAY_X, FUEL_Y + FUEL_SIZE - 1 - height, FUEL_SIZE, height);
        }
    }

    protected static void drawZapMeter(GuiGraphics graphics, int x, int y, int energy, int capacity) {
        blitGuiTexture(graphics, x, y, ZAP_X, ZAP_Y, ZAP_SIZE, ZAP_SIZE);
        if (energy > 0 && capacity > 0) {
            var height = Mth.clamp(1 + (ZAP_SIZE - 1) * energy / capacity, 1, ZAP_SIZE);
            blitGuiTexture(graphics, x, y + ZAP_SIZE - height, ZAP_OVERLAY_X, ZAP_Y + ZAP_SIZE - height, ZAP_SIZE, height);
        }
    }

    protected static void drawEnergyBar(GuiGraphics graphics, int x, int y, int amount, int capacity) {
        blitGuiTexture(graphics, x, y, ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT);
        if (amount > 0) {
            var gauge = Mth.clamp(1 + 47 * amount / capacity, 1, ENERGY_OVERLAY_HEIGHT);
            blitGuiTexture(graphics, x + 4, y + 4 + ENERGY_OVERLAY_HEIGHT - gauge, ENERGY_OVERLAY_X, ENERGY_OVERLAY_Y + ENERGY_OVERLAY_HEIGHT - gauge, ENERGY_OVERLAY_WIDTH, gauge);
        }
    }
}
