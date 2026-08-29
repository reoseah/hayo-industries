package hayo.mod_support.jei;

import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.processing_machine.classic.ElectricFurnaceBlockEntity;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ElectricCookingCategory extends SingleItemRecipeCategory<RecipeHolder<? extends AbstractCookingRecipe>> {
    private final IRecipeType<? extends RecipeHolder<? extends AbstractCookingRecipe>> type;
    private final Component title;
    private final IDrawable icon;

    public ElectricCookingCategory(IRecipeType<? extends RecipeHolder<? extends AbstractCookingRecipe>> type, Component title, IDrawable icon) {
        this.type = (IRecipeType<RecipeHolder<? extends AbstractCookingRecipe>>) type;
        this.title = title;
        this.icon = icon;
    }

    @Override
    public IRecipeType<RecipeHolder<? extends AbstractCookingRecipe>> getRecipeType() {
        return (IRecipeType<RecipeHolder<? extends AbstractCookingRecipe>>) this.type;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    protected boolean canHaveCatalyst() {
        return this.type != HayoJeiPlugin.ELECTRIC_SMELTING;
    }

    @Override
    protected ItemStack getCatalyst(RecipeHolder<? extends AbstractCookingRecipe> holder) {
        return this.type == HayoJeiPlugin.ELECTRIC_BLASTING ? new ItemStack(Hayo.Items.BLASTING_UPGRADE)
                : this.type == HayoJeiPlugin.ELECTRIC_SMOKING ? new ItemStack(Hayo.Items.SMOKING_UPGRADE)
                : ItemStack.EMPTY;
    }

    @Override
    protected boolean canHaveSecondaryResult() {
        return false;
    }

    @Override
    protected int getEnergyCost(RecipeHolder<? extends AbstractCookingRecipe> holder) {
        return ElectricFurnaceBlockEntity.energyCostFromCookingTime(holder.value());
    }

    @Override
    protected int getEnergyUseRate(RecipeHolder<? extends AbstractCookingRecipe> holder) {
        return this.type == (Object) HayoJeiPlugin.ELECTRIC_SMELTING ? ElectricFurnaceBlockEntity.ENERGY_USE_RATE : 2 * ElectricFurnaceBlockEntity.ENERGY_USE_RATE;
    }

    @Override
    protected Identifier getArrowSprite() {
        return HayoGuiSprites.DEFAULT_ARROW;
    }

    @Override
    protected Identifier getArrowOverlaySprite() {
        return HayoGuiSprites.DEFAULT_ARROW_OVERLAY;
    }
}
