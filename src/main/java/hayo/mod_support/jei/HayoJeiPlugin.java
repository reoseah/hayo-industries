package hayo.mod_support.jei;

import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.processing_machine.classic.*;
import hayo.processing_machine.matter_generator.MatterGeneratingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@JeiPlugin
public class HayoJeiPlugin implements IModPlugin {
    public static final IRecipeType<RecipeHolder<SmeltingRecipe>> ELECTRIC_SMELTING = (IRecipeType<RecipeHolder<SmeltingRecipe>>) (IRecipeType<?>) IRecipeType.create(Hayo.MOD_ID, "electric_smelting", RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<BlastingRecipe>> ELECTRIC_BLASTING = (IRecipeType<RecipeHolder<BlastingRecipe>>) (IRecipeType<?>) IRecipeType.create(Hayo.MOD_ID, "electric_blasting", RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<SmokingRecipe>> ELECTRIC_SMOKING = (IRecipeType<RecipeHolder<SmokingRecipe>>) (IRecipeType<?>) IRecipeType.create(Hayo.MOD_ID, "electric_smoking", RecipeHolder.class);
    public static final IRecipeType<RecipeHolder<MaceratingRecipe>> MACERATING = IRecipeType.create(Hayo.RecipeTypes.MACERATING);
    public static final IRecipeType<RecipeHolder<CompressingRecipe>> COMPRESSING = IRecipeType.create(Hayo.RecipeTypes.COMPRESSING);
    public static final IRecipeType<RecipeHolder<ExtractingRecipe>> EXTRACTING = IRecipeType.create(Hayo.RecipeTypes.EXTRACTING);
    public static final IRecipeType<RecipeHolder<MatterGeneratingRecipe>> MATTER_GENERATING = IRecipeType.create(Hayo.RecipeTypes.MATTER_GENERATING);

    @Override
    public Identifier getPluginUid() {
        return Hayo.modId("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        Function<Item, IDrawable> drawable = (item) -> registration.getJeiHelpers().getGuiHelper().createDrawableItemLike(item);

        registration.addRecipeCategories(new ElectricCookingJeiCategory(ELECTRIC_SMELTING, Component.translatable("hayo.recipe_type.electric_smelting"), drawable.apply(Hayo.Items.ELECTRIC_FURNACE)));
        registration.addRecipeCategories(new ElectricCookingJeiCategory(ELECTRIC_BLASTING, Component.translatable("hayo.recipe_type.electric_blasting"), drawable.apply(Hayo.Items.ELECTRIC_FURNACE)));
        registration.addRecipeCategories(new ElectricCookingJeiCategory(ELECTRIC_SMOKING, Component.translatable("hayo.recipe_type.electric_smoking"), drawable.apply(Hayo.Items.ELECTRIC_FURNACE)));
        registration.addRecipeCategories(new ClassicMachineRecipeJeiCategory(MACERATING, 2, HayoGuiSprites.MACERATING_ARROW, HayoGuiSprites.MACERATING_ARROW_OVERLAY, Component.translatable("hayo.recipe_type.macerating"), drawable.apply(Hayo.Items.MACERATOR)));
        registration.addRecipeCategories(new ClassicMachineRecipeJeiCategory(COMPRESSING, 2, HayoGuiSprites.COMPRESSING_ARROW, HayoGuiSprites.COMPRESSING_ARROW_OVERLAY, Component.translatable("hayo.recipe_type.compressing"), drawable.apply(Hayo.Items.COMPRESSOR)));
        registration.addRecipeCategories(new ClassicMachineRecipeJeiCategory(EXTRACTING, 2, HayoGuiSprites.EXTRACTING_ARROW, HayoGuiSprites.EXTRACTING_ARROW_OVERLAY, Component.translatable("hayo.recipe_type.extracting"), drawable.apply(Hayo.Items.EXTRACTOR)));
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
        for (var jeiRecipeType : List.of(
                ELECTRIC_SMELTING,
                ELECTRIC_BLASTING,
                ELECTRIC_SMOKING
        )) {
            registration.addRecipeTransferHandler(new IRecipeTransferInfo<ElectricFurnaceMenu, RecipeHolder<?>>() {
                @Override
                public Class<? extends ElectricFurnaceMenu> getContainerClass() {
                    return ElectricFurnaceMenu.class;
                }

                @Override
                public Optional<MenuType<ElectricFurnaceMenu>> getMenuType() {
                    return Optional.of(Hayo.MenuTypes.ELECTRIC_FURNACE);
                }

                @Override
                public IRecipeType<RecipeHolder<?>> getRecipeType() {
                    return (IRecipeType<RecipeHolder<?>>) (IRecipeType) jeiRecipeType;
                }

                @Override
                public boolean canHandle(ElectricFurnaceMenu container, RecipeHolder<?> holder) {
                    return container.getRecipeType() == holder.value().getType();
                }

                @Override
                public List<Slot> getRecipeSlots(ElectricFurnaceMenu container, RecipeHolder<?> recipe) {
                    return container.slots.subList(0, 1);
                }

                @Override
                public List<Slot> getInventorySlots(ElectricFurnaceMenu container, RecipeHolder<?> recipe) {
                    return container.slots.subList(container.playerInventory.start(), container.playerInventory.end());
                }
            });
        }

        registration.addRecipeTransferHandler(MaceratorMenu.class, Hayo.MenuTypes.MACERATOR, MACERATING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
        registration.addRecipeTransferHandler(CompressorMenu.class, Hayo.MenuTypes.COMPRESSOR, COMPRESSING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
        registration.addRecipeTransferHandler(ExtractorMenu.class, Hayo.MenuTypes.EXTRACTOR, EXTRACTING, 0, 1, ClassicMachineBlockEntity.SLOTS, 36);
    }
}
