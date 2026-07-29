package io.github.reoseah.hayo.feature.battery_box;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.machines.ClassicMachineScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class BatteryBoxScreen extends AbstractContainerScreen<BatteryBoxMenu> {
    public static final Identifier SLOT_CONNECTION_9_WIDE = Hayo.modId("slot_connection_9_wide");

    public BatteryBoxScreen(BatteryBoxMenu menu, Inventory inventory, Component title) {
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
        graphics.blit(RenderPipelines.GUI_TEXTURED, ClassicMachineScreen.BACKGROUND, this.leftPos, this.topPos, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);

        int x = this.leftPos;
        int y = this.topPos;

        for (int i = 0; i < 6; i++) {
            HayoGuiSprites.drawSlot(graphics, x + this.menu.slots.get(i).x - 1, y + this.menu.slots.get(i).y - 1);
        }

        EnergyGuiSprites.energyLarge(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_CONNECTION_9_WIDE, x + 78, y + 34, 11, 2);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_CONNECTION_9_WIDE, x + 78, y + 52, 11, 2);

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
