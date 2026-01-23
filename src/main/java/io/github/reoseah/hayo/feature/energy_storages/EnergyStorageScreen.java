package io.github.reoseah.hayo.feature.energy_storages;

import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class EnergyStorageScreen extends HayoContainerScreen<EnergyStorageMenu> {
    public EnergyStorageScreen(EnergyStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        int x = this.leftPos;
        int y = this.topPos;

        HayoGuiSprites.drawSlot(graphics, x + 61, y + 17);
        HayoGuiSprites.drawSmallArrowRight(graphics, x + 79, y + 17);

        HayoGuiSprites.drawSlot(graphics, x + 61, y + 53);
        HayoGuiSprites.drawSmallArrowLeft(graphics, x + 79, y + 53);

        HayoGuiSprites.drawEnergyStorage(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
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

        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
