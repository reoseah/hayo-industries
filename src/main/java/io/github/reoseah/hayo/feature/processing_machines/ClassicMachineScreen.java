package io.github.reoseah.hayo.feature.processing_machines;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import io.github.reoseah.hayo.feature.processing_machines.electric_furnace.ElectricFurnaceScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public abstract class ClassicMachineScreen extends HayoContainerScreen<MachineMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("textures/gui/container/machine.png");

    public ClassicMachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    protected abstract HayoGuiSprites.RecipeArrow getArrow();

    protected static void drawClassicSlots(ClassicMachineScreen screen, GuiGraphics graphics) {
        for (var slot : screen.menu.slots.subList(0, 2)) {
            HayoGuiSprites.drawSlot(graphics, screen.leftPos + slot.x - 1, screen.topPos + slot.y - 1);
        }

        HayoGuiSprites.drawOutputSlot(graphics, screen.leftPos + screen.menu.slots.get(2).x - 5, screen.topPos + screen.menu.slots.get(2).y - 5);

        for (var slot : screen.menu.slots.subList(3, 7)) {
            HayoGuiSprites.drawUpgradeSlot(graphics, screen.leftPos + slot.x - 1, screen.topPos + slot.y - 1);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        HayoGuiSprites.drawMachineEnergy(graphics, this.leftPos + 48, this.topPos + 37, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
        HayoGuiSprites.drawRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.getArrow(), this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy());
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(48, 37, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of(EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), this.menu.getEnergyCapacity())), Optional.empty(), mouseX, mouseY);
            return;
        }
        if (this.isHovering(70, 36, 24, 16, mouseX, mouseY) && this.menu.getRecipeTotalEnergy() > 0) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.amountWithCapacityAndPercentage(this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy()), //
                    EnergyTexts.durationAtAmountPerTick(this.menu.getRecipeDuration(), this.menu.getEnergyUseRate()).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        if (stack.is(Hayo.Items.OVERCLOCK_UPGRADE)) {
            var tooltip = super.getTooltipFromContainerItem(stack);
            tooltip.add(EnergyTexts.overclockUseRate(100).withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(EnergyTexts.overclockTotalCost(25).withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        if (stack.is(Hayo.Items.CAPACITOR_UPGRADE)) {
            var tooltip = super.getTooltipFromContainerItem(stack);
            tooltip.add(Component.translatable("hayo.energy.capacity_change", "+10000").withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        if (!(this instanceof ElectricFurnaceScreen) //
                && (stack.is(Hayo.Items.BLASTING_UPGRADE) || stack.is(Hayo.Items.SMOKING_UPGRADE))) {
            var tooltip = super.getTooltipFromContainerItem(stack);
            tooltip.add(Component.translatable("hayo.not_valid_for_this_machine").withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        return super.getTooltipFromContainerItem(stack);
    }

}
