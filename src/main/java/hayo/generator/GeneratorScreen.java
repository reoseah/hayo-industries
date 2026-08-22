package hayo.generator;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

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

        HayoGuiSprites.blitBackground(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        HayoGuiSprites.blitSmallArrowRight(graphics, this.leftPos + 79, this.topPos + 44);

        HayoGuiSprites.blitSlot(graphics, this.leftPos + 61, this.topPos + 53);
        HayoGuiSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);

        HayoGuiSprites.blitFuel(graphics, this.leftPos + 62, this.topPos + 37, this.menu.generatorData.fuelLeft(), this.menu.generatorData.fuelTotal());
        EnergyGuiSprites.extractVerticalBar(graphics, this.leftPos + 88, this.topPos + 16, this.menu.generatorData.energy(), GeneratorBlockEntity.CAPACITY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(88, 16, 18, 56, mouseX, mouseY)) {
            int energy = this.menu.generatorData.energy();
            int capacity = GeneratorBlockEntity.CAPACITY;

            graphics.setTooltipForNextFrame(this.font, List.of(
                    EnergyTexts.amountAndPercentage(energy, capacity),
                    EnergyTexts.maxAmount(capacity).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        }
    }
}
