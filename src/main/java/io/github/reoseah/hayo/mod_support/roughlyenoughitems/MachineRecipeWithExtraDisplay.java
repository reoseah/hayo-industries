package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.feature.processing_machines.ExtraChanceMachineRecipe;
import lombok.Getter;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MachineRecipeWithExtraDisplay extends MachineRecipeDisplay {
    public static final DisplaySerializer<MachineRecipeWithExtraDisplay> SERIALIZER = DisplaySerializer.of( //
            RecordCodecBuilder.mapCodec(instance -> instance.group( //
                    Codec.STRING.fieldOf("category").forGetter(display -> display.category.getIdentifier().toString()), //
                    ResourceLocation.CODEC.optionalFieldOf("location").forGetter(MachineRecipeWithExtraDisplay::getDisplayLocation), //
                    EntryIngredient.codec().fieldOf("inputs").forGetter(MachineRecipeWithExtraDisplay::getInput), //
                    EntryIngredient.codec().fieldOf("result").forGetter(MachineRecipeWithExtraDisplay::getResult), //
                    Codec.INT.fieldOf("energy").forGetter(MachineRecipeWithExtraDisplay::getProcessingEnergy), //
                    EntryIngredient.codec().fieldOf("extra_result").forGetter(MachineRecipeWithExtraDisplay::getExtraResult), //
                    Codec.FLOAT.fieldOf("extra_chance").forGetter(MachineRecipeWithExtraDisplay::getExtraChance) //
            ).apply(instance, MachineRecipeWithExtraDisplay::new)), //
            StreamCodec.composite( //
                    ByteBufCodecs.STRING_UTF8, //
                    display -> display.category.getIdentifier().toString(), //
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), //
                    MachineRecipeWithExtraDisplay::getDisplayLocation, //
                    EntryIngredient.streamCodec(), //
                    MachineRecipeWithExtraDisplay::getInput, //
                    EntryIngredient.streamCodec(), //
                    MachineRecipeWithExtraDisplay::getResult, //
                    ByteBufCodecs.INT, //
                    MachineRecipeWithExtraDisplay::getProcessingEnergy, //
                    EntryIngredient.streamCodec(), //
                    MachineRecipeWithExtraDisplay::getExtraResult, //
                    ByteBufCodecs.FLOAT, //
                    MachineRecipeWithExtraDisplay::getExtraChance, //
                    MachineRecipeWithExtraDisplay::new));

    @Getter
    public final EntryIngredient extraResult;
    @Getter
    public final float extraChance;

    public MachineRecipeWithExtraDisplay(String category, Optional<ResourceLocation> location, EntryIngredient input, EntryIngredient result, int processingEnergy, EntryIngredient extraResult, float extraChance) {
        super(category, location, input, result, processingEnergy);
        this.extraResult = extraResult;
        this.extraChance = extraChance;
    }

    public MachineRecipeWithExtraDisplay(RecipeHolder<? extends ExtraChanceMachineRecipe> holder) {
        super(holder);
        var recipe = holder.value();
        this.extraChance = recipe.getExtraChance();
        this.extraResult = this.extraChance > 0 ? EntryIngredients.of(recipe.result().copyWithCount(1)) : EntryIngredient.empty();
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(this.result, this.extraResult);
    }
}
