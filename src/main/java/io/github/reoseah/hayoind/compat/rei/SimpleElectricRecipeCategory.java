package io.github.reoseah.hayoind.compat.rei;

import io.github.reoseah.hayoind.client.screen.HayoMachineTexture;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SimpleElectricRecipeCategory implements DisplayCategory<SimpleElectricRecipeDisplay> {
    protected final ResourceLocation location;
    protected final Renderer icon;
    protected final HayoMachineTexture.RecipeArrow arrowType;
    protected final int energyUseRate;

    public SimpleElectricRecipeCategory(ResourceLocation location, Item icon, HayoMachineTexture.RecipeArrow arrowType, int energyUseRate) {
        this.location = location;
        this.icon = EntryStacks.of(icon);
        this.arrowType = arrowType;
        this.energyUseRate = energyUseRate;
    }

    @Override
    public CategoryIdentifier<? extends SimpleElectricRecipeDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of(this.location);
    }

    @Override
    public Component getTitle() {
        return Component.translatable(this.location.toLanguageKey("recipe_type"));
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
        widgets.add(new MachineArrowWidget(new Point(startPoint.x + 24, startPoint.y + 8), this.arrowType, 50 * display.processingEnergy / this.energyUseRate));

        return widgets;
    }
}
