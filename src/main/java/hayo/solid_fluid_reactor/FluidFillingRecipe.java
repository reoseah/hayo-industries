package hayo.solid_fluid_reactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.Hayo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record FluidFillingRecipe(
        Ingredient inputItem,
        FluidIngredientAmount inputFluid,
        ItemStackTemplate resultItem,
        int energyCost
) implements Recipe<ItemFluidPairRecipeInput> {
    public static final MapCodec<FluidFillingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Ingredient.CODEC.fieldOf("input_item").forGetter(FluidFillingRecipe::inputItem),
                    FluidIngredientAmount.NON_EMPTY_CODEC.fieldOf("input_fluid").forGetter(FluidFillingRecipe::inputFluid),
                    ItemStackTemplate.CODEC.fieldOf("result_item").forGetter(FluidFillingRecipe::resultItem),
                    Codec.INT.fieldOf("energy_cost").forGetter(FluidFillingRecipe::energyCost)
            )
            .apply(instance, FluidFillingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFillingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            FluidFillingRecipe::inputItem,
            FluidIngredientAmount.STREAM_CODEC,
            FluidFillingRecipe::inputFluid,
            ItemStackTemplate.STREAM_CODEC,
            FluidFillingRecipe::resultItem,
            ByteBufCodecs.VAR_INT,
            FluidFillingRecipe::energyCost,
            FluidFillingRecipe::new
    );

    @Override
    public boolean matches(ItemFluidPairRecipeInput input, Level level) {
        return this.inputItem.test(input.item()) && this.inputFluid.test(input.fluid());
    }

    @Override
    public ItemStack assemble(ItemFluidPairRecipeInput input) {
        return this.resultItem.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
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
    public RecipeSerializer<? extends Recipe<ItemFluidPairRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.FLUID_FILLING;
    }

    @Override
    public RecipeType<? extends Recipe<ItemFluidPairRecipeInput>> getType() {
        return Hayo.RecipeTypes.FLUID_FILLING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }
}
