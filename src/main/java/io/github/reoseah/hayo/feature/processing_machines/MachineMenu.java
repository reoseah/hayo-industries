package io.github.reoseah.hayo.feature.processing_machines;

import io.github.reoseah.hayo.feature.energy.ElectricItems;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class MachineMenu extends AbstractContainerMenu {
    protected final ContainerData data;
    protected final TagKey<Item> validUpgrades;

    protected MachineMenu(MenuType<?> type, TagKey<Item> validUpgrades, int menuId, Container container, ContainerData data, Inventory inventory) {
        super(type, menuId);

        this.validUpgrades = validUpgrades;

        this.data = data;
        this.addDataSlots(this.data);
    }

    public static void addClassicSlots(MachineMenu menu, Container container, Inventory inventory) {
        menu.addSlot(new Slot(container, 0, 47, 18));
        menu.addSlot(new Slot(container, 1, 47, 54));
        menu.addSlot(new ResultSlot(container, 2, 107, 36));

        menu.addSlot(new UpgradeSlot(container, 3, 152, 8, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 4, 152, 26, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 5, 152, 44, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 6, 152, 62, menu.validUpgrades));

        menu.addStandardInventorySlots(inventory, 8, 84);
    }

    public static void addMatterGeneratorSlots(MachineMenu menu, Container container, Inventory inventory) {
        menu.addSlot(new Slot(container, 0, 56, 34));
        menu.addSlot(new ResultSlot(container, 1, 116, 26));

        menu.addStandardInventorySlots(inventory, 8, 110);
    }

    public static void addSlotsWithSecondaryOutput(MachineMenu menu, Container container, Inventory inventory) {
        menu.addSlot(new Slot(container, 0, 47, 18));
        menu.addSlot(new Slot(container, 1, 47, 54));
        menu.addSlot(new ResultSlot(container, 2, 107, 26));
        menu.addSlot(new ResultSlot(container, 3, 107, 52));

        menu.addSlot(new UpgradeSlot(container, 4, 152, 8, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 5, 152, 26, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 6, 152, 44, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 7, 152, 62, menu.validUpgrades));

        menu.addStandardInventorySlots(inventory, 8, 84);
    }

    public float getRecipeDuration() {
        return Mth.ceil(this.getRecipeTotalEnergy() / (float) this.getEnergyUseRate()) / 20F;
    }

    protected static @NotNull ItemStack quickMoveClassicMachineStack(MachineMenu menu, Player player, int index, int firstPlayerSlot) {
        var slot = menu.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var remaining = stack.copy();
        if (index < firstPlayerSlot) {
            if (!menu.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, remaining);
        } else {
            if (menu.validUpgrades != null && stack.is(menu.validUpgrades)) {
                if (!menu.moveItemStackTo(stack, 3, 7, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (ElectricItems.isElectric(stack)) {
                if (!menu.moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (menu.isRecipeInput(stack)) {
                if (!menu.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < firstPlayerSlot + 27) {
                if (!menu.moveItemStackTo(stack, firstPlayerSlot + 27, firstPlayerSlot + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!menu.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == remaining.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return remaining;
    }

    protected abstract boolean isRecipeInput(ItemStack stack);

    public abstract int getEnergyUseRate();

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getStoredEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public int getRecipeUsedEnergy() {
        return this.data.get(2);
    }

    public int getRecipeTotalEnergy() {
        return this.data.get(3);
    }

    public int getEnergyCapacity() {
        return (this.data.get(5) << 16) | (this.data.get(4) & 0xFFFF);
    }

    public int getOverclockCount() {
        return this.data.get(6);
    }

    public boolean hasOverclockUpgrades() {
        return this.getOverclockCount() > 0;
    }

    public int getUseRatePercentage() {
        return 100 + 100 * this.getOverclockCount();
    }

    public int getRecipeEnergyPercentage() {
        return 100 + 25 * this.getOverclockCount();
    }

    public int getRecipeDurationPercentage() {
        return 100 * this.getRecipeEnergyPercentage() / this.getUseRatePercentage();
    }

    public static class ResultSlot extends Slot {
        public ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    public static class UpgradeSlot extends Slot {
        protected final TagKey<Item> validItems;

        public UpgradeSlot(Container container, int slot, int x, int y, TagKey<Item> validItems) {
            super(container, slot, x, y);
            this.validItems = validItems;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(this.validItems);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
