package io.github.reoseah.hayo.feature.rei_support;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.SimpleElectricRecipe;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

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
        registry.beginRecipeFiller(AbstractCookingRecipe.class)
                .filterType(type -> ((RecipeType<?>) type) == RecipeType.SMELTING || ((RecipeType<?>) type) == RecipeType.BLASTING || ((RecipeType<?>) type) == RecipeType.SMOKING)
                .fill(SimpleElectricRecipeDisplay::fromCookingRecipe);
    }
}
