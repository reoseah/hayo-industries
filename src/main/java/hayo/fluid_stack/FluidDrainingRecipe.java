package hayo.fluid_stack;

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

public record FluidDrainingRecipe(
        Ingredient input,
        ItemStackTemplate resultItem,
        FluidStack resultFluid,
        int energyCost
) implements Recipe<SingleRecipeInput> {
    public static final MapCodec<FluidDrainingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Ingredient.CODEC.fieldOf("input").forGetter(FluidDrainingRecipe::input),
                    ItemStackTemplate.CODEC.fieldOf("result_item").forGetter(FluidDrainingRecipe::resultItem),
                    FluidStack.MAP_CODEC.codec().fieldOf("result_fluid").forGetter(FluidDrainingRecipe::resultFluid),
                    Codec.INT.fieldOf("energy_cost").forGetter(FluidDrainingRecipe::energyCost)
            )
            .apply(instance, FluidDrainingRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidDrainingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            FluidDrainingRecipe::input,
            ItemStackTemplate.STREAM_CODEC,
            FluidDrainingRecipe::resultItem,
            FluidStack.STREAM_CODEC,
            FluidDrainingRecipe::resultFluid,
            ByteBufCodecs.INT,
            FluidDrainingRecipe::energyCost,
            FluidDrainingRecipe::new
    );

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return Hayo.RecipeTypes.FLUID_DRAINING;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.FLUID_DRAINING;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return this.resultItem.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
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
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    public boolean showNotification() {
        return false;
    }
}
