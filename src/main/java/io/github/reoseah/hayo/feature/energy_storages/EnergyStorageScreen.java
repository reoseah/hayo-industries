package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class EnergyStorageScreen extends HayoContainerScreen<EnergyStorageMenu> {
    public EnergyStorageScreen(EnergyStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        HayoGuiSprites.drawSlot(graphics, x + 61, y + 17);
        HayoGuiSprites.drawSmallArrowRight(graphics, x + 79, y + 17);

        HayoGuiSprites.drawSlot(graphics, x + 61, y + 53);
        HayoGuiSprites.drawSmallArrowLeft(graphics, x + 79, y + 53);

        HayoGuiSprites.drawEnergyStorage(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.isHovering(89, 17, 17, 55, mouseX, mouseY)) {
            int storedEnergy = this.menu.getStoredEnergy();
            int capacity = this.menu.getEnergyCapacity();
            float averageInput = this.menu.getAverageInputPerTick();
            float averageOutput = this.menu.getAverageOutputPerTick();

            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.amountAndPercentage(storedEnergy, capacity), //
                    EnergyTexts.maxAmount(capacity).withStyle(ChatFormatting.GRAY), //
                    EnergyTexts.averageInputPerTick(averageInput).withStyle(ChatFormatting.GRAY), //
                    EnergyTexts.averageOutputPerTick(averageOutput).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.extractTooltip(graphics, mouseX, mouseY);
    }
}
