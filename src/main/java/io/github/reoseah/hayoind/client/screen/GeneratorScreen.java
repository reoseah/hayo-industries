package io.github.reoseah.hayoind.client.screen;

import io.github.reoseah.hayoind.block.entity.EnergyCrystalArrayBlockEntity;
import io.github.reoseah.hayoind.block.entity.GeneratorBlockEntity;
import io.github.reoseah.hayoind.menu.EnergyTexts;
import io.github.reoseah.hayoind.menu.GeneratorMenu;
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

public class GeneratorScreen extends MachineScreen<GeneratorMenu> {
    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        drawSlot(graphics, x + 61, y + 53);
        blitGuiTexture(graphics, x + 79, y + 44, TINY_ARROW_X, TINY_ARROW_Y, TINY_ARROW_WIDTH, TINY_ARROW_HEIGHT);

        drawFuelMeter(graphics, x + 62, y + 37, this.menu.getFuelEnergyLeft(), this.menu.getFuelEnergyTotal());

        drawVerticalEnergyBar(graphics, x + 88, y + 16, this.menu.getStoredEnergy(), GeneratorBlockEntity.CAPACITY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(88, 16, VERT_ENERGY_WIDTH, VERT_ENERGY_HEIGHT, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.STORED_ENERGY, //
                    EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), GeneratorBlockEntity.CAPACITY).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        if (this.isHovering(62, 37, FUEL_GAUGE_SIZE, FUEL_GAUGE_SIZE, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    Component.translatable("hayoind.fuel"), //
                    EnergyTexts.approximateAmount(this.menu.getFuelEnergyLeft()).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var fuelValues = FuelValues.vanillaBurnTimes(Minecraft.getInstance().level.registryAccess(), FeatureFlags.DEFAULT_FLAGS);
        if (fuelValues.isFuel(stack)) {
            var fuelValue = fuelValues.burnDuration(stack) * GeneratorBlockEntity.ENERGY_PER_FUEL_TICK;
            var tooltip = super.getTooltipFromContainerItem(stack);
            tooltip.add(EnergyTexts.fuelValue(fuelValue).withStyle(ChatFormatting.DARK_AQUA));
            return tooltip;
        }
        return super.getTooltipFromContainerItem(stack);
    }
}
