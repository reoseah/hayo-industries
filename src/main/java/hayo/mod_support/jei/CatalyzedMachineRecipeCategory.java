package hayo.mod_support.jei;

import hayo.processing_machine.classic.ClassicMachineRecipe;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CatalyzedMachineRecipeCategory extends ClassicMachineRecipeCategory {
    protected final ItemStack catalyst;

    public CatalyzedMachineRecipeCategory(IRecipeType<? extends RecipeHolder<? extends ClassicMachineRecipe>> type, int energyUseRate, Identifier arrow, Identifier arrowOverlay, Component title, IDrawable icon, ItemStack catalyst) {
        super(type, energyUseRate, arrow, arrowOverlay, title, icon);
        this.catalyst = catalyst;
    }

    @Override
    protected boolean canHaveCatalyst() {
        return true;
    }

    @Override
    protected ItemStack getCatalyst(RecipeHolder<ClassicMachineRecipe> holder) {
        return this.catalyst;
    }
}
