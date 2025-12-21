package io.github.reoseah.hayo.feature.processing_machines;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
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

public class CompressorBlockEntity extends ProcessingMachineBlockEntity<SimpleElectricRecipe, SingleRecipeInput> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 12 * 20 * ENERGY_USE_RATE; // 12s * 20tick/s * 2e/tick = 480e

    public static final int SLOTS = SlotHelper.Classic.SLOTS;
    public static final int INPUT_SLOT = SlotHelper.Classic.INPUT_SLOT;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = SlotHelper.Classic.OUTPUT_SLOT;
    public static final int FIRST_UPGRADE_SLOT = SlotHelper.Classic.FIRST_UPGRADE_SLOT;
    public static final int LAST_UPGRADE_SLOT = SlotHelper.Classic.LAST_UPGRADE_SLOT;

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.COMPRESSOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, CompressorBlockEntity entity) {
        ElectricBlockEntity.tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity);
    }

    @Override
    protected RecipeType<SimpleElectricRecipe> getRecipeType() {
        return Hayo.RecipeTypes.COMPRESSING;
    }

    @Override
    protected SlotHelper<SimpleElectricRecipe, SingleRecipeInput> getSlotHelper() {
        return SlotHelper.classic();
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
    public int getDefaultRecipeEnergy(RecipeHolder<SimpleElectricRecipe> recipe) {
        return recipe.value().processingEnergy();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.compressor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ProcessingMachineMenu.CompressorMenu(menuId, this, inventory);
    }
}
