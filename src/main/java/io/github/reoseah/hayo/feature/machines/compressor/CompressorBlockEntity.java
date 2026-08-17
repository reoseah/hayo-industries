package io.github.reoseah.hayo.feature.machines.compressor;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.machines.ClassicMachineBlockEntity;
import io.github.reoseah.hayo.feature.universal_screen.RecipeProgressBar;
import io.github.reoseah.hayo.menu.UniversalContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CompressorBlockEntity extends ClassicMachineBlockEntity<CompressingRecipe> {
    public static final int TRANSFER_LIMIT = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 12 * 20 * ENERGY_USE_RATE; // 12s * 20tick/s * 2e/tick = 480e

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.COMPRESSOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, CompressorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        entity.tickRecipe((ServerLevel) level, pos, state);
        entity.resetEnergyPerTick();
    }

    @Override
    protected RecipeType<CompressingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.COMPRESSING;
    }

    @Override
    public int getBaseEnergyUseRate() {
        return ENERGY_USE_RATE;
    }

    @Override
    public int getBaseEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return TRANSFER_LIMIT;
    }

    @Override
    public int getBaseEnergyCost(@Nullable RecipeHolder<CompressingRecipe> holder) {
        return holder == null ? CompressingRecipe.DEFAULT_ENERGY : holder.value().energyCost;
    }

    @Override
    protected TagKey<Item> getUpgradeTag() {
        return Hayo.ItemTags.COMPRESSOR_UPGRADES;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.compressor");
    }

    @Override
    public UniversalContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return super.createMenu(containerId, inventory, player) //
                .addElement(RecipeProgressBar.compressor(70, 36, this::getProgressEnergy, this::getLastOrDefaultRecipeEnergy));
    }
}
