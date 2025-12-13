package io.github.reoseah.hayoind.client.screen;

import io.github.reoseah.hayoind.block.entity.EnergyCrystalArrayBlockEntity;
import io.github.reoseah.hayoind.menu.EnergyCrystalArrayMenu;
import io.github.reoseah.hayoind.menu.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class EnergyCrystalArrayScreen extends MachineScreen<EnergyCrystalArrayMenu> {
    public EnergyCrystalArrayScreen(EnergyCrystalArrayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        int x = this.leftPos;
        int y = this.topPos;

        drawSlot(graphics, x + 61, y + 17);
        blitGuiTexture(graphics, x + 79, y + 17, TINY_ARROW_X, TINY_ARROW_Y, TINY_ARROW_WIDTH, TINY_ARROW_HEIGHT);

        drawSlot(graphics, x + 61, y + 53);
        blitGuiTexture(graphics, x + 79, y + 53, TINY_ARROW_LEFT_X, TINY_ARROW_Y, TINY_ARROW_WIDTH, TINY_ARROW_HEIGHT);

        drawVerticalEnergyBar(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), EnergyCrystalArrayBlockEntity.CAPACITY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(88, 16, VERT_ENERGY_WIDTH, VERT_ENERGY_HEIGHT, mouseX, mouseY)) {
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
