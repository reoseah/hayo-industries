package hayo.processing_machine.matter_generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.Hayo;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

@Accessors(fluent = true)
public record MatterGeneratingRecipe(ItemStackTemplate result, int energyCost,
                                     Holder<Item> requiredUpgrade) implements Recipe<MatterGeneratorRecipeInput> {
    public static final MapCodec<MatterGeneratingRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(MatterGeneratingRecipe::result),
                    Codec.INT.fieldOf("energy_cost").forGetter(MatterGeneratingRecipe::energyCost),
                    Item.CODEC.fieldOf("required_upgrade").orElse(Items.AIR.builtInRegistryHolder()).forGetter(MatterGeneratingRecipe::requiredUpgrade)
            ).apply(instance, MatterGeneratingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MatterGeneratingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC,
            MatterGeneratingRecipe::result,
            ByteBufCodecs.INT,
            MatterGeneratingRecipe::energyCost,
            Item.STREAM_CODEC,
            MatterGeneratingRecipe::requiredUpgrade,
            MatterGeneratingRecipe::new);

    @Override
    public boolean matches(MatterGeneratorRecipeInput input, Level level) {
        if (this.requiredUpgrade.value() == Items.AIR) {
            return true;
        }

        for (var upgrade : input.upgrades()) {
            if (upgrade.is(this.requiredUpgrade.value())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(MatterGeneratorRecipeInput input) {
        return this.result.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<MatterGeneratorRecipeInput>> getSerializer() {
        return Hayo.RecipeSerializers.MATTER_GENERATING;
    }

    @Override
    public RecipeType<? extends Recipe<MatterGeneratorRecipeInput>> getType() {
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
