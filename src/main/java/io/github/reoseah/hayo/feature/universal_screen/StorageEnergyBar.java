package io.github.reoseah.hayo.feature.universal_screen;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyTexts;
import io.github.reoseah.hayo.menu.UniversalContainerMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public record StorageEnergyBar(int x, int y, IntSupplier amount, IntSupplier capacity,
                               @Nullable Supplier<Float> inputPerTick,
                               @Nullable Supplier<Float> outputPerTick) implements UniversalContainerMenu.GuiElement {
    public static final Identifier ENERGY_LARGE = Hayo.modId("energy_large");
    public static final Identifier ENERGY_LARGE_OVERLAY = Hayo.modId("energy_large_overlay");

    public StorageEnergyBar(int x, int y, IntSupplier amount, IntSupplier capacity) {
        this(x, y, amount, capacity, null, null);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void extract(GuiGraphicsExtractor graphics, Font font, int left, int top, int mouseX, int mouseY, float partialTick) {
        int amount = this.amount.getAsInt();
        int capacity = this.capacity.getAsInt();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_LARGE, left + this.x, top + this.y, 18, 56);

        if (amount > 0 && capacity > 0) {
            var filled = Mth.clamp(1 + (48 - 1) * amount / capacity, 1, 48);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_LARGE_OVERLAY, 10, 48, 0, 48 - filled, left + this.x + 4, top + this.y + 52 - filled, 10, filled);
        }
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
