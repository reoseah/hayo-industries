package io.github.reoseah.hayo.feature.electric_beacon;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.HayoContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ElectricBeaconMenu extends HayoContainerMenu {
    public static final int DATA_SLOT_TIER_1 = 0, DATA_SLOT_TIER_2 = 1, DATA_SLOT_TIER_3 = 2, DATA_SLOT_TIER_4 = 3;

    protected final ContainerData data;

    public ElectricBeaconMenu(int containerId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.ELECTRIC_BEACON, containerId, container);

        this.addDataSlots(this.data = data);

        this.addSlot(new Slot(container, 0, 169, 85));
        this.addStandardInventorySlots(inventory, 17, 117);
    }

    public ElectricBeaconMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(1), createBeaconData(), inventory);
    }

    public ElectricBeaconMenu(int containerId, ElectricBeaconBlockEntity entity, Inventory inventory) {
        this(containerId, entity, createBeaconData(entity), inventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    public static ContainerData createBeaconData() {
        return new SimpleContainerData(6);
    }

    public static ContainerData createBeaconData(ElectricBeaconBlockEntity entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 6;
            }

            @Override
            public int get(int dataId) {
                return switch (dataId) {
                    case 0 -> ElectricBeacon.encodeOption(entity.choices[0]);
                    case 1 -> ElectricBeacon.encodeOption(entity.choices[1]);
                    case 2 -> ElectricBeacon.encodeOption(entity.choices[2]);
                    case 3 -> ElectricBeacon.encodeOption(entity.choices[3]);

                    case 4 -> entity.getStoredEnergy() & 0xFFFF;
                    case 5 -> entity.getStoredEnergy() >>> 16;
                    default -> 0;
                };
            }

            @Override
            public void set(int dataId, int value) {
                switch (dataId) {
                    case 0, 1, 2, 3 -> {
                        entity.setChoice(dataId, ElectricBeacon.decodeOption(value));
                    }
                }
            }
        };
    }

    public @Nullable ElectricBeaconOption getOption(int tier) {
        if (tier < 0 || tier > 3) {
            throw new IllegalArgumentException();
        }
        var idx = this.data.get(tier);
        return ElectricBeacon.decodeOption(idx);
    }

    public boolean clickMenuButton(Player player, int buttonId) {
        var option = ElectricBeacon.decodeOption(buttonId);
        if (option != null) {
            for (int tier = 0; tier < 4; tier++) {
                var options = ElectricBeacon.OPTIONS_BY_TIER.get(tier);
                if (options.contains(option)) {
                    // TODO: set option to block entity directly
                    var idx = ElectricBeacon.OPTIONS.indexOf(option);
                    this.data.set(tier, idx);
                    return true;
                }
            }
        }

        return super.clickMenuButton(player, buttonId);
    }

    public int getStoredEnergy() {
        return (this.data.get(5) << 16) | (this.data.get(4) & 0xFFFF);
    }
}
