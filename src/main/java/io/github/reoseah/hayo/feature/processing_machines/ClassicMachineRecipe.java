package io.github.reoseah.hayo.feature.processing_machines;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

@Accessors(fluent = true)
public abstract class ClassicMachineRecipe implements Recipe<SingleRecipeInput>, ElectricCostProvider {
    @Getter
    private final Ingredient input;
    @Getter
    private final ItemStack result;
    private final int processingEnergy;
    @Getter
    public final float extraChance;

    public ClassicMachineRecipe(Ingredient input, //
                                ItemStack result, //
                                int processingEnergy, //
                                float extraChance) {
        this.input = input;
        this.result = result;
        this.processingEnergy = processingEnergy;
        this.extraChance = extraChance;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
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
    public int getEnergyCost() {
        return this.processingEnergy;
    }

    @FunctionalInterface
    public interface Factory<R extends ClassicMachineRecipe> {
        R create(Ingredient input, ItemStack result, int processingEnergy, float extraChance);
    }

    public static class Serializer<R extends ClassicMachineRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(Factory<R> factory, int defaultEnergy) {
            this.codec = RecordCodecBuilder.mapCodec( //
                    instance -> instance.group( //
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(ClassicMachineRecipe::input), //
                            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ClassicMachineRecipe::result), //
                            Codec.INT.fieldOf("processing_energy").orElse(defaultEnergy).forGetter(ClassicMachineRecipe::getEnergyCost), //
                            Codec.FLOAT.fieldOf("extra_chance").orElse(0F).forGetter(ClassicMachineRecipe::extraChance) //
                    ).apply(instance, factory::create));
            this.streamCodec = StreamCodec.composite( //
                    Ingredient.CONTENTS_STREAM_CODEC, //
                    ClassicMachineRecipe::input, //
                    ItemStack.STREAM_CODEC, //
                    ClassicMachineRecipe::result, //
                    ByteBufCodecs.INT, //
                    ClassicMachineRecipe::getEnergyCost, //
                    ByteBufCodecs.FLOAT, //
                    ClassicMachineRecipe::extraChance, //
                    factory::create);
        }

        @Override
        public MapCodec<R> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return this.streamCodec;
        }
    }

}
