package io.github.reoseah.hayo.compat.rei;

import io.github.reoseah.hayo.client.screen.HayoMachineTexture;
import io.github.reoseah.hayo.menu.EnergyTexts;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class SimpleElectricRecipeCategory implements DisplayCategory<SimpleElectricRecipeDisplay> {
    protected final ResourceLocation location;
    protected final Renderer icon;
    protected final HayoMachineTexture.RecipeArrow arrowType;
    protected final int energyUseRate;

    public SimpleElectricRecipeCategory(ResourceLocation location, Renderer icon, HayoMachineTexture.RecipeArrow arrowType, int energyUseRate) {
        this.location = location;
        this.icon = icon;
        this.arrowType = arrowType;
        this.energyUseRate = energyUseRate;
    }

    @Override
    public CategoryIdentifier<? extends SimpleElectricRecipeDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of(this.location);
    }

    @Override
    public Component getTitle() {
        return Component.translatable(this.location.toLanguageKey());
    }

    @Override
    public Renderer getIcon() {
        return this.icon;
    }

    @Override
    public int getDisplayHeight() {
        return 49;
    }

    @Override
    public List<Widget> setupDisplay(SimpleElectricRecipeDisplay display, Rectangle bounds) {
        var widgets = new ArrayList<Widget>();
        var startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 10);

        widgets.add(Widgets.createRecipeBase(bounds));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 1)) //
                .entries(display.getInput()) //
                .markInput());

        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 9)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 9)) //
                .entries(display.getResult()) //
                .disableBackground() //
                .markOutput());

        widgets.add(new MachineEnergyWidget(new Point(startPoint.x + 1, startPoint.y + 20)));
        widgets.add(Widgets.withTooltip(new MachineArrowWidget(new Point(startPoint.x + 24, startPoint.y + 8), //
                        this.arrowType, //
                        50 * display.processingEnergy / this.energyUseRate), //
                EnergyTexts.ENERGY, //
                EnergyTexts.amount(display.processingEnergy).withStyle(ChatFormatting.GRAY), //
                Component.translatable("hayo.energy.duration_at_amount_per_tick", //
                                Mth.positiveCeilDiv(display.processingEnergy, this.energyUseRate) / 20F, //
                                this.energyUseRate) //
                        .withStyle(ChatFormatting.GRAY)));

        return widgets;
    }

    public static MutableComponent duration(float seconds) {
        return Component.translatable("hayo.duration_in_seconds", Math.ceil(seconds * 100) / 100);
    }
}
