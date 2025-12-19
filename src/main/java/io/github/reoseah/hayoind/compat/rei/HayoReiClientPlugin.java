package io.github.reoseah.hayoind.compat.rei;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.client.screen.HayoMachineTexture;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class HayoReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SimpleElectricRecipeCategory(Hayo.modLocation("macerating"), Hayo.Items.MACERATOR, HayoMachineTexture.RecipeArrow.MACERATOR, 2));
        registry.add(new SimpleElectricRecipeCategory(Hayo.modLocation("compressing"), Hayo.Items.COMPRESSOR, HayoMachineTexture.RecipeArrow.COMPRESSOR, 2));
        registry.add(new SimpleElectricRecipeCategory(Hayo.modLocation("extracting"), Hayo.Items.EXTRACTOR, HayoMachineTexture.RecipeArrow.EXTRACTOR, 2));

        registry.addWorkstations(CategoryIdentifier.of(Hayo.modLocation("macerating")), EntryStacks.of(Hayo.Items.MACERATOR));
        registry.addWorkstations(CategoryIdentifier.of(Hayo.modLocation("compressing")), EntryStacks.of(Hayo.Items.COMPRESSOR));
        registry.addWorkstations(CategoryIdentifier.of(Hayo.modLocation("extracting")), EntryStacks.of(Hayo.Items.EXTRACTOR));
    }
}
