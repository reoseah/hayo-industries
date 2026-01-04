package io.github.reoseah.hayo.feature.processing_machines.electric_furnace;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.BasicMachineBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends BasicMachineBlockEntity<AbstractCookingRecipe> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 3;
    public static final int CAPACITY = energyCostFromCookingTime(AbstractFurnaceBlockEntity.BURN_TIME_STANDARD); /* 200 * 3/4 * 3 = 450 e */

    @Getter
    protected ElectricFurnaceMode mode = ElectricFurnaceMode.NORMAL;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, pos, state);
    }

    public static int energyCostFromCookingTime(int cookingTime) {
        return cookingTime * ENERGY_USE_RATE * 3 / 4;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        tickProcessing((ServerLevel) level, pos, state, entity);
        entity.onTickEnd();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RecipeType<AbstractCookingRecipe> getRecipeType() {
        return (RecipeType<AbstractCookingRecipe>) this.mode.recipeType;
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
    public int getDefaultEnergyCost(RecipeHolder<AbstractCookingRecipe> recipe) {
        return energyCostFromCookingTime(recipe.value().cookingTime());
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.electric_furnace");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(menuId, this, inventory);
    }

    @Override
    protected void updateUpgradeState() {
        super.updateUpgradeState();

        var mode = getRecipeMode(this.stacks);
        if (mode != this.mode) {
            this.mode = mode;
            this.resetRecipeProgress();
        }
    }

    protected static ElectricFurnaceMode getRecipeMode(NonNullList<ItemStack> items) {
        for (int i = FIRST_UPGRADE_SLOT; i <= LAST_UPGRADE_SLOT; i++) {
            var stack = items.get(i);
            if (stack.is(Hayo.Items.BLASTING_UPGRADE)) {
                return ElectricFurnaceMode.BLASTING;
            } else if (stack.is(Hayo.Items.SMOKING_UPGRADE)) {
                return ElectricFurnaceMode.SMOKING;
            }
        }
        return ElectricFurnaceMode.NORMAL;
    }
}

