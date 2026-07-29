package io.github.reoseah.hayo.feature.machines;

import io.github.reoseah.hayo.feature.energy.item.EnergyComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class MachineMenu extends AbstractContainerMenu {
    protected final Container container;
    protected final ContainerData data;

    protected MachineMenu(MenuType<?> type, int menuId, Container container, ContainerData data, Inventory inventory) {
        super(type, menuId);

        this.container = container;

        this.data = data;
        this.addDataSlots(this.data);
    }

    public static void addClassicSlots(MachineMenu menu, Container container, Inventory inventory, TagKey<Item> validUpgrades) {
        menu.addSlot(new Slot(container, 0, 47, 18));
        menu.addSlot(new Slot(container, 1, 47, 54));
        menu.addSlot(new ResultSlot(container, 2, 107, 36));

        menu.addSlot(new TagFilteredSlot(container, 3, 152, 8, validUpgrades));
        menu.addSlot(new TagFilteredSlot(container, 4, 152, 26, validUpgrades));
        menu.addSlot(new TagFilteredSlot(container, 5, 152, 44, validUpgrades));
        menu.addSlot(new TagFilteredSlot(container, 6, 152, 62, validUpgrades));

        menu.addStandardInventorySlots(inventory, 8, 84);
    }

    public static ContainerData createData(MachineBlockEntity<?, ?> entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 9;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> entity.getStoredEnergy() >>> 16;
                    case 2 -> entity.getEnergyCapacity() & 0xFFFF;
                    case 3 -> entity.getEnergyCapacity() >>> 16;
                    case 4 -> entity.getRecipeUsedEnergy() & 0xFFFF;
                    case 5 -> entity.getRecipeUsedEnergy() >>> 16;
                    case 6 -> entity.getRecipeTotalEnergy() & 0xFFFF;
                    case 7 -> entity.getRecipeTotalEnergy() >>> 16;
                    case 8 -> entity.getEnergyUseRate();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        };
    }

    public static ContainerData createData() {
        return new SimpleContainerData(9);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public float getRecipeDuration() {
        return Mth.ceil(this.getRecipeTotalEnergy() / (float) this.getEnergyUseRate()) / 20F;
    }

    protected static @NotNull ItemStack quickMoveClassicMachineStack(MachineMenu menu, //
            Player player, //
            int index, //
            int inputSlots,
            int batterySlots,
            int outputSlots,
            int upgradeSlots,
            @Nullable Predicate<ItemStack> inputs,
            @Nullable TagKey<Item> validUpgrades) {
        var slot = menu.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var remaining = stack.copy();

        int playerSlotsStart = inputSlots + batterySlots + outputSlots + upgradeSlots;
        if (index < playerSlotsStart) {
            if (!menu.moveItemStackTo(stack, playerSlotsStart, playerSlotsStart + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, remaining);
        } else {
            if (upgradeSlots != 0 && validUpgrades != null && stack.is(validUpgrades)) {
                int start = inputSlots + batterySlots + outputSlots;
                if (!menu.moveItemStackTo(stack, start, start + upgradeSlots, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (batterySlots != 0 && EnergyComponents.canDischargeInMachine(stack)) {
                if (!menu.moveItemStackTo(stack, inputSlots, inputSlots + batterySlots, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (inputSlots != 0 && (inputs == null || inputs.test(stack))) {
                if (!menu.moveItemStackTo(stack, 0, inputSlots, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < playerSlotsStart + 27) {
                if (!menu.moveItemStackTo(stack, playerSlotsStart + 27, playerSlotsStart + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!menu.moveItemStackTo(stack, playerSlotsStart, playerSlotsStart + 27, false)) {
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

    public int getEnergyUseRate() {
        return this.data.get(8);
    }

    public int getStoredEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public int getEnergyCapacity() {
        return (this.data.get(3) << 16) | (this.data.get(2) & 0xFFFF);
    }

    public int getRecipeUsedEnergy() {
        return (this.data.get(5) << 16) | (this.data.get(4) & 0xFFFF);
    }

    public int getRecipeTotalEnergy() {
        return (this.data.get(7) << 16) | (this.data.get(6) & 0xFFFF);
    }
}
