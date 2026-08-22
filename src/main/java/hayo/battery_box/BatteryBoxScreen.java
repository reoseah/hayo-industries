package hayo.battery_box;

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

public class BatteryBoxScreen extends AbstractContainerScreen<BatteryBoxMenu> {
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

        HayoGuiSprites.blitBackground(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        HayoGuiSprites.blitSmallArrowRight(graphics, this.leftPos + 108, this.topPos + 36);

        HayoGuiSprites.blitSlots(graphics, this.leftPos + 25, this.topPos + 26, 3, 2);
        HayoGuiSprites.blitOutputSlot(graphics, this.leftPos + 120, this.topPos + 32);
        HayoGuiSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);
        HayoGuiSprites.blitSlotConnection9Wide(graphics, this.leftPos + 78, this.topPos + 34);
        HayoGuiSprites.blitSlotConnection9Wide(graphics, this.leftPos + 78, this.topPos + 52);

        EnergyGuiSprites.extractVerticalBar(graphics, this.leftPos + 88, this.topPos + 16, this.menu.batteryBoxData.energy(), this.menu.batteryBoxData.capacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(89, 17, 17, 55, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of(
                    EnergyTexts.amountAndPercentage(this.menu.batteryBoxData.energy(), this.menu.batteryBoxData.capacity()),
                    EnergyTexts.maxAmount(this.menu.batteryBoxData.capacity()).withStyle(ChatFormatting.GRAY),
                    EnergyTexts.averageInput(this.menu.batteryBoxData.averageInput()).withStyle(ChatFormatting.GRAY),
                    EnergyTexts.averageOutput(this.menu.batteryBoxData.averageOutput()).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        }
    }
}
