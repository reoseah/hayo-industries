package io.github.reoseah.hayo.feature.electric_beacon;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.HayoContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ElectricBeaconMenu extends HayoContainerMenu {
    public static final int DATA_SLOT_TIER_1 = 0, DATA_SLOT_TIER_2 = 1, DATA_SLOT_TIER_3 = 2, DATA_SLOT_TIER_4 = 3;

    protected final ContainerData data;

    public ElectricBeaconMenu(int containerId, Container container, ContainerData data, Inventory inventory) {
        super(Hayo.MenuTypes.ELECTRIC_BEACON, containerId, container);

        this.addDataSlots(this.data = data);
        this.addStandardInventorySlots(inventory, 17, 116);
    }

    public ElectricBeaconMenu(int containerId, Inventory inventory) {
        this(containerId, new SimpleContainer(0), createBeaconData(), inventory);
    }

    public ElectricBeaconMenu(int containerId, ElectricBeaconBlockEntity entity, Inventory inventory) {
        this(containerId, new SimpleContainer(0), createBeaconData(entity), inventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    public static ContainerData createBeaconData() {
        return new SimpleContainerData(4);
    }

    public static ContainerData createBeaconData(ElectricBeaconBlockEntity entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public int get(int dataId) {
                switch (dataId) {
                    case 0 -> {
                        return entity.tier1Choice != null ? ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(0, List.of()).indexOf(entity.tier1Choice) : -1;
                    }
                    case 1 -> {
                        return entity.tier2Choice != null ? ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(1, List.of()).indexOf(entity.tier2Choice) : -1;
                    }
                    case 2 -> {
                        return entity.tier3Choice != null ? ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(2, List.of()).indexOf(entity.tier3Choice) : -1;
                    }
                    case 3 -> {
                        return entity.tier3Choice != null ? ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(3, List.of()).indexOf(entity.tier3Choice) : -1;
                    }
                }
                return 0;
            }

            @Override
            public void set(int dataId, int value) {
                switch (dataId) {
                    case 0 -> {
                        if (value == -1) {
                            entity.tier1Choice = null;
                            entity.setChanged();
                        } else if (value < ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(0, List.of()).size()) {
                            entity.tier1Choice = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(0, List.of()).get(value);
                            entity.setChanged();
                        }
                    }
                    case 1 -> {
                        if (value == -1) {
                            entity.tier2Choice = null;
                            entity.setChanged();
                        } else if (value < ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(1, List.of()).size()) {
                            entity.tier2Choice = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(1, List.of()).get(value);
                            entity.setChanged();
                        }
                    }
                    case 2 -> {
                        if (value == -1) {
                            entity.tier3Choice = null;
                            entity.setChanged();
                        } else if (value < ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(2, List.of()).size()) {
                            entity.tier3Choice = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(2, List.of()).get(value);
                            entity.setChanged();
                        }
                    }
                    case 3 -> {
                        if (value == -1) {
                            entity.tier4Choice = null;
                            entity.setChanged();
                        } else if (value < ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(3, List.of()).size()) {
                            entity.tier4Choice = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(3, List.of()).get(value);
                            entity.setChanged();
                        }
                    }
                }
            }
        };
    }

    public Optional<ElectricBeaconOption> getOption(int tier) {
        if (tier < 0 || tier > 3) {
            throw new IllegalArgumentException();
        }
        var idx = this.data.get(tier);
        var options = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(tier, List.of());
        if (idx < 0 || idx >= options.size()) {
            return Optional.empty();
        }
        return Optional.of(options.get(idx));
    }
}
