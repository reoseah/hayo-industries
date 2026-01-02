package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HayoReiClient implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new MachineRecipeCategory(Hayo.modId("electric_smelting"), EntryStacks.of(Hayo.Items.ELECTRIC_FURNACE), HayoGuiSprites.RecipeArrow.DEFAULT, 3));
        registry.add(new MachineRecipeCategory(Hayo.modId("electric_blasting"), EntryStacks.of(Hayo.Items.ELECTRIC_FURNACE), HayoGuiSprites.RecipeArrow.DEFAULT, 3));
        registry.add(new MachineRecipeCategory(Hayo.modId("electric_smoking"), EntryStacks.of(Hayo.Items.ELECTRIC_FURNACE), HayoGuiSprites.RecipeArrow.DEFAULT, 3));
        registry.add(new MachineRecipeWithExtraCategory(Hayo.modId("macerating"), EntryStacks.of(Hayo.Items.MACERATOR), HayoGuiSprites.RecipeArrow.MACERATOR, 2));
        registry.add(new MachineRecipeWithExtraCategory(Hayo.modId("compressing"), EntryStacks.of(Hayo.Items.COMPRESSOR), HayoGuiSprites.RecipeArrow.COMPRESSOR, 2));
        registry.add(new MachineRecipeWithExtraCategory(Hayo.modId("extracting"), EntryStacks.of(Hayo.Items.EXTRACTOR), HayoGuiSprites.RecipeArrow.EXTRACTOR, 2));

        registry.addWorkstations(CategoryIdentifier.of(Hayo.modId("electric_smelting")), EntryStacks.of(Hayo.Items.ELECTRIC_FURNACE));
        registry.addWorkstations(CategoryIdentifier.of(Hayo.modId("electric_blasting")), machineWithUpgrade(Hayo.Items.ELECTRIC_FURNACE, Hayo.Items.BLASTING_UPGRADE), EntryStacks.of(Hayo.Items.BLASTING_UPGRADE));
        registry.addWorkstations(CategoryIdentifier.of(Hayo.modId("electric_smoking")), machineWithUpgrade(Hayo.Items.ELECTRIC_FURNACE, Hayo.Items.SMOKING_UPGRADE), EntryStacks.of(Hayo.Items.SMOKING_UPGRADE));
        registry.addWorkstations(CategoryIdentifier.of(Hayo.modId("macerating")), EntryStacks.of(Hayo.Items.MACERATOR));
        registry.addWorkstations(CategoryIdentifier.of(Hayo.modId("compressing")), EntryStacks.of(Hayo.Items.COMPRESSOR));
        registry.addWorkstations(CategoryIdentifier.of(Hayo.modId("extracting")), EntryStacks.of(Hayo.Items.EXTRACTOR));
    }

    public static EntryStack<ItemStack> machineWithUpgrade(Item machine, Item upgrade) {
        return EntryStacks.of(machine).tooltip( //
                Component.translatable(upgrade.builtInRegistryHolder().key().identifier().toLanguageKey("item")).withStyle(ChatFormatting.DARK_AQUA) //
        );
    }
}
