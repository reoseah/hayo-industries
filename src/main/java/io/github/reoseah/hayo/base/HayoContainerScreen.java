package io.github.reoseah.hayo.base;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class HayoContainerScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("background");

    public HayoContainerScreen(AbstractContainerMenu menu, Inventory inventory, Component title) {
        var hayoMenu = (HayoContainerMenu) menu;
        super(hayoMenu, inventory, title, hayoMenu.width, hayoMenu.height);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }
}
