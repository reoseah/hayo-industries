package io.github.reoseah.hayo.feature.battery_box;

import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class BatteryBoxScreen extends HayoContainerScreen<BatteryBoxMenu> {
    public BatteryBoxScreen(BatteryBoxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        for (int i = 0; i < 6; i++) {
            HayoGuiSprites.drawSlot(graphics, x + this.menu.slots.get(i).x - 1, y + this.menu.slots.get(i).y - 1);
        }

        HayoGuiSprites.drawEnergyStorage(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HayoGuiSprites.SLOT_CONNECTION_9_WIDE, x + 78, y + 34, 11, 2);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HayoGuiSprites.SLOT_CONNECTION_9_WIDE, x + 78, y + 52, 11, 2);

        HayoGuiSprites.drawOutputSlot(graphics, x + this.menu.slots.get(6).x - 4, y + this.menu.slots.get(6).y - 4);
        HayoGuiSprites.drawSmallArrowRight(graphics, x + this.menu.slots.get(6).x - 16, y + this.menu.slots.get(6).y);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.isHovering(89, 17, 17, 55, mouseX, mouseY)) {
            int storedEnergy = this.menu.getStoredEnergy();
            int capacity = this.menu.getEnergyCapacity();
            float averageInput = this.menu.getAverageInput();
            float averageOutput = this.menu.getAverageOutput();

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
