package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.menu.ProcessingMachineMenu;
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
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FIRST_UPGRADE_SLOT = 3;
    public static final int UPGRADE_SLOTS = 4;

    protected ElectricFurnaceMode mode = ElectricFurnaceMode.NORMAL;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        tickChargeFromSlot(entity, BATTERY_SLOT, entity.getDefaultCapacity(), TRANSFER_RATE);
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
        return Component.translatable("block.hayoind.electric_furnace");
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

    protected static ElectricFurnaceMode getRecipeMode(NonNullList<ItemStack> stacks) {
        for (int i = FIRST_UPGRADE_SLOT; i < FIRST_UPGRADE_SLOT + UPGRADE_SLOTS; i++) {
            var stack = stacks.get(i);
            if (stack.is(Hayo.Items.BLASTING_UPGRADE)) {
                return ElectricFurnaceMode.BLASTING;
            } else if (stack.is(Hayo.Items.SMOKING_UPGRADE)) {
                return ElectricFurnaceMode.SMOKING;
            }
        }
        return ElectricFurnaceMode.NORMAL;
    }

    public enum ElectricFurnaceMode {
        NORMAL(RecipeType.SMELTING), BLASTING(RecipeType.BLASTING), SMOKING(RecipeType.SMOKING);

        public final RecipeType<? extends AbstractCookingRecipe> recipeType;

        ElectricFurnaceMode(RecipeType<? extends AbstractCookingRecipe> recipeType) {
            this.recipeType = recipeType;
        }
    }
}

