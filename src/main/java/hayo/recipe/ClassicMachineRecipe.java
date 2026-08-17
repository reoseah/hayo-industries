package hayo.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.experimental.Accessors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

@Accessors(fluent = true)
public abstract class ClassicMachineRecipe implements Recipe<SingleRecipeInput> {
    public final Ingredient input;
    public final int inputCount;
    public final ItemStackTemplate result;
    public final float extraResultChance;
    public final int energyCost;

    protected ClassicMachineRecipe( //
            Ingredient input, //
            int inputCount, //
            ItemStackTemplate result, //
            float extraResultChance, //
            int energyCost) {
        this.input = input;
        this.inputCount = inputCount;
        this.result = result;
        this.energyCost = energyCost;
        this.extraResultChance = extraResultChance;
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
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @FunctionalInterface
    public interface Factory<R extends ClassicMachineRecipe> {
        R create(Ingredient input, int inputCount, ItemStackTemplate result, float extraResultChance, int energyCost);
    }

    public static <R extends ClassicMachineRecipe> RecipeSerializer<R> createCodec(Factory<R> factory, int defaultEnergy) {
        var codec = RecordCodecBuilder.<R>mapCodec( //
                instance -> instance.group( //
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.input), //
                        Codec.INT.fieldOf("input_count").orElse(1).forGetter(recipe -> recipe.inputCount), //
                        ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result), //
                        Codec.FLOAT.fieldOf("extra_result_chance").orElse(0F).forGetter(recipe -> recipe.extraResultChance), //
                        Codec.INT.fieldOf("processing_energy").orElse(defaultEnergy).forGetter(recipe -> recipe.energyCost) //
                ).apply(instance, factory::create));

        var streamCodec = StreamCodec.composite( //
                Ingredient.CONTENTS_STREAM_CODEC, //
                recipe -> recipe.input, //
                ByteBufCodecs.INT, //
                recipe -> recipe.inputCount, //
                ItemStackTemplate.STREAM_CODEC, //
                recipe -> recipe.result, //
                ByteBufCodecs.FLOAT, //
                recipe -> recipe.extraResultChance, //
                ByteBufCodecs.INT,
                recipe -> recipe.energyCost, //
                factory::create);

        return new RecipeSerializer<>(codec, streamCodec);
    }
}
