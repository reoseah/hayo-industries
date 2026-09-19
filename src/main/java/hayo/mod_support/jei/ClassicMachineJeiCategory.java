package hayo.mod_support.jei;

import hayo.processing_machine.classic.ClassicMachineRecipe;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public class ClassicMachineJeiCategory extends SingleItemJeiCategory<RecipeHolder<ClassicMachineRecipe>> {
    private final IRecipeType<RecipeHolder<ClassicMachineRecipe>> type;
    private final int energyUseRate;
    private final Identifier arrow, arrowOverlay;
    private final Component title;

    public ClassicMachineJeiCategory(IRecipeType<? extends RecipeHolder<? extends ClassicMachineRecipe>> type, int energyUseRate, Identifier arrow, Identifier arrowOverlay, Component title, IDrawable icon, @Nullable ItemStack requiredUpgrade) {
        super(icon, requiredUpgrade);
        this.type = (IRecipeType<RecipeHolder<ClassicMachineRecipe>>) type;
        this.energyUseRate = energyUseRate;
        this.arrow = arrow;
        this.arrowOverlay = arrowOverlay;
        this.title = title;
    }

    @Override
    public IRecipeType<RecipeHolder<ClassicMachineRecipe>> getRecipeType() {
        return (IRecipeType<RecipeHolder<ClassicMachineRecipe>>) this.type;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    protected boolean canHaveSecondaryResult() {
        return true;
    }

    @Override
    protected int getEnergyCost(RecipeHolder<ClassicMachineRecipe> holder) {
        return holder.value().energyCost;
    }

    @Override
    protected int getEnergyUseRate(RecipeHolder<ClassicMachineRecipe> holder) {
        return this.energyUseRate;
    }

    @Override
    protected Identifier getArrowSprite() {
        return this.arrow;
    }

    @Override
    protected Identifier getArrowOverlaySprite() {
        return this.arrowOverlay;
    }
}
