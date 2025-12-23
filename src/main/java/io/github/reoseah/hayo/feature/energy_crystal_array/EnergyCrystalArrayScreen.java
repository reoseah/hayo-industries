package io.github.reoseah.hayo.feature.energy_crystal_array;

import io.github.reoseah.hayo.api.energy.EnergyTexts;
import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoMachineTexture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class EnergyCrystalArrayScreen extends HayoContainerScreen<EnergyCrystalArrayMenu> {
    public EnergyCrystalArrayScreen(EnergyCrystalArrayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        int x = this.leftPos;
        int y = this.topPos;

        HayoMachineTexture.drawSlot(graphics, x + 61, y + 17);
        HayoMachineTexture.blit(graphics, x + 79, y + 17, HayoMachineTexture.TINY_ARROW_X, HayoMachineTexture.TINY_ARROW_Y, HayoMachineTexture.TINY_ARROW_WIDTH, HayoMachineTexture.TINY_ARROW_HEIGHT);

        HayoMachineTexture.drawSlot(graphics, x + 61, y + 53);
        HayoMachineTexture.blit(graphics, x + 79, y + 53, HayoMachineTexture.TINY_ARROW_LEFT_X, HayoMachineTexture.TINY_ARROW_Y, HayoMachineTexture.TINY_ARROW_WIDTH, HayoMachineTexture.TINY_ARROW_HEIGHT);

        HayoMachineTexture.drawEnergyStorage(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), EnergyCrystalArrayBlockEntity.CAPACITY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(88, 16, HayoMachineTexture.ENERGY_WIDTH, HayoMachineTexture.ENERGY_HEIGHT, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.STORED_ENERGY, //
                    EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), EnergyCrystalArrayBlockEntity.CAPACITY).withStyle(ChatFormatting.GRAY), //
                    EnergyTexts.averageAmountPerTick(this.menu.getAverageEnergyPerTick()).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
