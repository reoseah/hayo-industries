package io.github.reoseah.hayo.feature.generator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyTexts;
import io.github.reoseah.hayo.feature.machines.ClassicMachineScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

import java.util.List;
import java.util.Optional;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    public static final Identifier FUEL = Hayo.modId("fuel");
    public static final Identifier FUEL_OVERLAY = Hayo.modId("fuel_overlay");

    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public static void drawFuel(GuiGraphicsExtractor graphics, int x, int y, int fuelLeft, int fuelTotal) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL, x, y, 14, 14);

        if (fuelLeft > 0 && fuelTotal > 0) {
            var height = Mth.clamp(1 + (14 - 1) * fuelLeft / fuelTotal, 1, 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL_OVERLAY, 14, 14, 0, 14 - height, x, y + 14 - height, 14, height);
        }
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ClassicMachineScreen.BACKGROUND, this.leftPos, this.topPos, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);

        int x = this.leftPos;
        int y = this.topPos;

        HayoGuiSprites.drawSlot(graphics, x + 61, y + 53);
        HayoGuiSprites.drawSmallArrowRight(graphics, x + 79, y + 44);

        drawFuel(graphics, x + 62, y + 37, this.menu.getFuelEnergyLeft(), this.menu.getFuelEnergyTotal());

        EnergyGuiSprites.energyLarge(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), GeneratorBlockEntity.CAPACITY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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

        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var fuelValues = FuelValues.vanillaBurnTimes(Minecraft.getInstance().level.registryAccess(), FeatureFlags.DEFAULT_FLAGS);
        if (fuelValues.isFuel(stack)) {
            if (!stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) {
                var energyValue = fuelValues.burnDuration(stack) * GeneratorBlockEntity.ENERGY_PER_FUEL_TICK;

                var tooltip = super.getTooltipFromContainerItem(stack);
                tooltip.add(EnergyTexts.approximateAmount(energyValue).withStyle(ChatFormatting.DARK_AQUA));
                return tooltip;
            } else {
                var tooltip = super.getTooltipFromContainerItem(stack);
                tooltip.add(Component.translatable("hayo.generator.disabled_fuel").withStyle(ChatFormatting.RED));
                return tooltip;
            }
        }
        return super.getTooltipFromContainerItem(stack);
    }
}
