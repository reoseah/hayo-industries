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

public abstract class MachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public static final ResourceLocation TEXTURE = Hayo.modLocation("textures/gui/container/machine.png");
    public static final int SLOT_X = 0, SLOT_Y = 166, SLOT_SIZE = 18;
    public static final int VERT_ENERGY_X = 0, VERT_ENERGY_Y = 184, VERT_ENERGY_WIDTH = 18, VERT_ENERGY_HEIGHT = 56, VERT_ENERGY_OVERLAY_X = 18;
    public static final int TINY_ARROW_X = 18, TINY_ARROW_Y = 166, TINY_ARROW_WIDTH = 9, TINY_ARROW_HEIGHT = 18, TINY_ARROW_LEFT_X = 27;
    public static final int FUEL_GAUGE_X = 48, FUEL_GAUGE_Y = 166, FUEL_GAUGE_SIZE = 14, FUEL_GAUGE_OVERLAY_X = 62;

    public MachineScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
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

    protected static void drawSlot(GuiGraphics graphics, int x, int y) {
        blitGuiTexture(graphics, x, y, SLOT_X, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
    }

    protected static void drawFuelMeter(GuiGraphics graphics, int x, int y, int fuelLeft, int fuelTotal) {
        blitGuiTexture(graphics, x, y, FUEL_GAUGE_X, FUEL_GAUGE_Y, FUEL_GAUGE_SIZE, FUEL_GAUGE_SIZE);
        if (fuelLeft > 0 && fuelTotal > 0) {
            var gauge = Mth.clamp(1 + 13 * fuelLeft / fuelTotal, 1, 14);
            blitGuiTexture(graphics, x, y + FUEL_GAUGE_SIZE - 1 - gauge, FUEL_GAUGE_OVERLAY_X, FUEL_GAUGE_Y + FUEL_GAUGE_SIZE - 1 - gauge, FUEL_GAUGE_SIZE, gauge);
        }
    }

    protected static void drawVerticalEnergyBar(GuiGraphics graphics, int x, int y, int amount, int capacity) {
        blitGuiTexture(graphics, x, y, VERT_ENERGY_X, VERT_ENERGY_Y, VERT_ENERGY_WIDTH, VERT_ENERGY_HEIGHT);
        if (amount > 0) {
            var gauge = Mth.clamp(1 + 47 * amount / capacity, 1, 48);
            blitGuiTexture(graphics, x + 4, y + 56 - 4 - gauge, 18, 232 - gauge, 10, gauge);
        }
    }
}
