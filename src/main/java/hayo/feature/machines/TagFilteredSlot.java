package hayo.feature.machines;

import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagFilteredSlot extends Slot {
    protected final TagKey<Item> validItems;

    public TagFilteredSlot(Container container, int slot, int x, int y, TagKey<Item> validItems) {
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
