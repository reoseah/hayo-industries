package io.github.reoseah.hayo.feature.processing_machines.macerator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.ExtraChanceMachineRecipe;
import io.github.reoseah.hayo.feature.processing_machines.SimpleMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaceratorBlockEntity extends SimpleMachineBlockEntity<ExtraChanceMachineRecipe> {
    public static final int TRANSFER_RATE = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 10 * 20 * ENERGY_USE_RATE; // 10s * 20tick/s * 2e/tick = 400e

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MACERATOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MaceratorBlockEntity entity) {
        tickChargeFromSlot(entity, BATTERY_SLOT, CAPACITY, TRANSFER_RATE);
        tickProcessing((ServerLevel) level, pos, state, entity);
    }

    @Override
    public boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<ExtraChanceMachineRecipe> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
        if (recipe == null || input.isEmpty()) {
            return false;
        }
        if (recipe.value().extraChance == 0) {
            return super.canCraft(registryAccess, recipe, input, items);
        }

        var recipeOutput = recipe.value().result().copyWithCount(recipe.value().result().getCount() + 1);

        return canInsertToSlot(items, recipeOutput, OUTPUT_SLOT);
    }

    @Override
    public void craft(RegistryAccess registryAccess, RecipeHolder<ExtraChanceMachineRecipe> recipe, SingleRecipeInput input, NonNullList<ItemStack> items) {
        if (recipe.value().extraChance == 0) {
            super.craft(registryAccess, recipe, input, items);
            return;
        }

        var recipeOutput = recipe.value().assemble(input, registryAccess);
        if (this.level.getRandom().nextFloat() < recipe.value().extraChance) {
            recipeOutput.setCount(recipeOutput.getCount() + 1);
        }

        var outputStack = items.get(OUTPUT_SLOT);

        if (outputStack.isEmpty()) {
            items.set(OUTPUT_SLOT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }

        var inputStack = items.get(INPUT_SLOT);
        inputStack.shrink(1);
    }

    @Override
    protected RecipeType<ExtraChanceMachineRecipe> getRecipeType() {
        return Hayo.RecipeTypes.MACERATING;
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
    public int getDefaultRecipeEnergy(RecipeHolder<ExtraChanceMachineRecipe> recipe) {
        return recipe.value().processingEnergy();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.macerator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new MaceratorMenu(menuId, this, inventory);
    }
}
