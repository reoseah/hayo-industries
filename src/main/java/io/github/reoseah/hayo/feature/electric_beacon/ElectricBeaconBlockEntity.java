package io.github.reoseah.hayo.feature.electric_beacon;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.entity.ElectricBlockEntity;
import io.github.reoseah.hayo.feature.energy.item.EnergyComponents;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

@Accessors(fluent = true)
public class ElectricBeaconBlockEntity extends ElectricBlockEntity {
    @Getter
    protected int levels;
    @Getter
    protected ElectricBeaconOption[] choices = { //
            ElectricBeacon.NONE_1, //
            ElectricBeacon.NONE_2, //
            ElectricBeacon.NONE_3, //
            ElectricBeacon.NONE_4 //
    };

    @Getter
    @Setter
    protected boolean choicesChanged = true;

    @Getter
    @Setter
    protected int wirelessTransmissionRate = 0;
    @Getter
    @Setter
    protected AABB area = new AABB(0, 0, 0, 0, 0, 0);

    protected List<Player> players = List.of();

    public ElectricBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.ELECTRIC_BEACON, pos, state);
    }

    @Override
    protected NonNullList<ItemStack> createInventory() {
        return NonNullList.withSize(1, ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.electric_beacon");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricBeaconMenu(containerId, this, inventory);
    }

    @Override
    protected int getEnergyCapacity() {
        return 10000;
    }

    @Override
    protected int getEnergyTransferRate() {
        return 512;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < 4; i++) {
            if (this.choices[i] != ElectricBeacon.OPTIONS_BY_TIER.get(i).getFirst()) {
                output.store("chosen_option_" + i, ElectricBeacon.CODEC, this.choices[i]);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < 4; i++) {
            this.choices[i] = input.read("chosen_option_" + i, ElectricBeacon.CODEC).orElse(ElectricBeacon.OPTIONS_BY_TIER.get(i).getFirst());
        }
        this.choicesChanged = true;
    }

    public void setChoice(int level, ElectricBeaconOption option) {
        if (option != this.choices[level]) {
            this.choices[level] = option;
            this.choicesChanged = true;
            this.setChanged();
        }
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ElectricBeaconBlockEntity entity) {
        entity.chargeFromSlot(0);

        if (level.getGameTime() % 80 == 0) {
            updateLevels(level, pos, state, entity);
            updateTickingStats(entity);
            entity.choicesChanged = false;
        } else if (entity.choicesChanged) {
            updateTickingStats(entity);
            entity.choicesChanged = false;
        }

        if (entity.levels > 0 && level.getGameTime() % 80 == 0) {
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            updatePlayerList(level, pos, entity);
        }

        int energyToSend = Math.min(entity.wirelessTransmissionRate, entity.storedEnergy);
        int sentEnergy = 0;

        for (var player : entity.players) {
            if (player.isAlive()) {
                int moved = EnergyComponents.spreadEnergy(player, energyToSend, null);
                if (moved > 0) {
                    player.getInventory().setChanged();
                    energyToSend -= moved;
                    sentEnergy += moved;
                    if (energyToSend == 0) {
                        break;
                    }
                }
            }
        }
        if (sentEnergy > 0) {
            entity.storedEnergy -= sentEnergy;
            entity.setChanged();
            if (!state.getValue(ElectricBeaconBlock.TRANSFERRING)) {
                level.setBlockAndUpdate(pos, state.setValue(ElectricBeaconBlock.TRANSFERRING, true));
            }
        } else if (state.getValue(ElectricBeaconBlock.TRANSFERRING)) {
            level.setBlockAndUpdate(pos, state.setValue(ElectricBeaconBlock.TRANSFERRING, false));
        }

        entity.onTickEnd();
    }

    private static void updatePlayerList(Level level, BlockPos pos, ElectricBeaconBlockEntity entity) {
        entity.players = new ArrayList<>(level.getEntitiesOfClass(Player.class, entity.area));
    }

    private static void updateTickingStats(ElectricBeaconBlockEntity entity) {
        var areaCenter = entity.worldPosition;
        int baseUsageRate = 0;
        int baseHorizontalRange = 1;
        int baseVerticalRange = 1;
        int usageMultiplier = 1;
        int horizontalRangeMultiplier = 1;
        int verticalRangeMultiplier = 1;

        if (entity.levels >= 1) {
            if (entity.choices[0] == ElectricBeacon.WIRELESS_CHARGE) {
                baseUsageRate = 32;
                baseHorizontalRange = 10;
                baseVerticalRange = 10;
            }
        }

        if (entity.levels >= 2) {
            if (entity.choices[1] == ElectricBeacon.HORIZONTAL_RANGE_1) {
                horizontalRangeMultiplier += 1;
            } else if (entity.choices[1] == ElectricBeacon.VERTICAL_RANGE_1) {
                verticalRangeMultiplier += 1;
            } else if (entity.choices[1] == ElectricBeacon.POWER_1) {
                usageMultiplier += 1;
            } else if (entity.choices[1] == ElectricBeacon.CHARGE_PAD) {
                baseVerticalRange = 1;
                baseHorizontalRange = 1;
                areaCenter = entity.worldPosition.above();
                usageMultiplier += 7;
            }
        }

        if (entity.levels >= 3) {
            if (entity.choices[2] == ElectricBeacon.HORIZONTAL_RANGE_2) {
                horizontalRangeMultiplier += 1;
            } else if (entity.choices[2] == ElectricBeacon.VERTICAL_RANGE_2) {
                verticalRangeMultiplier += 1;
            } else if (entity.choices[2] == ElectricBeacon.POWER_2) {
                usageMultiplier += 1;
            }
        }

        if (entity.levels >= 4) {
            if (entity.choices[3] == ElectricBeacon.HORIZONTAL_RANGE_3) {
                horizontalRangeMultiplier += 2;
            } else if (entity.choices[3] == ElectricBeacon.VERTICAL_RANGE_3) {
                verticalRangeMultiplier += 2;
            } else if (entity.choices[3] == ElectricBeacon.POWER_3) {
                usageMultiplier += 1;
            }
        }

        entity.wirelessTransmissionRate = baseUsageRate * usageMultiplier;
        entity.area = new AABB(areaCenter).inflate(baseHorizontalRange * horizontalRangeMultiplier, baseVerticalRange * verticalRangeMultiplier, baseHorizontalRange * horizontalRangeMultiplier);
    }

    private static void updateLevels(Level level, BlockPos pos, BlockState state, ElectricBeaconBlockEntity entity) {
        int previousLevels = entity.levels;
        entity.levels = getLevels(level, pos);
        if (entity.levels != previousLevels) {
            if (entity.levels > 0) {
                if (!state.getValue(ElectricBeaconBlock.LIT)) {
                    level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.setBlockAndUpdate(pos, state.setValue(ElectricBeaconBlock.LIT, true));
                }
            } else {
                level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(pos, state.setValue(ElectricBeaconBlock.LIT, false));
            }
        }
    }

    private static int getLevels(Level level, BlockPos pos) {
        int levels = 0;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int step = 1; step <= 4; levels = step++) {
            int ly = y - step;
            if (ly < level.getMinY()) {
                break;
            }

            for (int lx = x - step; lx <= x + step; lx++) {
                for (int lz = z - step; lz <= z + step; lz++) {
                    if (!level.getBlockState(new BlockPos(lx, ly, lz)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        return levels;
                    }
                }
            }
        }

        return levels;
    }
}
