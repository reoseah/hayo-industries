package hayo.energy;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.energy.item.EnergyComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class EnergyPreservingShapedRecipe extends ShapedRecipe {
    public static final MapCodec<EnergyPreservingShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
                            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
                            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                    )
                    .apply(i, EnergyPreservingShapedRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyPreservingShapedRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC,
            recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
            recipe -> recipe.bookInfo,
            ShapedRecipePattern.STREAM_CODEC,
            recipe -> recipe.pattern,
            ItemStackTemplate.STREAM_CODEC,
            recipe -> recipe.result,
            EnergyPreservingShapedRecipe::new
    );
    public static final RecipeSerializer<EnergyPreservingShapedRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public EnergyPreservingShapedRecipe(CommonInfo commonInfo, CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        var result = super.assemble(input);

        var resultStorage = result.get(EnergyComponents.ENERGY_STORAGE);
        if (resultStorage != null) {
            int totalEnergy = 0;
            for (int i = 0; i < input.size(); i++) {
                totalEnergy += EnergyComponents.getEnergy(input.getItem(i));
            }
            EnergyComponents.setEnergy(result, Math.min(totalEnergy / result.getCount(), resultStorage.capacity()));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }
}
