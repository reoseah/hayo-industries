package hayo.processing_machine.solid_fluid_reactor;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.fluid_stack.FluidIngredientAmount;
import hayo.fluid_stack.FluidStack;
import hayo.fluid_stack.ItemFluidRecipeInput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class BaseSolidFluidReactingRecipe implements Recipe<ItemFluidRecipeInput> {
    @FunctionalInterface
    public interface Factory<T extends BaseSolidFluidReactingRecipe> {
        T create(
                Ingredient inputItem,
                int inputAmount,
                FluidIngredientAmount inputFluid,
                List<ItemStackTemplate> resultItems,
                List<Float> extraResultChances,
                FluidStack resultFluid,
                int energyCost
        );
    }

    public static <T extends BaseSolidFluidReactingRecipe> MapCodec<T> codec(Factory<T> factory, int defaultEnergyCost) {
        return RecordCodecBuilder.<T>mapCodec(instance -> instance
                .group(
                        Ingredient.CODEC.fieldOf("input_item").forGetter(recipe -> recipe.inputItem),
                        ExtraCodecs.POSITIVE_INT.fieldOf("input_count").orElse(1).forGetter(recipe -> recipe.inputCount),
                        FluidIngredientAmount.NON_EMPTY_CODEC.fieldOf("input_fluid").forGetter(recipe -> recipe.inputFluid),
                        ItemStackTemplate.CODEC.listOf(0, 3).fieldOf("result_items").orElse(List.of()).forGetter(recipe -> recipe.resultItems),
                        ExtraCodecs.floatRange(0, 1).listOf(0, 3).fieldOf("extra_result_chances").orElse(List.of()).forGetter(recipe -> recipe.extraResultChances),
                        FluidStack.MAP_CODEC.fieldOf("result_fluid").orElse(FluidStack.EMPTY).forGetter(recipe -> recipe.resultFluid),
                        ExtraCodecs.POSITIVE_INT.fieldOf("energy_cost").orElse(defaultEnergyCost).forGetter(recipe -> recipe.energyCost)
                )
                .apply(instance, factory::create)
        ).validate(result -> {
            if (result.extraResultChances.size() > result.resultItems.size()) {
                return DataResult.error(() -> "Extra result chances must not be more than result items", result);
            }
            return DataResult.success(result);
        });
    }

    public static <T extends BaseSolidFluidReactingRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                recipe -> recipe.inputItem,
                ByteBufCodecs.VAR_INT,
                recipe -> recipe.inputCount,
                FluidIngredientAmount.STREAM_CODEC,
                recipe -> recipe.inputFluid,
                ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()),
                recipe -> recipe.resultItems,
                ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()),
                recipe -> recipe.extraResultChances,
                FluidStack.STREAM_CODEC,
                recipe -> recipe.resultFluid,
                ByteBufCodecs.VAR_INT,
                recipe -> recipe.energyCost,
                factory::create
        );
    }

    public final Ingredient inputItem;
    public final int inputCount;
    public final FluidIngredientAmount inputFluid;
    public final List<ItemStackTemplate> resultItems;
    public final List<Float> extraResultChances;
    public final FluidStack resultFluid;
    public final int energyCost;

    public BaseSolidFluidReactingRecipe(
            Ingredient inputItem,
            int inputCount,
            FluidIngredientAmount inputFluid,
            List<ItemStackTemplate> resultItems,
            List<Float> extraResultChances,
            FluidStack resultFluid,
            int energyCost
    ) {
        this.inputItem = inputItem;
        this.inputCount = inputCount;
        this.inputFluid = inputFluid;
        this.resultItems = resultItems;
        this.extraResultChances = extraResultChances;
        this.resultFluid = resultFluid;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(ItemFluidRecipeInput input, Level level) {
        return this.inputItem.test(input.item())
                && this.inputCount <= input.item().getCount()
                && this.inputFluid.test(input.fluid());
    }

    @Override
    public ItemStack assemble(ItemFluidRecipeInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @Nullable RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
