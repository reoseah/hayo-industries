package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import io.github.reoseah.hayo.api.energy.EnergyTexts;
import io.github.reoseah.hayo.base.client.HayoMachineTexture;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class MachineRecipeCategory implements DisplayCategory<MachineRecipeDisplay> {
    protected final ResourceLocation location;
    protected final Renderer icon;
    protected final HayoMachineTexture.RecipeArrow arrowType;
    protected final int energyUseRate;

    public MachineRecipeCategory(ResourceLocation location, Renderer icon, HayoMachineTexture.RecipeArrow arrowType, int energyUseRate) {
        this.location = location;
        this.icon = icon;
        this.arrowType = arrowType;
        this.energyUseRate = energyUseRate;
    }

    @Override
    public CategoryIdentifier<? extends MachineRecipeDisplay> getCategoryIdentifier() {
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
        return 45;
    }

    @Override
    public List<Widget> setupDisplay(MachineRecipeDisplay display, Rectangle bounds) {
        var widgets = new ArrayList<Widget>();
        var startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 6);

        addCommonMachineRecipeWidgets(display, bounds, widgets, startPoint, this.energyUseRate, this.arrowType);

        return widgets;
    }

    public static void addCommonMachineRecipeWidgets(MachineRecipeDisplay display, Rectangle bounds, ArrayList<Widget> widgets, Point startPoint, int energyUseRate, HayoMachineTexture.RecipeArrow arrowType) {
        var tooltip = new Component[]{ //
                EnergyTexts.amount(display.processingEnergy), //
                Component.translatable("hayo.energy.duration_at_amount_per_tick", //
                        Mth.positiveCeilDiv(display.processingEnergy, energyUseRate) / 20F, //
                        energyUseRate) //
                        .withStyle(ChatFormatting.GRAY)};

        widgets.add(Widgets.createRecipeBase(bounds));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 1)) //
                .entries(display.getInput()) //
                .markInput());

        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 5)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5)) //
                .entries(display.getResult()) //
                .disableBackground() //
                .markOutput());

        var energy = new MachineEnergyWidget(new Point(startPoint.x + 1, startPoint.y + 20));
        widgets.add(Widgets.withTooltip(energy, tooltip));

        widgets.add(Widgets.createLabel(new Point(startPoint.x + 19, startPoint.y + 24), //
                        EnergyTexts.amount(display.processingEnergy)) //
                .noShadow().leftAligned().color(0xFF404040, 0xFFBBBBBB));

        var arrow = new MachineArrowWidget(new Point(startPoint.x + 24, startPoint.y + 4), //
                arrowType, //
                50 * display.processingEnergy / energyUseRate);
        widgets.add(Widgets.withTooltip(arrow, tooltip));
    }
}
