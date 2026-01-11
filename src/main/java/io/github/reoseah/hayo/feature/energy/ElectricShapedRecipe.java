package io.github.reoseah.hayo.feature.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy.items.ElectricItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class ElectricShapedRecipe extends ShapedRecipe {
    public ElectricShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    public ElectricShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result) {
        super(group, category, pattern, result);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var result = super.assemble(input, registries);

        if (result.getItem() instanceof ElectricItem resultElectricItem) {
            int totalEnergy = 0;
            for (int i = 0; i < input.size(); i++) {
                var stack = input.getItem(i);
                if (stack.getItem() instanceof ElectricItem electricItem) {
                    totalEnergy += electricItem.getEnergy(stack);
                }
            }
            resultElectricItem.setEnergy(result, totalEnergy);
        }

        return result;
    }

    @Override
    public RecipeSerializer<? extends ShapedRecipe> getSerializer() {
        return Hayo.RecipeSerializers.ELECTRIC_SHAPED_CRAFTING;
    }

    public static class Serializer implements RecipeSerializer<ElectricShapedRecipe> {
        public static final MapCodec<ElectricShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance //
                .group(Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::group), //
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category), //
                        ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern), //
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(o -> o.result), //
                        Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification) //
                ) //
                .apply(instance, ElectricShapedRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ElectricShapedRecipe> STREAM_CODEC = StreamCodec.of( //
                ElectricShapedRecipe.Serializer::toNetwork, //
                ElectricShapedRecipe.Serializer::fromNetwork //
        );

        @Override
        public MapCodec<ElectricShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ElectricShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ElectricShapedRecipe fromNetwork(RegistryFriendlyByteBuf input) {
            var group = input.readUtf();
            var category = input.readEnum(CraftingBookCategory.class);
            var pattern = ShapedRecipePattern.STREAM_CODEC.decode(input);
            var result = ItemStack.STREAM_CODEC.decode(input);
            boolean showNotification = input.readBoolean();
            return new ElectricShapedRecipe(group, category, pattern, result, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf output, ElectricShapedRecipe recipe) {
            output.writeUtf(recipe.group());
            output.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(output, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(output, recipe.result);
            output.writeBoolean(recipe.showNotification());
        }
    }
}
