package io.github.reoseah.hayo.feature.universal_screen;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class UniversalContainerScreen extends AbstractContainerScreen<UniversalContainerMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("background");
    public static final Identifier SLOT = Hayo.modId("slots/default");

    public UniversalContainerScreen(UniversalContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.width, menu.height);

        this.titleLabelX = menu.titleX;
        this.titleLabelY = menu.titleY;
        this.inventoryLabelX = menu.inventoryLabelX;
        this.inventoryLabelY = menu.inventoryLabelY;
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.centerTitle) {
            this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        for (var slot : this.menu.slots) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 18, 18);
        }

        for (var element : this.menu.guiElements) {
            element.render(graphics, this.font, this.leftPos, this.topPos, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (var element : this.menu.guiElements) {
            element.renderTooltip(graphics, this.font, this.leftPos, this.topPos, mouseX, mouseY);
        }

        super.extractTooltip(graphics, mouseX, mouseY);
    }
}
