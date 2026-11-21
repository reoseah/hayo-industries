package hayo.mod_support.rrv;

import cc.cassian.rrv.api.ReliableRecipeViewerClientPlugin;
import cc.cassian.rrv.api.recipe.ItemView;
import hayo.Hayo;
import net.minecraft.client.Minecraft;

public class HayoRrvPlugin implements ReliableRecipeViewerClientPlugin {
    @Override
    public void onIntegrationInitialize() {
        ItemView.addClientRecipeProvider(recipeList -> {
            var synchronizedRecipes = Minecraft.getInstance().level.recipeAccess().getSynchronizedRecipes();

            for (var holder : synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.MATTER_GENERATING)) {
                recipeList.add(new MatterGeneratingRrvRecipe(holder));
            }
            for (var holder : synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.SOLID_FLUID_REACTING)) {
                recipeList.add(new SolidFluidReactingRrvRecipe(holder));
            }
        });
    }
}
