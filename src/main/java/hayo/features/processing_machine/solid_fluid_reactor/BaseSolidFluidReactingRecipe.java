package hayo.features.processing_machine.solid_fluid_reactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.features.fluid_stack.FluidIngredientAmount;
import hayo.features.fluid_stack.FluidStack;
import hayo.features.fluid_stack.ItemFluidRecipeInput;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseSolidFluidReactingRecipe implements Recipe<ItemFluidRecipeInput> {
    @FunctionalInterface
    public interface Factory<T extends BaseSolidFluidReactingRecipe> {
        T create(
                Ingredient inputItem,
                int inputAmount,
                FluidIngredientAmount inputFluid,
                List<ItemTemplateWithChance> resultItems,
                FluidStack resultFluid,
                int energyCost
        );
    }

    public static final Codec<List<ItemTemplateWithChance>> OUTPUTS_CODEC = ItemTemplateWithChance.MAP_CODEC.codec().listOf(0, 6)
            .validate(list -> {
                Set<ItemStackTemplate> templates = new HashSet<>();
                for (var entry : list) {
                    templates.add(entry.template());
                    if (templates.size() > 3) {
                        return DataResult.error(() -> "At most 3 distinct outputs are allowed", list);
                    }
                }

                return DataResult.success(list);
            });

    public static <T extends BaseSolidFluidReactingRecipe> MapCodec<T> codec(Factory<T> factory, int defaultEnergyCost) {
        return RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        Ingredient.CODEC.fieldOf("item_ingredient").forGetter(recipe -> recipe.inputItem),
                        ExtraCodecs.POSITIVE_INT.fieldOf("input_count").orElse(1).forGetter(recipe -> recipe.inputCount),
                        FluidIngredientAmount.NON_EMPTY_CODEC.fieldOf("fluid_ingredient").forGetter(recipe -> recipe.inputFluid),
                        ItemTemplateWithChance.MAP_CODEC.codec().listOf(0, 6).fieldOf("item_results").orElse(List.of()).forGetter(recipe -> recipe.resultItems),
                        FluidStack.MAP_CODEC.fieldOf("fluid_result").orElse(FluidStack.EMPTY).forGetter(recipe -> recipe.resultFluid),
                        ExtraCodecs.POSITIVE_INT.fieldOf("energy_cost").orElse(defaultEnergyCost).forGetter(recipe -> recipe.energyCost)
                )
                .apply(instance, factory::create)
        );
    }

    public static <T extends BaseSolidFluidReactingRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                recipe -> recipe.inputItem,
                ByteBufCodecs.VAR_INT,
                recipe -> recipe.inputCount,
                FluidIngredientAmount.STREAM_CODEC,
                recipe -> recipe.inputFluid,
                ItemTemplateWithChance.STREAM_CODEC.apply(ByteBufCodecs.list()),
                recipe -> recipe.resultItems,
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
    public final List<ItemTemplateWithChance> resultItems;
    public final FluidStack resultFluid;
    public final int energyCost;

    public BaseSolidFluidReactingRecipe(
            Ingredient inputItem,
            int inputCount,
            FluidIngredientAmount inputFluid,
            List<ItemTemplateWithChance> resultItems,
            FluidStack resultFluid,
            int energyCost
    ) {
        this.inputItem = inputItem;
        this.inputCount = inputCount;
        this.inputFluid = inputFluid;
        this.resultItems = resultItems;
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
