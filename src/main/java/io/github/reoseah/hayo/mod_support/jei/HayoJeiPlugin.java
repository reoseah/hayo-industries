package io.github.reoseah.hayo.mod_support.jei;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressorMenu;
import io.github.reoseah.hayo.feature.processing_machines.electric_furnace.ElectricFurnaceMenu;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractorMenu;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratorMenu;
import io.github.reoseah.hayo.feature.processing_machines.matter_generator.MatterGeneratingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.List;
import java.util.function.Function;

@JeiPlugin
public class HayoJeiPlugin implements IModPlugin {
    public static final IRecipeType<RecipeHolder<SmeltingRecipe>> ELECTRIC_SMELTING = IRecipeType.create(Hayo.MOD_ID, "electric_smelting", (Class<? extends RecipeHolder<SmeltingRecipe>>) (Object) RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<BlastingRecipe>> ELECTRIC_BLASTING = IRecipeType.create(Hayo.MOD_ID, "electric_blasting", (Class<? extends RecipeHolder<BlastingRecipe>>) (Object) RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<SmokingRecipe>> ELECTRIC_SMOKING = IRecipeType.create(Hayo.MOD_ID, "electric_smoking", (Class<? extends RecipeHolder<SmokingRecipe>>) (Object) RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<MaceratingRecipe>> MACERATING = IRecipeType.create(Hayo.RecipeTypes.MACERATING);
    public static final IRecipeType<RecipeHolder<CompressingRecipe>> COMPRESSING = IRecipeType.create(Hayo.RecipeTypes.COMPRESSING);
    public static final IRecipeType<RecipeHolder<ExtractingRecipe>> EXTRACTING = IRecipeType.create(Hayo.RecipeTypes.EXTRACTING);
    public static final IRecipeType<RecipeHolder<MatterGeneratingRecipe>> MATTER_GENERATING = IRecipeType.create(Hayo.RecipeTypes.MATTER_GENERATING);

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
        registration.addRecipeCategories(new MatterGeneratingJeiCategory(drawable.apply(Hayo.Items.MATTER_GENERATOR)));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var synchronizedRecipes = Minecraft.getInstance().level.recipeAccess().getSynchronizedRecipes();

        registration.addRecipes(ELECTRIC_SMELTING, List.copyOf(synchronizedRecipes.getAllOfType(RecipeType.SMELTING)));
        registration.addRecipes(ELECTRIC_BLASTING, List.copyOf(synchronizedRecipes.getAllOfType(RecipeType.BLASTING)));
        registration.addRecipes(ELECTRIC_SMOKING, List.copyOf(synchronizedRecipes.getAllOfType(RecipeType.SMOKING)));

        registration.addRecipes(MACERATING, List.copyOf(synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.MACERATING)));
        registration.addRecipes(COMPRESSING, List.copyOf(synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.COMPRESSING)));
        registration.addRecipes(EXTRACTING, List.copyOf(synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.EXTRACTING)));
        registration.addRecipes(MATTER_GENERATING, List.copyOf(synchronizedRecipes.getAllOfType(Hayo.RecipeTypes.MATTER_GENERATING)));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ELECTRIC_SMELTING, new ItemStack(Hayo.Items.ELECTRIC_FURNACE));
        registration.addCraftingStation(ELECTRIC_BLASTING, new ItemStack(Hayo.Items.ELECTRIC_FURNACE), new ItemStack(Hayo.Items.BLASTING_UPGRADE));
        registration.addCraftingStation(ELECTRIC_SMOKING, new ItemStack(Hayo.Items.ELECTRIC_FURNACE), new ItemStack(Hayo.Items.SMOKING_UPGRADE));

        registration.addCraftingStation(MACERATING, new ItemStack(Hayo.Items.MACERATOR));
        registration.addCraftingStation(COMPRESSING, new ItemStack(Hayo.Items.COMPRESSOR));
        registration.addCraftingStation(EXTRACTING, new ItemStack(Hayo.Items.EXTRACTOR));
        registration.addCraftingStation(MATTER_GENERATING, new ItemStack(Hayo.Items.MATTER_GENERATOR));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ElectricFurnaceMenu.class, Hayo.MenuTypes.ELECTRIC_FURNACE, RecipeTypes.SMELTING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
        registration.addRecipeTransferHandler(ElectricFurnaceMenu.class, Hayo.MenuTypes.ELECTRIC_FURNACE, ELECTRIC_SMELTING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);

        registration.addRecipeTransferHandler(ElectricFurnaceMenu.class, Hayo.MenuTypes.ELECTRIC_FURNACE, RecipeTypes.BLASTING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
        registration.addRecipeTransferHandler(ElectricFurnaceMenu.class, Hayo.MenuTypes.ELECTRIC_FURNACE, ELECTRIC_BLASTING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);

        registration.addRecipeTransferHandler(ElectricFurnaceMenu.class, Hayo.MenuTypes.ELECTRIC_FURNACE, RecipeTypes.SMOKING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
        registration.addRecipeTransferHandler(ElectricFurnaceMenu.class, Hayo.MenuTypes.ELECTRIC_FURNACE, ELECTRIC_SMOKING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);

        registration.addRecipeTransferHandler(MaceratorMenu.class, Hayo.MenuTypes.MACERATOR, MACERATING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
        registration.addRecipeTransferHandler(CompressorMenu.class, Hayo.MenuTypes.COMPRESSOR, COMPRESSING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
        registration.addRecipeTransferHandler(ExtractorMenu.class, Hayo.MenuTypes.EXTRACTOR, EXTRACTING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
    }
}
