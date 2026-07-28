package io.github.reoseah.hayo.feature.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy.item.EnergyComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class EnergyPreservingShapedRecipe extends ShapedRecipe {
    public static final MapCodec<EnergyPreservingShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance //
            .group(Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::group), //
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category), //
                    ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern), //
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result), //
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification) //
            ) //
            .apply(instance, EnergyPreservingShapedRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyPreservingShapedRecipe> STREAM_CODEC = StreamCodec.of( //
            EnergyPreservingShapedRecipe::toNetwork, //
            EnergyPreservingShapedRecipe::fromNetwork //
    );

    public EnergyPreservingShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStackTemplate result, boolean showNotification) {
        // TODO: change JSON shape to match vanilla recipes?
        super(new CommonInfo(showNotification), new CraftingBookInfo(category, group), pattern, result);
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

    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) Hayo.RecipeSerializers.ENERGY_PRESERVING_CRAFTING;
    }

    private static EnergyPreservingShapedRecipe fromNetwork(RegistryFriendlyByteBuf input) {
        var group = input.readUtf();
        var category = input.readEnum(CraftingBookCategory.class);
        var pattern = ShapedRecipePattern.STREAM_CODEC.decode(input);
        var result = ItemStackTemplate.STREAM_CODEC.decode(input);
        boolean showNotification = input.readBoolean();
        return new EnergyPreservingShapedRecipe(group, category, pattern, result, showNotification);
    }

    private static void toNetwork(RegistryFriendlyByteBuf output, EnergyPreservingShapedRecipe recipe) {
        output.writeUtf(recipe.group());
        output.writeEnum(recipe.category());
        ShapedRecipePattern.STREAM_CODEC.encode(output, recipe.pattern);
        ItemStackTemplate.STREAM_CODEC.encode(output, recipe.result);
        output.writeBoolean(recipe.showNotification());
    }
}
