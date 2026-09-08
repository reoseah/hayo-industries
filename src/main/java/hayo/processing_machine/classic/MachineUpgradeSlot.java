package hayo.processing_machine.classic;

import hayo.Hayo;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MachineUpgradeSlot extends Slot {
    protected final TagKey<Item> upgrades;
    protected final int firstUpgrade;
    protected final int upgradeCount;

    public MachineUpgradeSlot(Container container, int slot, int x, int y, TagKey<Item> upgrades, int firstUpgrade, int upgradeCount) {
        super(container, slot, x, y);
        this.upgrades = upgrades;
        this.firstUpgrade = firstUpgrade;
        this.upgradeCount = upgradeCount;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!stack.is(this.upgrades)) {
            return false;
        }

        if (stack.is(Hayo.ItemTags.NON_REPEATABLE_UPGRADES)) {
            for (int i = this.firstUpgrade; i < this.firstUpgrade + this.upgradeCount; i++) {
                if (i == this.index) continue;

                var upgrade = this.container.getItem(i);
                if (upgrade.is(Hayo.ItemTags.NON_REPEATABLE_UPGRADES) && ItemStack.isSameItem(stack, upgrade)) {
                    return false;
                }
            }
        }
        if (stack.is(Hayo.ItemTags.MUTUALLY_EXCLUSIVE_UPGRADES)) {
            for (int i = this.firstUpgrade; i < this.firstUpgrade + this.upgradeCount; i++) {
                if (i == this.index) continue;

                var upgrade = this.container.getItem(i);
                if (upgrade.is(Hayo.ItemTags.MUTUALLY_EXCLUSIVE_UPGRADES)) {
                    return false;
                }
            }
        }

        return true;
    }
}
