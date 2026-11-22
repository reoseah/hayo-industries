package hayo.features.geothermal_generator;

import hayo.common.HayoSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.HayoEnergySprites;
import hayo.features.fluid_stack.HayoFluidRendering;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;

import static hayo.features.geothermal_generator.GeothermalGeneratorBlockEntity.FLUID_CAPACITY;

public class GeothermalGeneratorScreen extends AbstractContainerScreen<GeothermalGeneratorMenu> {
    public GeothermalGeneratorScreen(GeothermalGeneratorMenu menu, Inventory inventory, Component title) {
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

        HayoSprites.blitBackground(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        HayoSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);
        HayoSprites.blitSlot(graphics, this.leftPos + 44, this.topPos + 16);
        HayoSprites.blitSlot(graphics, this.leftPos + 44, this.topPos + 52);

        HayoSprites.blitDrainingArrow(graphics, this.leftPos + 49, this.topPos + 36, this.menu.generatorData.drainingProgress(), this.menu.generatorData.drainingMaxProgress());
        HayoFluidRendering.extractFluidTank(graphics, this.menu.generatorData.fluid(), FLUID_CAPACITY, this.leftPos + 70, this.topPos + 15);
        HayoSprites.blitSmallArrowRight(graphics, this.leftPos + 92, this.topPos + 33);

        HayoEnergySprites.extractVerticalBar(graphics, this.leftPos + 106, this.topPos + 16, this.menu.generatorData.energy(), GeothermalGeneratorBlockEntity.CAPACITY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.isHovering(70, 15, 18, 56, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, HayoFluidRendering.createTooltip(this.menu.generatorData.fluid(), GeothermalGeneratorBlockEntity.FLUID_CAPACITY, Fluids.LAVA), Optional.empty(), mouseX, mouseY);
        } else if (this.isHovering(106, 16, 18, 56, mouseX, mouseY)) {
            int energy = this.menu.generatorData.energy();
            int capacity = GeothermalGeneratorBlockEntity.CAPACITY;
            float averageOutput = this.menu.generatorData.averageOutput();

            graphics.setTooltipForNextFrame(this.font, List.of(
                    EnergyTexts.amountAndPercentage(energy, capacity),
                    EnergyTexts.maxAmount(capacity).withStyle(ChatFormatting.GRAY),
                    EnergyTexts.averageOutput(averageOutput).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        } else {
            super.extractTooltip(graphics, mouseX, mouseY);
        }
    }
}
