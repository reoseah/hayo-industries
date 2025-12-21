package io.github.reoseah.hayo.client.screen;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.menu.EnergyTexts;
import io.github.reoseah.hayo.menu.ProcessingMachineMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public abstract class ProcessingMachineScreen extends HayoContainerScreen<ProcessingMachineMenu> {
    public ProcessingMachineScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    protected static void drawClassicSlots(ProcessingMachineScreen screen, GuiGraphics graphics) {
        HayoMachineTexture.drawSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(0));
        HayoMachineTexture.drawSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(1));
        HayoMachineTexture.drawOutputSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(2));

        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(3));
        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(4));
        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(5));
        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(6));
    }

    protected static void drawClassicWithSecondaryOutputSlots(ProcessingMachineScreen screen, GuiGraphics graphics) {
        HayoMachineTexture.drawSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(0));
        HayoMachineTexture.drawSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(1));
        HayoMachineTexture.drawOutputSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(2));
        HayoMachineTexture.drawSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(3));

        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(4));
        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(5));
        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(6));
        HayoMachineTexture.drawUpgradeSlot(graphics, screen.leftPos, screen.topPos, screen.menu.slots.get(7));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        HayoMachineTexture.drawZapMeter(graphics, this.leftPos + 48, this.topPos + 37, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
        HayoMachineTexture.drawRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.getArrow(), this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy());
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(48, 37, HayoMachineTexture.ZAP_SIZE, HayoMachineTexture.ZAP_SIZE, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    Component.translatable("hayo.energy"), //
                    EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), this.menu.getEnergyCapacity()).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        // TODO: show this over the arrow, split into three lines + the name of recipe type as title?
//        if (stack == this.menu.getSlot(0).getItem()) {
//            var tooltip = super.getTooltipFromContainerItem(stack);
//            if (this.menu.hasOverclockUpgrades()) {
//                tooltip.add(EnergyTexts.overclockedRecipe(this.menu.getRecipeTotalEnergy(), this.menu.getRecipeEnergyPercentage(), //
//                                this.menu.getEnergyUseRate(), this.menu.getUseRatePercentage(), //
//                                this.menu.getRecipeDuration(), this.menu.getRecipeDurationPercentage()) //
//                        .withStyle(ChatFormatting.DARK_AQUA));
//            } else {
//                tooltip.add(EnergyTexts.recipe(this.menu.getRecipeTotalEnergy(), this.menu.getEnergyUseRate(), this.menu.getRecipeDuration()).withStyle(ChatFormatting.DARK_AQUA));
//            }
//
//            return tooltip;
//        }
        if (stack.is(Hayo.Items.OVERCLOCK_UPGRADE)) {
            var tooltip = super.getTooltipFromContainerItem(stack);
            tooltip.add(EnergyTexts.overclockUseRate(100).withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(EnergyTexts.overclockTotalCost(25).withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        } else if (stack.is(Hayo.Items.CAPACITOR_UPGRADE)) {
            var tooltip = super.getTooltipFromContainerItem(stack);
            tooltip.add(Component.translatable("hayo.energy.capacity_change", "+10000").withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        return super.getTooltipFromContainerItem(stack);
    }

    protected abstract HayoMachineTexture.RecipeArrow getArrow();

    public static class ElectricFurnaceScreen extends ProcessingMachineScreen {
        public ElectricFurnaceScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        protected HayoMachineTexture.RecipeArrow getArrow() {
            return HayoMachineTexture.RecipeArrow.DEFAULT;
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            super.renderBg(graphics, partialTick, mouseX, mouseY);

            drawClassicSlots(this, graphics);
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
        protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            super.renderBg(graphics, partialTick, mouseX, mouseY);

            drawClassicSlots(this, graphics);
        }
    }

    public static class CompressorScreen extends ProcessingMachineScreen {
        public CompressorScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        protected HayoMachineTexture.RecipeArrow getArrow() {
            return HayoMachineTexture.RecipeArrow.COMPRESSOR;
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            super.renderBg(graphics, partialTick, mouseX, mouseY);

            drawClassicSlots(this, graphics);
        }
    }

    public static class ExtractorScreen extends ProcessingMachineScreen {
        public ExtractorScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        protected HayoMachineTexture.RecipeArrow getArrow() {
            return HayoMachineTexture.RecipeArrow.EXTRACTOR;
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            super.renderBg(graphics, partialTick, mouseX, mouseY);

            drawClassicWithSecondaryOutputSlots(this, graphics); // FIXME: draw slots with secondary result
        }
    }
}
