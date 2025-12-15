package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.block.OrientableMachineBlock;
import io.github.reoseah.hayoind.item.ElectricItems;
import io.github.reoseah.hayoind.menu.ClassicProcessingMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends ProcessingMachineBlockEntity<SmeltingRecipe, SingleRecipeInput> {
    public static final int CAPACITY = 400;
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 3;
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_FURNACE, RecipeType.SMELTING, pos, state);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(7, ItemStack.EMPTY);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayoind.electric_furnace");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new ClassicProcessingMachineMenu.ElectricFurnaceMenu(menuId, this, inventory);
    }


    @SuppressWarnings("unused")
    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity entity) {
        int chargeable = Math.min(CAPACITY - entity.storedEnergy, TRANSFER_RATE);
        if (chargeable != 0) {
            int charge = ElectricItems.tryDischarge(chargeable, entity, BATTERY_SLOT);
            if (charge > 0) {
                entity.storedEnergy += charge;
                entity.setChanged();
            }
        }

        boolean wasProcessing = entity.recipeUsedEnergy > 0;

        var inputStack = entity.getItem(0);
        boolean hasInput = !inputStack.isEmpty();
        if (hasInput) {
            var recipeInput = new SingleRecipeInput(inputStack);
            var recipeHolder = entity.quickCheck.getRecipeFor(recipeInput, (ServerLevel) level).orElse(null);

            boolean hasEnergy = entity.storedEnergy >= ENERGY_USE_RATE;
            if (hasEnergy && canCraft(level.registryAccess(), recipeHolder, recipeInput, entity.stacks)) {
                int usable = Math.min(Math.min(ENERGY_USE_RATE, entity.recipeTotalEnergy - entity.recipeUsedEnergy), entity.storedEnergy);

                entity.storedEnergy -= usable;
                entity.recipeUsedEnergy += usable;
                if (entity.recipeUsedEnergy >= entity.recipeTotalEnergy) {
                    craft(level.registryAccess(), recipeHolder, recipeInput, entity.stacks);
                    entity.recipeUsedEnergy = 0;
                }
                entity.setChanged();
            } else {
                entity.recipeUsedEnergy = Mth.clamp(entity.recipeUsedEnergy - 2 * ENERGY_USE_RATE, 0, entity.recipeTotalEnergy);
                entity.setChanged();
            }
        } else if (entity.recipeUsedEnergy > 0) {
            entity.recipeUsedEnergy = entity.recipeTotalEnergy = 0;
            entity.setChanged();
        }

        boolean isProcessing = entity.recipeUsedEnergy > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(OrientableMachineBlock.LIT, isProcessing));
        }
    }

    protected static boolean canCraft(RegistryAccess registryAccess, //
                                      @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipe, //
                                      SingleRecipeInput recipeInput, //
                                      NonNullList<ItemStack> items) {
        if (recipe == null || recipeInput.isEmpty()) {
            return false;
        }

        var recipeOutput = recipe.value().assemble(recipeInput, registryAccess);
        if (recipeOutput.isEmpty()) {
            return false;
        }

        var outputStack = items.get(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(outputStack, recipeOutput)) {
            return false;
        }

        return outputStack.getCount() + recipeOutput.getCount() < recipeOutput.getMaxStackSize();
    }

    protected static void craft(RegistryAccess registryAccess, //
                                RecipeHolder<? extends AbstractCookingRecipe> recipe, //
                                SingleRecipeInput recipeInput, //
                                NonNullList<ItemStack> items) {
        var inputStack = items.get(INPUT_SLOT);
        var outputStack = items.get(OUTPUT_SLOT);
        var recipeOutput = recipe.value().assemble(recipeInput, registryAccess);

        if (outputStack.isEmpty()) {
            items.set(OUTPUT_SLOT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }

        inputStack.shrink(1);
    }
}

