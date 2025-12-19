package io.github.reoseah.hayoind.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayoind.recipe.SimpleElectricRecipe;
import lombok.Getter;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SimpleElectricRecipeDisplay implements Display {
    public static final DisplaySerializer<SimpleElectricRecipeDisplay> SERIALIZER = DisplaySerializer.of( //
            RecordCodecBuilder.<SimpleElectricRecipeDisplay>mapCodec(instance -> instance.group( //
                    Codec.STRING.fieldOf("category").forGetter((SimpleElectricRecipeDisplay d) -> d.category.getIdentifier().toString()), //
                    ResourceLocation.CODEC.optionalFieldOf("location").forGetter(SimpleElectricRecipeDisplay::getDisplayLocation), //
                    EntryIngredient.codec().fieldOf("inputs").forGetter(SimpleElectricRecipeDisplay::getInput), //
                    EntryIngredient.codec().fieldOf("outputs").forGetter(SimpleElectricRecipeDisplay::getResult), //
                    Codec.INT.fieldOf("energy").forGetter(SimpleElectricRecipeDisplay::getProcessingEnergy) //
            ).apply(instance, SimpleElectricRecipeDisplay::new)), //
            StreamCodec.composite( //
                    ByteBufCodecs.STRING_UTF8, //
                    d -> d.category.getIdentifier().toString(), //
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), //
                    SimpleElectricRecipeDisplay::getDisplayLocation, //
                    EntryIngredient.streamCodec(), //
                    SimpleElectricRecipeDisplay::getInput, //
                    EntryIngredient.streamCodec(), //
                    SimpleElectricRecipeDisplay::getResult, //
                    ByteBufCodecs.INT, //
                    SimpleElectricRecipeDisplay::getProcessingEnergy, //
                    SimpleElectricRecipeDisplay::new));

    protected final CategoryIdentifier<?> category;
    @Getter
    protected final Optional<ResourceLocation> location;
    @Getter
    protected final EntryIngredient input;
    @Getter
    protected final EntryIngredient result;
    @Getter
    protected final int processingEnergy;

    public SimpleElectricRecipeDisplay(String category, Optional<ResourceLocation> location, EntryIngredient input, EntryIngredient result, int processingEnergy) {
        this.category = CategoryIdentifier.of(category);
        this.location = location;
        this.input = input;
        this.result = result;
        this.processingEnergy = processingEnergy;
    }

    public SimpleElectricRecipeDisplay(RecipeHolder<SimpleElectricRecipe> holder) {
        this.location = Optional.of(holder.id().location());

        var recipe = holder.value();
        this.category = CategoryIdentifier.of(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
        this.input = EntryIngredients.ofIngredient(recipe.input());
        this.result = EntryIngredients.of(recipe.result());
        this.processingEnergy = recipe.processingEnergy();
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(this.input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(this.result);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return this.category;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return this.location;
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
