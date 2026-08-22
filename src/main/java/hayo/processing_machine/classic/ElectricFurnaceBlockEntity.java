package hayo.processing_machine.classic;

import hayo.Hayo;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricFurnaceBlockEntity extends ClassicMachineBlockEntity<AbstractCookingRecipe> {
    public static final int TRANSFER_LIMIT = 32;
    public static final int ENERGY_USE_RATE = 3;
    public static final int CAPACITY = 450; /* 200 ticks * 3/4 * 3 e/tick = 450 e */

    @Getter
    protected ElectricFurnaceMode mode = ElectricFurnaceMode.SMELTING;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, pos, state);
    }

    public static int energyCostFromCookingTime(AbstractCookingRecipe recipe) {
        int cookingTime = recipe.cookingTime();
        var type = recipe.getType();
        if (type == RecipeType.BLASTING || type == RecipeType.SMOKING) {
            return energyCostFromCookingTime(2 * cookingTime);
        }
        return energyCostFromCookingTime(cookingTime);
    }

    public static int energyCostFromCookingTime(int cookingTime) {
        return cookingTime * ENERGY_USE_RATE * 3 / 4;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.tickRecipe((ServerLevel) level, pos, state);
        entity.resetEnergyPerTick();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RecipeType<AbstractCookingRecipe> getRecipeType() {
        return (RecipeType<AbstractCookingRecipe>) this.mode.recipeType;
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
    public int getBaseEnergyCost(RecipeHolder<AbstractCookingRecipe> holder) {
        return energyCostFromCookingTime(holder == null ? AbstractFurnaceBlockEntity.BURN_TIME_STANDARD : holder.value().cookingTime());
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.electric_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(containerId, this, inventory);
    }

    @Override
    protected void updateUpgradeState() {
        super.updateUpgradeState();

        var mode = ElectricFurnaceMode.SMELTING;
        for (int i = FIRST_UPGRADE; i < FIRST_UPGRADE + UPGRADES; i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.BLASTING_UPGRADE)) {
                mode = ElectricFurnaceMode.BLASTING;
                this.extraCraftingSpeed += 1;
                break;
            } else if (stack.is(Hayo.Items.SMOKING_UPGRADE)) {
                mode = ElectricFurnaceMode.SMOKING;
                this.extraCraftingSpeed += 1;
                break;
            }
        }
        if (mode != this.mode) {
            this.mode = mode;
            this.recipeProgress = 0;
        }
    }
}

