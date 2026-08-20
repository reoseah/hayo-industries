package hayo.generator;

import hayo.common.HayoGuiSprites;
import hayo.energy.client.EnergySprites;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        HayoGuiSprites.extractBackground(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        HayoGuiSprites.extractSlot(graphics, this.leftPos + 61, this.topPos + 53);
        HayoGuiSprites.extractStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);
        HayoGuiSprites.extractFuel(graphics, this.leftPos + 62, this.topPos + 37, this.menu.generatorData.fuelLeft(), this.menu.generatorData.fuelTotal());
        HayoGuiSprites.extractSmallArrowRight(graphics, this.leftPos + 79, this.topPos + 44);
        EnergySprites.extractVerticalBar(graphics, this.leftPos + 88, this.topPos + 16, this.menu.generatorData.energy(), GeneratorBlockEntity.CAPACITY);
    }
}
