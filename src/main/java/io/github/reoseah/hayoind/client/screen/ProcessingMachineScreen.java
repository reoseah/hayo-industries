package io.github.reoseah.hayoind.client.screen;

import io.github.reoseah.hayoind.block.entity.ElectricFurnaceBlockEntity;
import io.github.reoseah.hayoind.block.entity.MaceratorBlockEntity;
import io.github.reoseah.hayoind.menu.ProcessingMachineMenu;
import io.github.reoseah.hayoind.menu.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public abstract class ProcessingMachineScreen extends HayoContainerScreen<ProcessingMachineMenu> {
    public ProcessingMachineScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        HayoMachineTexture.drawSlot(graphics, this.leftPos, this.topPos, this.menu.slots.get(0));
        HayoMachineTexture.drawSlot(graphics, this.leftPos, this.topPos, this.menu.slots.get(1));
        HayoMachineTexture.drawOutputSlot(graphics, this.leftPos, this.topPos, this.menu.slots.get(2));
        HayoMachineTexture.drawUpgradeSlot(graphics, this.leftPos, this.topPos, this.menu.slots.get(3));
        HayoMachineTexture.drawUpgradeSlot(graphics, this.leftPos, this.topPos, this.menu.slots.get(4));
        HayoMachineTexture.drawUpgradeSlot(graphics, this.leftPos, this.topPos, this.menu.slots.get(5));
        HayoMachineTexture.drawUpgradeSlot(graphics, this.leftPos, this.topPos, this.menu.slots.get(6));

        HayoMachineTexture.drawZapMeter(graphics, this.leftPos + 48, this.topPos + 37, this.menu.getStoredEnergy(), this.getEnergyCapacity());
        HayoMachineTexture.drawRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.getArrow(), this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy());
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(48, 37, HayoMachineTexture.ZAP_SIZE, HayoMachineTexture.ZAP_SIZE, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    Component.translatable("hayoind.energy"), //
                    EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), this.getEnergyCapacity()).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }

    protected abstract HayoMachineTexture.RecipeArrow getArrow();

    protected abstract int getEnergyCapacity();

    public static class ElectricFurnaceScreen extends ProcessingMachineScreen {
        public ElectricFurnaceScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        protected HayoMachineTexture.RecipeArrow getArrow() {
            return HayoMachineTexture.RecipeArrow.DEFAULT;
        }

        @Override
        protected int getEnergyCapacity() {
            return ElectricFurnaceBlockEntity.CAPACITY;
        }
    }

    public static class MaceratorScreen extends ProcessingMachineScreen {
        public MaceratorScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        protected HayoMachineTexture.RecipeArrow getArrow() {
            return HayoMachineTexture.RecipeArrow.MACERATOR;
        }

        @Override
        protected int getEnergyCapacity() {
            return MaceratorBlockEntity.CAPACITY;
        }
    }
}
