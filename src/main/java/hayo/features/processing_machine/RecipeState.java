package hayo.features.processing_machine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

public class RecipeState<R extends Recipe<I>, I extends RecipeInput> {
    public @Nullable RecipeHolder<R> lastMatch;
    public int progress;

    public int tick(RecipeType<R> recipeType, I input, ServerLevel level, Owner<R, I> owner) {
        if (input.isEmpty()) {
            if (this.progress > 0) {
                this.progress = 0;
                owner.setChanged();
            }
            return 0;
        }

        var match = this.getMatchingRecipe(recipeType, input, level);
        if (match == null) {
            if (this.progress != 0) {
                this.progress = 0;
                owner.setChanged();
            }
            return 0;
        }
        if (match != this.lastMatch) {
            this.lastMatch = match;
        }

        return switch (owner.getEnergyState()) {
            case EnergyState.NotSufficient(int decayPerTick) -> {
                if (this.progress <= 0) {
                    yield 0;
                }
                int progressChange = Math.min(-decayPerTick, this.progress);
                this.progress -= progressChange;
                owner.setChanged();
                yield 0;
            }
            case EnergyState.Sufficient(int progressLimit) -> {
                if (!owner.canCraft(match, input)) {
                    yield 0;
                }

                int recipeCost = owner.getRecipeCost(match.value());
                int progressChange = Math.min(progressLimit, recipeCost - this.progress);
                this.progress += progressChange;
                owner.setChanged();

                if (this.progress >= recipeCost) {
                    owner.craft(match, input);
                    this.progress = 0;
                }

                yield progressChange;
            }
        };
    }

    protected @Nullable RecipeHolder<R> getMatchingRecipe(RecipeType<R> recipeType, I input, ServerLevel level) {
        return level.recipeAccess().getRecipeFor(recipeType, input, level, this.lastMatch).orElse(null);
    }

    public sealed interface EnergyState permits EnergyState.NotSufficient, EnergyState.Sufficient {
        record NotSufficient(int decayPerTick) implements EnergyState {
        }

        record Sufficient(int progressLimit) implements EnergyState {
        }
    }

    public interface Owner<R extends Recipe<I>, I extends RecipeInput> {
        void setChanged();

        RecipeState.EnergyState getEnergyState();

        boolean canCraft(RecipeHolder<R> holder, I input);

        int getRecipeCost(R holder);

        void craft(RecipeHolder<R> holder, I input);
    }
}
