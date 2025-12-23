package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import io.github.reoseah.hayo.base.client.HayoMachineTexture;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MachineRecipeWithExtraCategory implements DisplayCategory<MachineRecipeWithExtraDisplay> {
    protected final ResourceLocation location;
    protected final Renderer icon;
    protected final HayoMachineTexture.RecipeArrow arrowType;
    protected final int energyUseRate;

    public MachineRecipeWithExtraCategory(ResourceLocation location, Renderer icon, HayoMachineTexture.RecipeArrow arrowType, int energyUseRate) {
        this.location = location;
        this.icon = icon;
        this.arrowType = arrowType;
        this.energyUseRate = energyUseRate;
    }

    @Override
    public CategoryIdentifier<? extends MachineRecipeWithExtraDisplay> getCategoryIdentifier() {
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
    public List<Widget> setupDisplay(MachineRecipeWithExtraDisplay display, Rectangle bounds) {
        var widgets = new ArrayList<Widget>();
        if (display.extraChance > 0) {
            var startPoint = new Point(bounds.getCenterX() - 50, bounds.y + 6);

            MachineRecipeCategory.addCommonMachineRecipeWidgets(display, bounds, widgets, startPoint, this.energyUseRate, this.arrowType);

            widgets.add(Widgets.createSlot(new Point(startPoint.x + 85, startPoint.y + 1)) //
                    .entries(display.getExtraResult()) //
                    .markOutput());
            widgets.add(Widgets.createLabel(new Point(startPoint.x + 85, startPoint.y + 24), //
                            Component.translatable("hayo.chance.percentage", String.format("%.0f", 100 * display.extraChance))) //
                    .noShadow().leftAligned().color(0xFF404040, 0xFFBBBBBB));
        } else {
            var startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 6);

            MachineRecipeCategory.addCommonMachineRecipeWidgets(display, bounds, widgets, startPoint, this.energyUseRate, this.arrowType);
        }
        return widgets;
    }

}
