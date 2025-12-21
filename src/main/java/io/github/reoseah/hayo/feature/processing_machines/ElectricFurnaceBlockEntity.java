package io.github.reoseah.hayo.feature.processing_machines;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends ProcessingMachineBlockEntity<AbstractCookingRecipe, SingleRecipeInput> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 3;
    public static final int CAPACITY = energyCostFromCookingTime(AbstractFurnaceBlockEntity.BURN_TIME_STANDARD); /* 200 * 3/4 * 3 = 450 e */

    public static final int SLOTS = SlotHelper.Classic.SLOTS;
    public static final int INPUT_SLOT = SlotHelper.Classic.INPUT_SLOT;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = SlotHelper.Classic.OUTPUT_SLOT;
    public static final int FIRST_UPGRADE_SLOT = SlotHelper.Classic.FIRST_UPGRADE_SLOT;
    public static final int LAST_UPGRADE_SLOT = SlotHelper.Classic.LAST_UPGRADE_SLOT;

    @Getter
    protected ElectricFurnaceMode mode = ElectricFurnaceMode.NORMAL;

    public enum ElectricFurnaceMode {
        NORMAL(RecipeType.SMELTING), BLASTING(RecipeType.BLASTING), SMOKING(RecipeType.SMOKING);

        public final RecipeType<? extends AbstractCookingRecipe> recipeType;

        ElectricFurnaceMode(RecipeType<? extends AbstractCookingRecipe> recipeType) {
            this.recipeType = recipeType;
        }
    }

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        ElectricBlockEntity.tickChargeFromSlot(entity, BATTERY_SLOT, entity.getDefaultCapacity(), TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity);
    }

    public static int energyCostFromCookingTime(int cookingTime) {
        return cookingTime * ENERGY_USE_RATE * 3 / 4;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RecipeType<AbstractCookingRecipe> getRecipeType() {
        return (RecipeType<AbstractCookingRecipe>) this.mode.recipeType;
    }

    @Override
    protected SlotHelper<AbstractCookingRecipe, SingleRecipeInput> getSlotHelper() {
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
    public int getDefaultRecipeEnergy(RecipeHolder<AbstractCookingRecipe> recipe) {
        return energyCostFromCookingTime(recipe.value().cookingTime());
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.electric_furnace");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ProcessingMachineMenu.ElectricFurnaceMenu(menuId, this, inventory);
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

