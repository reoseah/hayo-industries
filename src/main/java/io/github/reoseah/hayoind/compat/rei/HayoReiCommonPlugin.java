package io.github.reoseah.hayoind.compat.rei;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.recipe.SimpleElectricRecipe;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;

public class HayoReiCommonPlugin implements REICommonPlugin {
    @Override
    public double getPriority() {
        return 1;
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(Hayo.modLocation("simple_electric_recipe"), SimpleElectricRecipeDisplay.SERIALIZER);
    }

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(SimpleElectricRecipe.class).fill(SimpleElectricRecipeDisplay::new);
    }
}
