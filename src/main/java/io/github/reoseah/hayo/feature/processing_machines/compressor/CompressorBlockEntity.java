package io.github.reoseah.hayo.feature.processing_machines.compressor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.SimpleMachineRecipe;
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

public class CompressorBlockEntity extends ClassicMachineBlockEntity<SimpleMachineRecipe> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 12 * 20 * ENERGY_USE_RATE; // 12s * 20tick/s * 2e/tick = 480e

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.COMPRESSOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, CompressorBlockEntity entity) {
        ElectricBlockEntity.tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity);
    }

    @Override
    protected RecipeType<SimpleMachineRecipe> getRecipeType() {
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
    public int getDefaultRecipeEnergy(RecipeHolder<SimpleMachineRecipe> recipe) {
        return recipe.value().processingEnergy();
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
