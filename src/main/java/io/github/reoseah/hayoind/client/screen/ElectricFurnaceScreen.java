package io.github.reoseah.hayoind.client.screen;

import io.github.reoseah.hayoind.block.entity.ElectricFurnaceBlockEntity;
import io.github.reoseah.hayoind.menu.ElectricFurnaceMenu;
import io.github.reoseah.hayoind.menu.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class ElectricFurnaceScreen extends MachineScreen<ElectricFurnaceMenu> {
    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        this.drawSlot(graphics, this.menu.slots.get(0));
        this.drawSlot(graphics, this.menu.slots.get(1));
        this.drawOutputSlot(graphics, this.menu.slots.get(2));
        this.drawUpgradeSlot(graphics, this.menu.slots.get(3));
        this.drawUpgradeSlot(graphics, this.menu.slots.get(4));
        this.drawUpgradeSlot(graphics, this.menu.slots.get(5));
        this.drawUpgradeSlot(graphics, this.menu.slots.get(6));

        drawZapMeter(graphics, this.leftPos + 48, this.topPos + 37, this.menu.getStoredEnergy(), ElectricFurnaceBlockEntity.CAPACITY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(48, 37, ZAP_SIZE, ZAP_SIZE, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    Component.translatable("hayoind.energy"), //
                    EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), ElectricFurnaceBlockEntity.CAPACITY).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
