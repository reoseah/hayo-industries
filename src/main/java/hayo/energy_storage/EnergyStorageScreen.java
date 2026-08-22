package hayo.energy_storage;

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

public class EnergyStorageScreen extends AbstractContainerScreen<EnergyStorageMenu> {
    public EnergyStorageScreen(EnergyStorageMenu menu, Inventory inventory, Component title) {
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
        HayoGuiSprites.blitSmallArrowRight(graphics, this.leftPos + 79, this.topPos + 17);
        HayoGuiSprites.blitSmallArrowLeft(graphics, this.leftPos + 79, this.topPos + 53);

        HayoGuiSprites.blitSlot(graphics, this.leftPos + 61, this.topPos + 17);
        HayoGuiSprites.blitSlot(graphics, this.leftPos + 61, this.topPos + 53);
        HayoGuiSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);

        EnergyGuiSprites.extractVerticalBar(graphics, this.leftPos + 88, this.topPos + 16, this.menu.storageData.energy(), this.menu.storageData.capacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(89, 17, 17, 55, mouseX, mouseY)) {
            int storedEnergy = this.menu.storageData.energy();
            int capacity = this.menu.storageData.capacity();
            float averageInput = this.menu.storageData.averageInput();
            float averageOutput = this.menu.storageData.averageOutput();

            graphics.setTooltipForNextFrame(this.font, List.of(
                    EnergyTexts.amountAndPercentage(storedEnergy, capacity),
                    EnergyTexts.maxAmount(capacity).withStyle(ChatFormatting.GRAY),
                    EnergyTexts.averageInput(averageInput).withStyle(ChatFormatting.GRAY),
                    EnergyTexts.averageOutput(averageOutput).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        }
    }
}
