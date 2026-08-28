package hayo.processing_machine.classic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.experimental.Accessors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

@Accessors(fluent = true)
public abstract class ClassicMachineRecipe extends SingleItemRecipe {
    public final int inputCount;
    public final float extraResultChance;
    public final int energyCost;

    protected ClassicMachineRecipe(
            Ingredient input,
            int inputCount,
            ItemStackTemplate result,
            float extraResultChance,
            int energyCost) {
        super(new Recipe.CommonInfo(false), input, result);
        this.inputCount = inputCount;
        this.energyCost = energyCost;
        this.extraResultChance = extraResultChance;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
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

    @Override
    public boolean isSpecial() {
        return true;
    }

    @FunctionalInterface
    public interface Factory<R extends ClassicMachineRecipe> {
        R create(Ingredient input, int inputCount, ItemStackTemplate result, float extraResultChance, int energyCost);
    }

    public static <R extends ClassicMachineRecipe> RecipeSerializer<R> createCodec(Factory<R> factory, int defaultEnergy) {
        var codec = RecordCodecBuilder.<R>mapCodec(
                instance -> instance.group(
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
                        Codec.INT.fieldOf("input_count").orElse(1).forGetter(r -> r.inputCount),
                        ItemStackTemplate.CODEC.fieldOf("result").forGetter(SingleItemRecipe::result),
                        Codec.FLOAT.fieldOf("extra_result_chance").orElse(0F).forGetter(r -> r.extraResultChance),
                        Codec.INT.fieldOf("energy_cost").orElse(defaultEnergy).forGetter(r -> r.energyCost)
                ).apply(instance, factory::create));

        var streamCodec = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                SingleItemRecipe::input,
                ByteBufCodecs.INT,
                r -> r.inputCount,
                ItemStackTemplate.STREAM_CODEC,
                SingleItemRecipe::result,
                ByteBufCodecs.FLOAT,
                r -> r.extraResultChance,
                ByteBufCodecs.INT,
                r -> r.energyCost,
                factory::create);

        return new RecipeSerializer<>(codec, streamCodec);
    }
}
