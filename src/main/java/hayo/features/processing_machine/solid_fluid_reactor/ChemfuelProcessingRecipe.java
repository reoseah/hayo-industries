package hayo.features.processing_machine.solid_fluid_reactor;

import com.mojang.serialization.MapCodec;
import hayo.Hayo;
import hayo.features.fluid_stack.FluidIngredientAmount;
import hayo.features.fluid_stack.FluidStack;
import hayo.features.fluid_stack.ItemFluidRecipeInput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public class ChemfuelProcessingRecipe extends BaseSolidFluidReactingRecipe {
    public static final MapCodec<ChemfuelProcessingRecipe> CODEC = codec(ChemfuelProcessingRecipe::new, 800);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemfuelProcessingRecipe> STREAM_CODEC = streamCodec(ChemfuelProcessingRecipe::new);

    public ChemfuelProcessingRecipe(
            Ingredient inputItem,
            int inputAmount,
            FluidIngredientAmount inputFluid,
            List<ItemTemplateWithChance> resultItems,
            FluidStack resultFluid,
            int energyCost
    ) {
        super(inputItem, inputAmount, inputFluid, resultItems, resultFluid, energyCost);
    }

    @Override
    public RecipeType<? extends Recipe<ItemFluidRecipeInput>> getType() {
        return Hayo.RecipeTypes.CHEMFUEL_PROCESSING;
    }

    @Override
    public RecipeSerializer<? extends Recipe<ItemFluidRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.CHEMFUEL_PROCESSING;
    }
}
