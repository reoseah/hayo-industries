package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.feature.processing_machines.BasicMachineRecipe;
import io.github.reoseah.hayo.feature.processing_machines.electric_furnace.ElectricFurnaceBlockEntity;
import io.github.reoseah.hayo.mixin.SingleItemRecipeAccessor;
import lombok.Getter;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MachineRecipeDisplay implements Display {
    public static final DisplaySerializer<MachineRecipeDisplay> SERIALIZER = DisplaySerializer.of( //
            RecordCodecBuilder.mapCodec(instance -> instance.group( //
                    Codec.STRING.fieldOf("category").forGetter(display -> display.category.getIdentifier().toString()), //
                    Identifier.CODEC.optionalFieldOf("identifier").forGetter(MachineRecipeDisplay::getDisplayLocation), //
                    EntryIngredient.codec().fieldOf("inputs").forGetter(MachineRecipeDisplay::getInput), //
                    EntryIngredient.codec().fieldOf("outputs").forGetter(MachineRecipeDisplay::getResult), //
                    Codec.INT.fieldOf("energy").forGetter(MachineRecipeDisplay::getProcessingEnergy) //
            ).apply(instance, MachineRecipeDisplay::new)), //
            StreamCodec.composite( //
                    ByteBufCodecs.STRING_UTF8, //
                    display -> display.category.getIdentifier().toString(), //
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC), //
                    MachineRecipeDisplay::getDisplayLocation, //
                    EntryIngredient.streamCodec(), //
                    MachineRecipeDisplay::getInput, //
                    EntryIngredient.streamCodec(), //
                    MachineRecipeDisplay::getResult, //
                    ByteBufCodecs.INT, //
                    MachineRecipeDisplay::getProcessingEnergy, //
                    MachineRecipeDisplay::new));

    protected final CategoryIdentifier<?> category;
    @Getter
    protected final Optional<Identifier> identifier;
    @Getter
    protected final EntryIngredient input;
    @Getter
    protected final EntryIngredient result;
    @Getter
    protected final int processingEnergy;

    public MachineRecipeDisplay(String category, Optional<Identifier> identifier, EntryIngredient input, EntryIngredient result, int processingEnergy) {
        this.category = CategoryIdentifier.of(category);
        this.identifier = identifier;
        this.input = input;
        this.result = result;
        this.processingEnergy = processingEnergy;
    }

    public MachineRecipeDisplay(RecipeHolder<? extends BasicMachineRecipe> holder) {
        this.identifier = Optional.of(holder.id().identifier());

        var recipe = holder.value();
        this.category = CategoryIdentifier.of(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
        this.input = EntryIngredients.ofIngredient(recipe.input());
        this.result = EntryIngredients.of(recipe.result());
        this.processingEnergy = recipe.processingEnergy();
    }

    public static MachineRecipeDisplay fromCookingRecipe(RecipeHolder<AbstractCookingRecipe> holder) {
        var identifier = Optional.of(holder.id().identifier());

        var recipe = holder.value();
        var path = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).getPath();
        var category = "hayo:electric_" + path;

        var input = EntryIngredients.ofIngredient(recipe.input());
        var result = EntryIngredients.of(((SingleItemRecipeAccessor) recipe).hayo$getResult());
        var processingEnergy = ElectricFurnaceBlockEntity.energyCostFromCookingTime(recipe.cookingTime());

        return new MachineRecipeDisplay(category, identifier, input, result, processingEnergy);
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
    public Optional<Identifier> getDisplayLocation() {
        return this.identifier;
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
