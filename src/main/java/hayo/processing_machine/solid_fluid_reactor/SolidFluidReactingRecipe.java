package hayo.processing_machine.solid_fluid_reactor;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class SolidFluidReactingRecipe implements Recipe<ItemFluidRecipeInput> {
    public static final MapCodec<SolidFluidReactingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Ingredient.CODEC.fieldOf("input_item").forGetter(solidFluidReactingRecipe -> solidFluidReactingRecipe.inputItem),
                    FluidIngredientAmount.NON_EMPTY_CODEC.fieldOf("input_fluid").forGetter(solidFluidReactingRecipe -> solidFluidReactingRecipe.inputFluid),
                    ItemStackTemplate.CODEC.listOf(0, 3).fieldOf("result_items").orElse(List.of()).forGetter(solidFluidReactingRecipe -> solidFluidReactingRecipe.resultItems),
                    FluidStack.MAP_CODEC.fieldOf("result_fluid").orElse(FluidStack.EMPTY).forGetter(solidFluidReactingRecipe -> solidFluidReactingRecipe.resultFluid),
                    Codec.INT.fieldOf("energy_cost").forGetter(solidFluidReactingRecipe -> solidFluidReactingRecipe.energyCost)
            )
            .apply(instance, SolidFluidReactingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SolidFluidReactingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            solidFluidReactingRecipe -> solidFluidReactingRecipe.inputItem,
            FluidIngredientAmount.STREAM_CODEC,
            solidFluidReactingRecipe -> solidFluidReactingRecipe.inputFluid,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            solidFluidReactingRecipe -> solidFluidReactingRecipe.resultItems,
            FluidStack.STREAM_CODEC,
            solidFluidReactingRecipe -> solidFluidReactingRecipe.resultFluid,
            ByteBufCodecs.VAR_INT,
            solidFluidReactingRecipe -> solidFluidReactingRecipe.energyCost,
            SolidFluidReactingRecipe::new
    );
    public final Ingredient inputItem;
    public final FluidIngredientAmount inputFluid;
    public final List<ItemStackTemplate> resultItems;
    public final FluidStack resultFluid;
    public final int energyCost;

    public SolidFluidReactingRecipe(
            Ingredient inputItem,
            FluidIngredientAmount inputFluid,
            List<ItemStackTemplate> resultItems,
            FluidStack resultFluid,
            int energyCost
    ) {
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.resultItems = resultItems;
        this.resultFluid = resultFluid;
        this.energyCost = energyCost;
    }

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
