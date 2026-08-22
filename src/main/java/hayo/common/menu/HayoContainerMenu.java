package hayo.common.menu;

import hayo.common.IntRange;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class HayoContainerMenu extends AbstractContainerMenu {
    protected final Container container;

    protected Player player;
    public IntRange playerInventory;
    protected IntRange playerMainInventory;
    protected IntRange playerHotbar;

    protected HayoContainerMenu(@Nullable MenuType<?> menuType, int containerId, Container container) {
        super(menuType, containerId);
        this.container = container;
    }

    protected void addStandardInventorySlots(Container inventory, int x, int y) {
        int start = this.slots.size();

        super.addStandardInventorySlots(inventory, x, y);

        this.player = ((Inventory) inventory).player;
        this.playerInventory = new IntRange(start, start + 36);
        this.playerMainInventory = new IntRange(start, start + 27);
        this.playerHotbar = new IntRange(start + 27, start + 36);
    }

    @Override
    public final boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public final ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var original = stack.copy();

        if (!this.playerInventory.contains(index)) {
            if (!this.moveItemStackTo(stack, this.playerInventory.start(), this.playerInventory.end(), true)) {
                return ItemStack.EMPTY;
            }

            slot.onQuickCraft(stack, original);
        } else {
            boolean moved = this.handleQuickMoveFromInventory(stack, player, index);

            if (!moved) {
                if (this.playerMainInventory.contains(index)) {
                    moved = this.moveItemStackTo(stack, this.playerHotbar.start(), this.playerHotbar.end(), false);
                } else if (this.playerHotbar.contains(index)) {
                    moved = this.moveItemStackTo(stack, this.playerMainInventory.start(), this.playerMainInventory.end(), false);
                }
            }

            if (!moved) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    protected boolean handleQuickMoveFromInventory(ItemStack stack, Player player, int index) {
        return this.moveItemStackTo(stack, 0, this.container.getContainerSize(), false);
    }
}
