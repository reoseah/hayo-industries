package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.function.Function;

@JeiPlugin
public class HayoJeiPlugin implements IModPlugin {
    public static final IRecipeType<RecipeHolder<SmeltingRecipe>> ELECTRIC_SMELTING = IRecipeType.create(Hayo.MOD_ID, "electric_smelting", (Class<? extends RecipeHolder<SmeltingRecipe>>) (Object) RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<BlastingRecipe>> ELECTRIC_BLASTING = IRecipeType.create(Hayo.MOD_ID, "electric_blasting", (Class<? extends RecipeHolder<BlastingRecipe>>) (Object) RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<SmokingRecipe>> ELECTRIC_SMOKING = IRecipeType.create(Hayo.MOD_ID, "electric_smoking", (Class<? extends RecipeHolder<SmokingRecipe>>) (Object) RecipeHolder.class);
    public static final IRecipeType<MaceratingRecipe> MACERATING = IRecipeType.create(Hayo.MOD_ID, "macerating", MaceratingRecipe.class);
    public static final IRecipeType<CompressingRecipe> COMPRESSING = IRecipeType.create(Hayo.MOD_ID, "compressing", CompressingRecipe.class);
    public static final IRecipeType<ExtractingRecipe> EXTRACTING = IRecipeType.create(Hayo.MOD_ID, "extracting", ExtractingRecipe.class);

    @Override
    public Identifier getPluginUid() {
        return Hayo.modId("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        Function<Item, IDrawable> drawable = (item) -> registration.getJeiHelpers().getGuiHelper().createDrawableItemLike(item);

        registration.addRecipeCategories(new ElectricCookingJeiCategory(ELECTRIC_SMELTING, Component.translatable("hayo.electric_smelting"), drawable.apply(Hayo.Items.ELECTRIC_FURNACE)));
        registration.addRecipeCategories(new ElectricCookingJeiCategory(ELECTRIC_BLASTING, Component.translatable("hayo.electric_blasting"), drawable.apply(Hayo.Items.ELECTRIC_FURNACE)));
        registration.addRecipeCategories(new ElectricCookingJeiCategory(ELECTRIC_SMOKING, Component.translatable("hayo.electric_smoking"), drawable.apply(Hayo.Items.ELECTRIC_FURNACE)));
        registration.addRecipeCategories(new ClassicMachineRecipeJeiCategory(MACERATING, 2, HayoGuiSprites.RecipeArrow.MACERATOR, Component.translatable("hayo.macerating"), drawable.apply(Hayo.Items.MACERATOR)));
        registration.addRecipeCategories(new ClassicMachineRecipeJeiCategory(COMPRESSING, 2, HayoGuiSprites.RecipeArrow.COMPRESSOR, Component.translatable("hayo.compressing"), drawable.apply(Hayo.Items.COMPRESSOR)));
        registration.addRecipeCategories(new ClassicMachineRecipeJeiCategory(EXTRACTING, 2, HayoGuiSprites.RecipeArrow.EXTRACTOR, Component.translatable("hayo.extracting"), drawable.apply(Hayo.Items.EXTRACTOR)));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var synchronizedRecipes = Minecraft.getInstance().level.recipeAccess().getSynchronizedRecipes();

        registration.addRecipes(ELECTRIC_SMELTING, synchronizedRecipes.getAllOfType(RecipeType.SMELTING).stream().toList());
        registration.addRecipes(ELECTRIC_BLASTING, synchronizedRecipes.getAllOfType(RecipeType.BLASTING).stream().toList());
        registration.addRecipes(ELECTRIC_SMOKING, synchronizedRecipes.getAllOfType(RecipeType.SMOKING).stream().toList());

        var macerating = synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.MACERATING);
        registration.addRecipes(MACERATING, macerating.stream().map(RecipeHolder::value).toList());

        var compressing = synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.COMPRESSING);
        registration.addRecipes(COMPRESSING, compressing.stream().map(RecipeHolder::value).toList());

        var extracting = synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.EXTRACTING);
        registration.addRecipes(EXTRACTING, extracting.stream().map(RecipeHolder::value).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ELECTRIC_SMELTING, new ItemStack(Hayo.Items.ELECTRIC_FURNACE));
        registration.addCraftingStation(ELECTRIC_BLASTING, new ItemStack(Hayo.Items.ELECTRIC_FURNACE), new ItemStack(Hayo.Items.BLASTING_UPGRADE));
        registration.addCraftingStation(ELECTRIC_SMOKING, new ItemStack(Hayo.Items.ELECTRIC_FURNACE), new ItemStack(Hayo.Items.SMOKING_UPGRADE));

        registration.addCraftingStation(MACERATING, new ItemStack(Hayo.Items.MACERATOR));
        registration.addCraftingStation(COMPRESSING, new ItemStack(Hayo.Items.COMPRESSOR));
        registration.addCraftingStation(EXTRACTING, new ItemStack(Hayo.Items.EXTRACTOR));

    }
}
