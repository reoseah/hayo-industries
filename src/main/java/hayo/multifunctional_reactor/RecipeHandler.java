package hayo.multifunctional_reactor;

import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class RecipeHandler<R extends Recipe<I>, I extends RecipeInput> {
    @Getter
    protected @Nullable RecipeHolder<R> lastMatch;

    @Getter
    protected int progress;

    public void save(ValueOutput output) {
        output.putInt("progress", this.progress);
    }

    public void load(ValueInput input) {
        this.progress = input.getIntOr("progress", 0);
    }

    public int tick(RecipeType<R> recipeType, I input, ServerLevel level, RecipeResourceState resourceState, Context<R, I> ctx) {
        if (input.isEmpty()) {
            if (this.progress > 0) {
                this.progress = 0;
                ctx.setChanged();
            }
            return 0;
        }

        var match = level.recipeAccess().getRecipeFor(recipeType, input, level, this.lastMatch).orElse(null);
        if (match == null) {
            if (this.progress != 0) {
                this.progress = 0;
                ctx.setChanged();
            }
            return 0;
        }
        if (match != this.lastMatch) {
            this.lastMatch = match;
        }

        return switch (resourceState) {
            case RecipeResourceState.NotSufficient(int progressLossPerTick) -> {
                if (this.progress <= 0) {
                    yield 0;
                }
                int progressChange = Math.min(progressLossPerTick, this.progress);
                this.progress -= progressChange;
                ctx.setChanged();
                yield -progressChange;
            }
            case RecipeResourceState.Sufficient(int progressLimit) -> {
                if (!ctx.canCraft(match, input)) {
                    yield 0;
                }

                int recipeCost = ctx.getRecipeCost(match.value());
                int progressChange = Math.min(progressLimit, recipeCost - this.progress);
                this.progress += progressChange;
                ctx.setChanged();

                if (this.progress >= recipeCost) {
                    ctx.craft(match, input);
                    this.progress = 0;
                }

                yield progressChange;
            }
        };
    }

    public sealed interface RecipeResourceState permits RecipeResourceState.NotSufficient, RecipeResourceState.Sufficient {
        record NotSufficient(int progressLossPerTick) implements RecipeResourceState {
        }

        record Sufficient(int progressLimit) implements RecipeResourceState {
        }
    }

    public interface Context<R extends Recipe<I>, I extends RecipeInput> {
        void setChanged();

        boolean canCraft(RecipeHolder<R> recipe, I input);

        int getRecipeCost(R recipe);

        void craft(RecipeHolder<R> recipe, I input);
    }
}
