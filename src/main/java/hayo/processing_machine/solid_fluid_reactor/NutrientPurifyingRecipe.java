package hayo.processing_machine.solid_fluid_reactor;

import com.mojang.serialization.MapCodec;
import hayo.Hayo;
import hayo.fluid_stack.FluidIngredientAmount;
import hayo.fluid_stack.FluidStack;
import hayo.fluid_stack.ItemFluidRecipeInput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public class NutrientPurifyingRecipe extends BaseSolidFluidReactingRecipe {
    public static final MapCodec<NutrientPurifyingRecipe> CODEC = codec(NutrientPurifyingRecipe::new, 800);
    public static final StreamCodec<RegistryFriendlyByteBuf, NutrientPurifyingRecipe> STREAM_CODEC = streamCodec(NutrientPurifyingRecipe::new);

    public NutrientPurifyingRecipe(
            Ingredient inputItem,
            int inputAmount,
            FluidIngredientAmount inputFluid,
            List<ItemStackTemplate> resultItems,
            List<Float> extraResultChances,
            FluidStack resultFluid,
            int energyCost
    ) {
        super(inputItem, inputAmount, inputFluid, resultItems, extraResultChances, resultFluid, energyCost);
    }

    @Override
    public RecipeType<? extends Recipe<ItemFluidRecipeInput>> getType() {
        return Hayo.RecipeTypes.NUTRIENT_PURIFYING;
    }

    @Override
    public RecipeSerializer<? extends Recipe<ItemFluidRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.NUTRIENT_PURIFYING;
    }
}
