package hayo.feature.universal_screen;

import hayo.energy.client.EnergySprites;
import hayo.energy.EnergyTexts;
import hayo.menu.UniversalContainerMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public record StorageEnergyBar(int x, int y, IntSupplier amount, IntSupplier capacity,
                               @Nullable Supplier<Float> inputPerTick,
                               @Nullable Supplier<Float> outputPerTick) implements UniversalContainerMenu.GuiElement {
    public StorageEnergyBar(int x, int y, IntSupplier amount, IntSupplier capacity) {
        this(x, y, amount, capacity, null, null);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void extract(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY, float partialTick) {
        EnergySprites.extractVerticalBar(graphics, left + this.x, top + this.y, this.amount.getAsInt(), this.capacity.getAsInt());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void extractTooltip(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY) {
        if (UniversalContainerMenu.GuiElement.isHovering(left, top, this.x, this.y, 18, 56, mouseX, mouseY)) {
            var tooltip = new ArrayList<Component>();
            int amount = this.amount.getAsInt();
            int capacity = this.capacity.getAsInt();

            tooltip.add(EnergyTexts.amountAndPercentage(amount, capacity));
            tooltip.add(EnergyTexts.maxAmount(capacity).withStyle(ChatFormatting.GRAY));

            if (this.inputPerTick != null) {
                tooltip.add(EnergyTexts.averageInputPerTick(this.inputPerTick.get()).withStyle(ChatFormatting.GRAY));
            }
            if (this.outputPerTick != null) {
                tooltip.add(EnergyTexts.averageOutputPerTick(this.outputPerTick.get()).withStyle(ChatFormatting.GRAY));
            }

            graphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }
}
