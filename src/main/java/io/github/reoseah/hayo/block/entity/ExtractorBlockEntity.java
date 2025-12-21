package io.github.reoseah.hayo.block.entity;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.menu.ProcessingMachineMenu;
import io.github.reoseah.hayo.recipe.SecondaryOutputElectricRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ExtractorBlockEntity extends ProcessingMachineBlockEntity<SecondaryOutputElectricRecipe, SingleRecipeInput> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 15 * 20 * ENERGY_USE_RATE; // 15s * 20tick/s * 2e/tick = 600e

    public static final int SLOTS = SlotHelper.ClassicWithExtraOutput.SLOTS;
    public static final int INPUT_SLOT = SlotHelper.ClassicWithExtraOutput.INPUT_SLOT;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = SlotHelper.ClassicWithExtraOutput.OUTPUT_SLOT;
    public static final int FIRST_UPGRADE_SLOT = SlotHelper.ClassicWithExtraOutput.FIRST_UPGRADE_SLOT;
    public static final int LAST_UPGRADE_SLOT = SlotHelper.ClassicWithExtraOutput.LAST_UPGRADE_SLOT;

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.EXTRACTOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ExtractorBlockEntity entity) {
        tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity);
    }

    @Override
    protected RecipeType<SecondaryOutputElectricRecipe> getRecipeType() {
        return Hayo.RecipeTypes.EXTRACTING;
    }

    @Override
    protected SlotHelper<SecondaryOutputElectricRecipe, SingleRecipeInput> getSlotHelper() {
        return SlotHelper.classicWithExtraOutput();
    }

    @Override
    public int getDefaultEnergyUseRate() {
        return ENERGY_USE_RATE;
    }

    @Override
    public int getDefaultCapacity() {
        return CAPACITY;
    }

    @Override
    public int getDefaultRecipeEnergy(RecipeHolder<SecondaryOutputElectricRecipe> recipe) {
        return recipe.value().processingEnergy();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.extractor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ProcessingMachineMenu.ExtractorMenu(menuId, this, inventory);
    }
}
