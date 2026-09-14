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

import java.util.List;

// TODO: extract into interface + default implementation
public record SolidFluidReactingRecipe(
        Ingredient inputItem,
        FluidInput inputFluid,
        List<ItemStackTemplate> resultItems,
        FluidStack resultFluid,
        int energyCost
) implements Recipe<ItemFluidPairRecipeInput> {
    public static final MapCodec<SolidFluidReactingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Ingredient.CODEC.fieldOf("input_item").forGetter(SolidFluidReactingRecipe::inputItem),
                    FluidInput.NON_EMPTY_CODEC.fieldOf("input_fluid").forGetter(SolidFluidReactingRecipe::inputFluid),
                    ItemStackTemplate.CODEC.listOf(0, 3).fieldOf("result_items").forGetter(SolidFluidReactingRecipe::resultItems),
                    FluidStack.MAP_CODEC.fieldOf("result_fluid").forGetter(SolidFluidReactingRecipe::resultFluid),
                    Codec.INT.fieldOf("energy_cost").forGetter(SolidFluidReactingRecipe::energyCost)
            )
            .apply(instance, SolidFluidReactingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SolidFluidReactingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            SolidFluidReactingRecipe::inputItem,
            FluidInput.STREAM_CODEC,
            SolidFluidReactingRecipe::inputFluid,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SolidFluidReactingRecipe::resultItems,
            FluidStack.STREAM_CODEC,
            SolidFluidReactingRecipe::resultFluid,
            ByteBufCodecs.VAR_INT,
            SolidFluidReactingRecipe::energyCost,
            SolidFluidReactingRecipe::new
    );

    @Override
    public RecipeType<? extends Recipe<ItemFluidPairRecipeInput>> getType() {
        return Hayo.RecipeTypes.SOLID_FLUID_REACTING;
    }

    @Override
    public RecipeSerializer<? extends Recipe<ItemFluidPairRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.SOLID_FLUID_REACTING;
    }

    @Override
    public boolean matches(ItemFluidPairRecipeInput input, Level level) {
        return this.inputItem.test(input.item()) && this.inputFluid.test(input.fluid());
    }

    @Override
    public ItemStack assemble(ItemFluidPairRecipeInput input) {
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
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }
}
