package hayo.common.menuslot;

import hayo.Hayo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BatteryBoxSlot extends Slot {
    public static final Identifier BATTERY_SLOT_ICON = Hayo.modId("slot_icons/battery");

    public BatteryBoxSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Hayo.ItemTags.BATTERY_BOX_BATTERIES);
    }

    @Override
    public Identifier getNoItemIcon() {
        return BATTERY_SLOT_ICON;
    }
}
