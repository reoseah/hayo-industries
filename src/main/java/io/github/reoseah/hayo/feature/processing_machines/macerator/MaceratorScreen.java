package io.github.reoseah.hayo.feature.processing_machines.macerator;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class MaceratorScreen extends ClassicMachineScreen {
    public MaceratorScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected HayoGuiSprites.RecipeArrow getArrow() {
        return HayoGuiSprites.RecipeArrow.MACERATOR;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        drawClassicSlots(this, graphics);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(48, 37, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(getFont(), List.of(EnergyTexts.amountAndCapacity(getMenu().getStoredEnergy(), getMenu().getEnergyCapacity())), Optional.empty(), mouseX, mouseY);
            return;
        }
        if (isHovering(70, 36, 24, 16, mouseX, mouseY) && getMenu().getRecipeTotalEnergy() > 0) {
            graphics.setTooltipForNextFrame(getFont(), List.of( //
                    Component.translatable("hayo.energy.amount_with_capacity_and_percentage", getMenu().getRecipeUsedEnergy(), getMenu().getRecipeTotalEnergy(), 100 * getMenu().getRecipeUsedEnergy() / getMenu().getRecipeTotalEnergy()), //
                    Component.translatable("hayo.energy.duration_at_amount_per_tick", getMenu().getRecipeDuration(), getMenu().getEnergyUseRate()).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
