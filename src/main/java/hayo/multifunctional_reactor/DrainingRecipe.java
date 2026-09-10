package hayo.multifunctional_reactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.Hayo;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.material.Fluid;

public class DrainingRecipe extends SingleItemRecipe {
    public static final MapCodec<DrainingRecipe> CODEC = RecordCodecBuilder.<DrainingRecipe>mapCodec(instance -> instance
            .group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(DrainingRecipe::input),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(DrainingRecipe::result),
                    Codec.INT.fieldOf("energy_cost").forGetter(r -> r.energyCost),
                    RecordCodecBuilder.<ResourceAmount<Holder<Fluid>>>create(instance2 -> instance2
                                    .group(
                                            BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("id").forGetter(ResourceAmount::resource),
                                            ExtraCodecs.NON_NEGATIVE_LONG.fieldOf("amount").forGetter(ResourceAmount::amount)
                                    )
                                    .apply(instance2, ResourceAmount::new)
                            )
                            .fieldOf("fluid")
                            .forGetter(r -> r.fluid)
            )
            .apply(instance, DrainingRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DrainingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            DrainingRecipe::input,
            ItemStackTemplate.STREAM_CODEC,
            SingleItemRecipe::result,
            ByteBufCodecs.INT,
            r -> r.energyCost,
            StreamCodec.composite(
                    ByteBufCodecs.holderRegistry(Registries.FLUID),
                    ResourceAmount::resource,
                    ByteBufCodecs.VAR_LONG,
                    ResourceAmount::amount,
                    ResourceAmount::new
            ),
            r -> r.fluid,
            DrainingRecipe::new
    );

    public final int energyCost;
    public final ResourceAmount<Holder<Fluid>> fluid;

    public DrainingRecipe(Ingredient input, ItemStackTemplate result, int energyCost, ResourceAmount<Holder<Fluid>> fluid) {
        super(new Recipe.CommonInfo(false), input, result);
        this.energyCost = energyCost;
        this.fluid = fluid;
    }

    @Override
    public RecipeType<? extends SingleItemRecipe> getType() {
        return Hayo.RecipeTypes.DRAINING;
    }

    @Override
    public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
        return Hayo.RecipeSerializers.DRAINING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String group() {
        return "";
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
}
