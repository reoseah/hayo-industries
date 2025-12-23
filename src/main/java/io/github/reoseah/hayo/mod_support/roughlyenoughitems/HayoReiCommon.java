package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratingRecipe;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

public class HayoReiCommon implements REICommonPlugin {
    @Override
    public double getPriority() {
        return 1;
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(Hayo.modLocation("machine_recipe"), MachineRecipeDisplay.SERIALIZER);
        registry.register(Hayo.modLocation("machine_recipe_with_extra"), MachineRecipeWithExtraDisplay.SERIALIZER);
    }

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(CompressingRecipe.class).fill(MachineRecipeDisplay::new);
        registry.beginRecipeFiller(ExtractingRecipe.class).fill(MachineRecipeDisplay::new);
        registry.beginRecipeFiller(MaceratingRecipe.class).fill(MachineRecipeWithExtraDisplay::new);
        registry.beginRecipeFiller(AbstractCookingRecipe.class) //
                .filterType(type -> ((RecipeType<?>) type) == RecipeType.SMELTING || ((RecipeType<?>) type) == RecipeType.BLASTING || ((RecipeType<?>) type) == RecipeType.SMOKING) //
                .fill(MachineRecipeDisplay::fromCookingRecipe);
    }
}
