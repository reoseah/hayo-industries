package io.github.reoseah.hayo.feature.processing_machines.extractor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineBlockEntity;
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

public class ExtractorBlockEntity extends ClassicMachineBlockEntity<ExtractingRecipe> {
    public static final int TRANSFER_LIMIT = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 15 * 20 * ENERGY_USE_RATE; // 15s * 20tick/s * 2e/tick = 600e

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.EXTRACTOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ExtractorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        tickProcessing((ServerLevel) level, pos, state, entity);
        entity.onTickEnd();
    }

    @Override
    protected RecipeType<ExtractingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.EXTRACTING;
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
    protected int getEnergyTransferLimit() {
        return TRANSFER_LIMIT;
    }

    @Override
    public int getDefaultEnergyCost(RecipeHolder<ExtractingRecipe> recipe) {
        return recipe.value().getEnergyCost();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.extractor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ExtractorMenu(menuId, this, inventory);
    }
}
