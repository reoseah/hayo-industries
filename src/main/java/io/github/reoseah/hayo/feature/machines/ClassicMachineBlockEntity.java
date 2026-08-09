package io.github.reoseah.hayo.feature.machines;

import io.github.reoseah.hayo.feature.electric_items.EnergyComponents;
import io.github.reoseah.hayo.feature.universal_screen.MachineEnergyBar;
import io.github.reoseah.hayo.feature.universal_screen.SpriteElement;
import io.github.reoseah.hayo.feature.universal_screen.UniversalContainerMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class ClassicMachineBlockEntity<R extends Recipe<SingleRecipeInput>> extends MachineBlockEntity<R, SingleRecipeInput> implements WorldlyContainer, ExtendedMenuProvider<BlockPos> {
    public static final int SLOTS = 7;
    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FIRST_UPGRADE_SLOT = 3;
    public static final int LAST_UPGRADE_SLOT = 6;

    protected ClassicMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    @Override
    public boolean isInputSlot(int slot) {
        return slot == INPUT_SLOT;
    }

    @Override
    public int getFirstUpgradeSlot() {
        return FIRST_UPGRADE_SLOT;
    }

    @Override
    public int getLastUpgradeSlot() {
        return LAST_UPGRADE_SLOT;
    }

    @Override
    public SingleRecipeInput createRecipeInput() {
        return new SingleRecipeInput(this.stacks.get(INPUT_SLOT));
    }

    protected abstract TagKey<Item> getUpgradeTag();

    @Override
    public boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, SingleRecipeInput input) {
        if (recipe == null || input.isEmpty()) {
            return false;
        }

        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.inputCount > input.item().getCount()) {
            return false;
        }

        var recipeOutput = recipe.value().assemble(input);
        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            recipeOutput.setCount(recipeOutput.getCount() + 1);
        }
        return canInsertToSlot(this.stacks, recipeOutput, OUTPUT_SLOT);
    }

    @Override
    public void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, SingleRecipeInput input) {
        var inputStack = this.stacks.get(INPUT_SLOT);
        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe) {
            inputStack.shrink(machineRecipe.inputCount);
        } else {
            inputStack.shrink(1);
        }
        var recipeOutput = recipe.value().assemble(input);
        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            if (this.level.getRandom().nextFloat() < machineRecipe.extraResultChance) {
                recipeOutput.setCount(recipeOutput.getCount() + 1);
            }
        }

        var outputStack = this.stacks.get(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            this.stacks.set(OUTPUT_SLOT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> new int[]{INPUT_SLOT};
            case DOWN -> new int[]{OUTPUT_SLOT};
            default -> new int[]{BATTERY_SLOT};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction side) {
        return switch (side) {
            case null -> index != OUTPUT_SLOT;
            case UP -> true;
            case DOWN -> false;
            default -> EnergyComponents.isStorage(stack);
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
        return side != Direction.UP;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new UniversalContainerMenu(containerId, this) //
                .addSlotChainable(new Slot(this, INPUT_SLOT, 47, 18)) //
                .addSlotChainable(new Slot(this, BATTERY_SLOT, 47, 54)) //
                .addSlotChainable(new ResultSlot(this, OUTPUT_SLOT, 107, 36)) //
                .addSlotChainable(new TagFilteredSlot(this, 3, 152, 8, this.getUpgradeTag())) //
                .addSlotChainable(new TagFilteredSlot(this, 4, 152, 26, this.getUpgradeTag())) //
                .addSlotChainable(new TagFilteredSlot(this, 5, 152, 44, this.getUpgradeTag())) //
                .addSlotChainable(new TagFilteredSlot(this, 6, 152, 62, this.getUpgradeTag())) //
                .addStandardInventorySlotsChainable(inventory) //
                .addElement(new MachineEnergyBar(48, 37, this::getStoredEnergy, this::getEnergyCapacity)) //
                .addElement(SpriteElement.upgradeSlot(151, 7)) //
                .addElement(SpriteElement.upgradeSlot(151, 25)) //
                .addElement(SpriteElement.upgradeSlot(151, 43)) //
                .addElement(SpriteElement.upgradeSlot(151, 61)) //
                ;
    }
}
