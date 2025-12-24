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

public abstract class SideProductMachineRecipe extends SimpleMachineRecipe {
    @Getter
    private final ItemStack secondaryResult;
    @Getter
    private final float secondaryResultChance;

    public SideProductMachineRecipe(Ingredient input, ItemStack result, int processingEnergy, ItemStack secondaryResult, float secondaryResultChance) {
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

    public static class Serializer<R extends SideProductMachineRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(Factory<R> factory, int defaultEnergy) {
            this.codec = RecordCodecBuilder.mapCodec( //
                    instance -> instance.group( //
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(SideProductMachineRecipe::input), //
                            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SideProductMachineRecipe::result), //
                            Codec.INT.fieldOf("processing_energy").orElse(defaultEnergy).forGetter(SideProductMachineRecipe::processingEnergy), //
                            ItemStack.STRICT_CODEC.fieldOf("secondary_result").orElse(ItemStack.EMPTY).forGetter(SideProductMachineRecipe::getSecondaryResult), //
                            Codec.FLOAT.fieldOf("secondary_result_chance").orElse(1F).forGetter(SideProductMachineRecipe::getSecondaryResultChance) //
                    ).apply(instance, factory::create));

            this.streamCodec = StreamCodec.composite( //
                    Ingredient.CONTENTS_STREAM_CODEC, //
                    SideProductMachineRecipe::input, //
                    ItemStack.STREAM_CODEC, //
                    SideProductMachineRecipe::result, //
                    ByteBufCodecs.INT, //
                    SideProductMachineRecipe::processingEnergy, //
                    ItemStack.STREAM_CODEC, //
                    SideProductMachineRecipe::getSecondaryResult, //
                    ByteBufCodecs.FLOAT, //
                    SideProductMachineRecipe::getSecondaryResultChance, //
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
