package io.github.reoseah.hayo.feature.processing_machines;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ClassicMachineScreen extends HayoContainerScreen<MachineMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("textures/gui/container/machine.png");

    protected final HayoGuiSprites.RecipeArrow arrow;

    public ClassicMachineScreen(MachineMenu menu, Inventory inventory, Component title, HayoGuiSprites.RecipeArrow arrow) {
        super(menu, inventory, title);
        this.arrow = arrow;
    }

    public static MenuScreens.ScreenConstructor<MachineMenu, ClassicMachineScreen> withArrow(HayoGuiSprites.RecipeArrow arrowType) {
        return (menu, inventory, title) -> new ClassicMachineScreen(menu, inventory, title, arrowType);
    }

    protected static void drawClassicSlots(ClassicMachineScreen screen, GuiGraphics graphics) {
        for (var slot : screen.menu.slots.subList(0, 2)) {
            HayoGuiSprites.drawSlot(graphics, screen.leftPos + slot.x - 1, screen.topPos + slot.y - 1);
        }

        HayoGuiSprites.drawOutputSlot(graphics, screen.leftPos + screen.menu.slots.get(2).x - 4, screen.topPos + screen.menu.slots.get(2).y - 4);

        for (var slot : screen.menu.slots.subList(3, 7)) {
            HayoGuiSprites.drawUpgradeSlot(graphics, screen.leftPos + slot.x - 1, screen.topPos + slot.y - 1);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        drawClassicSlots(this, graphics);
        HayoGuiSprites.drawMachineEnergy(graphics, this.leftPos + 48, this.topPos + 37, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
        HayoGuiSprites.drawRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.arrow, this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy());
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
        var tooltip = super.getTooltipFromContainerItem(stack);
        if (stack.is(Hayo.Items.OVERCLOCK_UPGRADE)) {
            tooltip.add(EnergyTexts.overclockUseRate(100).withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(EnergyTexts.overclockTotalCost(25).withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        if (stack.is(Hayo.Items.CAPACITOR_UPGRADE)) {
            tooltip.add(Component.translatable("hayo.energy.capacity_change", "+10000").withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        if (stack.is(Hayo.Items.BLASTING_UPGRADE) || stack.is(Hayo.Items.SMOKING_UPGRADE)) {
            if (this.menu.getType() != Hayo.MenuTypes.ELECTRIC_FURNACE) {
                tooltip.add(Component.translatable("hayo.not_valid_for_this_machine").withStyle(ChatFormatting.DARK_AQUA));
                return tooltip;
            } else {
                if (stack.is(Hayo.Items.BLASTING_UPGRADE)) {
                    tooltip.add(Component.translatable("hayo.blasting_recipes_only").withStyle(ChatFormatting.DARK_AQUA));
                } else {
                    tooltip.add(Component.translatable("hayo.smoking_recipes_only").withStyle(ChatFormatting.DARK_AQUA));
                }
                tooltip.add(EnergyTexts.overclockUseRate(100).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        if (stack.is(Hayo.Items.INDUCTION_UPGRADE)) {
            if (this.menu.getType() != Hayo.MenuTypes.ELECTRIC_FURNACE) {
                tooltip.add(Component.translatable("hayo.not_valid_for_this_machine").withStyle(ChatFormatting.DARK_AQUA));
                return tooltip;
            } else {
                tooltip.add(Component.translatable("hayo.use_rate_scales_with_heat").withStyle(ChatFormatting.DARK_AQUA));
                tooltip.add(Component.translatable("hayo.heat.max", 10000).withStyle(ChatFormatting.DARK_AQUA));
                tooltip.add(Component.translatable("hayo.heat.amount_per_tick_when_active", "+1").withStyle(ChatFormatting.DARK_AQUA));
                tooltip.add(EnergyTexts.overclockUseRate(300).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        return tooltip;
    }

}
