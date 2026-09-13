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

public record DrainingRecipe(Ingredient input, ItemStackTemplate result, int energyCost,
                             FluidStack fluid) implements Recipe<SingleRecipeInput> {
    public static final MapCodec<DrainingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(DrainingRecipe::input),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(DrainingRecipe::result),
                    Codec.INT.fieldOf("energy_cost").forGetter(DrainingRecipe::energyCost),
                    FluidStack.MAP_CODEC.codec().fieldOf("fluid").forGetter(DrainingRecipe::fluid)
            )
            .apply(instance, DrainingRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DrainingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            DrainingRecipe::input,
            ItemStackTemplate.STREAM_CODEC,
            DrainingRecipe::result,
            ByteBufCodecs.INT,
            DrainingRecipe::energyCost,
            FluidStack.STREAM_CODEC,
            DrainingRecipe::fluid,
            DrainingRecipe::new
    );

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return Hayo.RecipeTypes.DRAINING;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.DRAINING;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return this.result.create();
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
