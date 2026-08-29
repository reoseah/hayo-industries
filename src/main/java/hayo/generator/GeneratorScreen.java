package hayo.generator;

import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

import java.util.List;
import java.util.Optional;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
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
        HayoGuiSprites.blitSmallArrowRight(graphics, this.leftPos + 79, this.topPos + 44);

        HayoGuiSprites.blitSlot(graphics, this.leftPos + 61, this.topPos + 53);
        HayoGuiSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);

        HayoGuiSprites.blitFuel(graphics, this.leftPos + 62, this.topPos + 37, this.menu.generatorData.fuelLeft(), this.menu.generatorData.fuelTotal());
        EnergyGuiSprites.extractVerticalBar(graphics, this.leftPos + 88, this.topPos + 16, this.menu.generatorData.energy(), GeneratorBlockEntity.CAPACITY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(88, 16, 18, 56, mouseX, mouseY)) {
            int energy = this.menu.generatorData.energy();
            int capacity = GeneratorBlockEntity.CAPACITY;

            graphics.setTooltipForNextFrame(this.font, List.of(
                    EnergyTexts.amountAndPercentage(energy, capacity),
                    EnergyTexts.maxAmount(capacity).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var fuelValues = FuelValues.vanillaBurnTimes(Minecraft.getInstance().level.registryAccess(), FeatureFlags.DEFAULT_FLAGS);
        if (fuelValues.isFuel(stack)) {
            if (stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) {
                var tooltip = super.getTooltipFromContainerItem(stack);
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("hayo.generator.disabled_fuel").withStyle(ChatFormatting.RED));
                return tooltip;
            } else {
                var energyValue = fuelValues.burnDuration(stack) * GeneratorBlockEntity.ENERGY_PER_FUEL_TICK;

                var tooltip = super.getTooltipFromContainerItem(stack);
                tooltip.add(Component.empty());
                if (stack.count() == 1) {
                    tooltip.add(Component.translatable("hayo.generator.item_value", energyValue).withStyle(ChatFormatting.DARK_AQUA));
                } else {
                    tooltip.add(Component.translatable("hayo.generator.stack_value", energyValue, energyValue * stack.count()).withStyle(ChatFormatting.DARK_AQUA));
                }

                return tooltip;
            }
        }
        return super.getTooltipFromContainerItem(stack);
    }
}
