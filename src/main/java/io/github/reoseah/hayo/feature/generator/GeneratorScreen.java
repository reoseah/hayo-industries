package io.github.reoseah.hayo.feature.generator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

import java.util.List;
import java.util.Optional;

public class GeneratorScreen extends HayoContainerScreen<GeneratorMenu> {
    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        HayoGuiSprites.drawSlot(graphics, x + 61, y + 53);
        HayoGuiSprites.drawSmallArrowRight(graphics, x + 79, y + 44);

        HayoGuiSprites.drawFuel(graphics, x + 62, y + 37, this.menu.getFuelEnergyLeft(), this.menu.getFuelEnergyTotal());

        HayoGuiSprites.drawEnergyStorage(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), GeneratorBlockEntity.CAPACITY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(88, 16, 18, 56, mouseX, mouseY)) {
            int energy = this.menu.getStoredEnergy();
            int capacity = GeneratorBlockEntity.CAPACITY;

            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.amountAndPercentage(energy, capacity), //
                    Component.translatable("hayo.energy.max_amount", EnergyTexts.formatAmount(capacity)).withStyle(ChatFormatting.GRAY) //

            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        if (this.isHovering(62, 37, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.approximateAmount(this.menu.getFuelEnergyLeft()), //
                    EnergyTexts.conversionRate(GeneratorBlockEntity.GENERATION_RATE).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var fuelValues = FuelValues.vanillaBurnTimes(Minecraft.getInstance().level.registryAccess(), FeatureFlags.DEFAULT_FLAGS);
        if (fuelValues.isFuel(stack) && !stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) {
            var energyValue = fuelValues.burnDuration(stack) * GeneratorBlockEntity.ENERGY_PER_FUEL_TICK;

            var tooltip = super.getTooltipFromContainerItem(stack);
            tooltip.add(EnergyTexts.approximateAmount(energyValue).withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        return super.getTooltipFromContainerItem(stack);
    }
}
