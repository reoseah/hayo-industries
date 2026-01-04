package io.github.reoseah.hayo.base.client;

import io.github.reoseah.hayo.feature.processing_machines.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class HayoContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public HayoContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    public HayoContainerScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        super(menu, inventory, title, imageWidth, imageHeight);
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
        graphics.blit(RenderPipelines.GUI_TEXTURED, MachineScreen.TEXTURE, this.leftPos, this.topPos, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);
    }
}
