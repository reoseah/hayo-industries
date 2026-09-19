package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.processing_machine.classic.ElectricFurnaceBlockEntity;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public class ElectricCookingCategory extends SingleItemJeiCategory<RecipeHolder<? extends AbstractCookingRecipe>> {
    private final IRecipeType<? extends RecipeHolder<? extends AbstractCookingRecipe>> type;
    private final Component title;

    public ElectricCookingCategory(IRecipeType<? extends RecipeHolder<? extends AbstractCookingRecipe>> type, Component title, IDrawable icon, @Nullable ItemStack requiredUpgrade) {
        super(icon, requiredUpgrade);
        this.type = (IRecipeType<RecipeHolder<? extends AbstractCookingRecipe>>) type;
        this.title = title;
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
