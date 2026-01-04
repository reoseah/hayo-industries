package io.github.reoseah.hayo.feature.processing_machines.compressor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.BasicMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CompressorBlockEntity extends BasicMachineBlockEntity<CompressingRecipe> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 12 * 20 * ENERGY_USE_RATE; // 12s * 20tick/s * 2e/tick = 480e

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.COMPRESSOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, CompressorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        tickProcessing((ServerLevel) level, pos, state, entity);
        entity.onTickEnd();
    }

    @Override
    protected RecipeType<CompressingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.COMPRESSING;
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
    protected int getEnergyTransferRate() {
        return TRANSFER_RATE;
    }

    @Override
    public int getDefaultEnergyCost(RecipeHolder<CompressingRecipe> recipe) {
        return recipe.value().getEnergyCost();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.compressor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new CompressorMenu(menuId, this, inventory);
    }
}
