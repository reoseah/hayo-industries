package hayo.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.Hayo;
import lombok.experimental.Accessors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

@Accessors(fluent = true)
public record MatterGeneratingRecipe(ItemStackTemplate result, int energyCost) implements Recipe<EmptyRecipeInput> {
    public static final MapCodec<MatterGeneratingRecipe> CODEC = RecordCodecBuilder.mapCodec( //
            instance -> instance.group( //
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result), //
                    Codec.INT.fieldOf("processing_energy").forGetter(recipe -> recipe.energyCost) //
            ).apply(instance, MatterGeneratingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MatterGeneratingRecipe> STREAM_CODEC = StreamCodec.composite( //
            ItemStackTemplate.STREAM_CODEC, //
            recipe -> recipe.result, //
            ByteBufCodecs.INT, //
            recipe -> recipe.energyCost, //
            MatterGeneratingRecipe::new);

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

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }
}
