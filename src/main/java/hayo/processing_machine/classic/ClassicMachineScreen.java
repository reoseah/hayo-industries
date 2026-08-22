package hayo.processing_machine.classic;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class ClassicMachineScreen extends AbstractContainerScreen<ClassicMachineMenu> {
    protected final Identifier arrow;
    protected final Identifier arrowOverlay;

    public ClassicMachineScreen(ClassicMachineMenu menu, Inventory inventory, Component title, Identifier arrow, Identifier arrowOverlay) {
        super(menu, inventory, title);
        this.arrow = arrow;
        this.arrowOverlay = arrowOverlay;
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

        HayoGuiSprites.blitSlot(graphics, this.leftPos + 46, this.topPos + 17);
        HayoGuiSprites.blitSlot(graphics, this.leftPos + 46, this.topPos + 53);
        HayoGuiSprites.blitOutputSlot(graphics, this.leftPos + 103, this.topPos + 32);
        HayoGuiSprites.blitUpgradeSlots(graphics, this.leftPos + 151, this.topPos + 7, 4);
        HayoGuiSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);

        EnergyGuiSprites.blitZap(graphics, this.leftPos + 48, this.topPos + 37, this.menu.machineData.energy(), this.menu.machineData.capacity());
        HayoGuiSprites.blitRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.arrow, this.arrowOverlay, this.menu.machineData.recipeProgress(), this.menu.machineData.recipeCost());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(48, 37, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of(
                    EnergyTexts.amountAndCapacity(this.menu.machineData.energy(), this.menu.machineData.capacity()),
                    Component.translatable("hayo.machine.crafting_speed", this.menu.machineData.energyUseRate()).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        }
    }
}
