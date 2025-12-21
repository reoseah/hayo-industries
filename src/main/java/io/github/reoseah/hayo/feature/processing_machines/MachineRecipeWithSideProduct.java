package io.github.reoseah.hayo.feature.processing_machines;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public abstract class MachineRecipeWithSideProduct extends SimpleMachineRecipe {
    @Getter
    private final ItemStack secondaryResult;
    @Getter
    private final float secondaryResultChance;

    public MachineRecipeWithSideProduct(Ingredient input, ItemStack result, int processingEnergy, ItemStack secondaryResult, float secondaryResultChance) {
        super(input, result, processingEnergy);
        this.secondaryResult = secondaryResult;
        this.secondaryResultChance = secondaryResultChance;
    }

    @Override
    @Deprecated
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return super.assemble(input, registries);
    }

    @FunctionalInterface
    public interface Factory<R extends SimpleMachineRecipe> {
        R create(Ingredient input, ItemStack result, int processingEnergy, ItemStack secondaryResult, float secondaryResultChance);
    }

    public static class Serializer<R extends MachineRecipeWithSideProduct> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(Factory<R> factory, int defaultEnergy) {
            this.codec = RecordCodecBuilder.mapCodec( //
                    instance -> instance.group( //
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(MachineRecipeWithSideProduct::input), //
                            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MachineRecipeWithSideProduct::result), //
                            Codec.INT.fieldOf("processing_energy").orElse(defaultEnergy).forGetter(MachineRecipeWithSideProduct::processingEnergy), //
                            ItemStack.STRICT_CODEC.fieldOf("secondary_result").orElse(ItemStack.EMPTY).forGetter(MachineRecipeWithSideProduct::getSecondaryResult), //
                            Codec.FLOAT.fieldOf("secondary_result_chance").orElse(1F).forGetter(MachineRecipeWithSideProduct::getSecondaryResultChance) //
                    ).apply(instance, factory::create));

            this.streamCodec = StreamCodec.composite( //
                    Ingredient.CONTENTS_STREAM_CODEC, //
                    MachineRecipeWithSideProduct::input, //
                    ItemStack.STREAM_CODEC, //
                    MachineRecipeWithSideProduct::result, //
                    ByteBufCodecs.INT, //
                    MachineRecipeWithSideProduct::processingEnergy, //
                    ItemStack.STREAM_CODEC, //
                    MachineRecipeWithSideProduct::getSecondaryResult, //
                    ByteBufCodecs.FLOAT, //
                    MachineRecipeWithSideProduct::getSecondaryResultChance, //
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
