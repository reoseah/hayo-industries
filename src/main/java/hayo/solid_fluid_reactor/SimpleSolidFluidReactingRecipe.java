package hayo.solid_fluid_reactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.Hayo;
import hayo.fluid_stack.FluidIngredientAmount;
import hayo.fluid_stack.FluidStack;
import hayo.fluid_stack.ItemFluidRecipeInput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public record SimpleSolidFluidReactingRecipe(
        Ingredient inputItem,
        FluidIngredientAmount inputFluid,
        List<ItemStackTemplate> resultItems,
        FluidStack resultFluid,
        int energyCost
) implements SolidFluidReactingRecipe {
    public static final MapCodec<SimpleSolidFluidReactingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Ingredient.CODEC.fieldOf("input_item").forGetter(SimpleSolidFluidReactingRecipe::inputItem),
                    FluidIngredientAmount.NON_EMPTY_CODEC.fieldOf("input_fluid").forGetter(SimpleSolidFluidReactingRecipe::inputFluid),
                    ItemStackTemplate.CODEC.listOf(0, 3).fieldOf("result_items").orElse(List.of()).forGetter(SimpleSolidFluidReactingRecipe::resultItems),
                    FluidStack.MAP_CODEC.fieldOf("result_fluid").orElse(FluidStack.EMPTY).forGetter(SimpleSolidFluidReactingRecipe::resultFluid),
                    Codec.INT.fieldOf("energy_cost").forGetter(SimpleSolidFluidReactingRecipe::energyCost)
            )
            .apply(instance, SimpleSolidFluidReactingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SimpleSolidFluidReactingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            SimpleSolidFluidReactingRecipe::inputItem,
            FluidIngredientAmount.STREAM_CODEC,
            SimpleSolidFluidReactingRecipe::inputFluid,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SimpleSolidFluidReactingRecipe::resultItems,
            FluidStack.STREAM_CODEC,
            SimpleSolidFluidReactingRecipe::resultFluid,
            ByteBufCodecs.VAR_INT,
            SimpleSolidFluidReactingRecipe::energyCost,
            SimpleSolidFluidReactingRecipe::new
    );

    @Override
    public RecipeType<? extends Recipe<ItemFluidRecipeInput>> getType() {
        return Hayo.RecipeTypes.SOLID_FLUID_REACTING;
    }

    @Override
    public RecipeSerializer<? extends Recipe<ItemFluidRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.SOLID_FLUID_REACTING;
    }

    @Override
    public boolean matches(ItemFluidRecipeInput input, Level level) {
        return this.inputItem.test(input.item()) && this.inputFluid.test(input.fluid());
    }
}
