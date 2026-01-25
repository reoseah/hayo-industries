package io.github.reoseah.hayo.feature.processing_machines.matter_generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.ElectricCostProvider;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

@Accessors(fluent = true)
public class MatterGeneratingRecipe implements Recipe<EmptyRecipeInput>, ElectricCostProvider {
    @Getter
    private final ItemStackTemplate result;
    private final int processingEnergy;

    public MatterGeneratingRecipe(ItemStackTemplate result, int processingEnergy) {
        this.result = result;
        this.processingEnergy = processingEnergy;
    }

    @Override
    public int getEnergyCost() {
        return this.processingEnergy;
    }

    @Override
    public boolean matches(EmptyRecipeInput input, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(EmptyRecipeInput input) {
        return this.result.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<EmptyRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.MATTER_GENERATING;
    }

    @Override
    public RecipeType<? extends Recipe<EmptyRecipeInput>> getType() {
        return Hayo.RecipeTypes.MATTER_GENERATING;
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
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<MatterGeneratingRecipe> {
        private static final MapCodec<MatterGeneratingRecipe> CODEC = RecordCodecBuilder.mapCodec( //
                instance -> instance.group( //
                        ItemStackTemplate.CODEC.fieldOf("result").forGetter(MatterGeneratingRecipe::result), //
                        Codec.INT.fieldOf("processing_energy").forGetter(MatterGeneratingRecipe::getEnergyCost) //
                ).apply(instance, MatterGeneratingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MatterGeneratingRecipe> STREAM_CODEC = StreamCodec.composite( //
                ItemStackTemplate.STREAM_CODEC, //
                MatterGeneratingRecipe::result, //
                ByteBufCodecs.INT, //
                MatterGeneratingRecipe::getEnergyCost, //
                MatterGeneratingRecipe::new);

        @Override
        public MapCodec<MatterGeneratingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MatterGeneratingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
