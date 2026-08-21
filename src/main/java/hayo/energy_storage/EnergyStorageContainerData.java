package hayo.energy_storage;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public interface EnergyStorageContainerData extends ContainerData {
    int COUNT = 6;

    default int energy() {
        return (this.get(1) << 16) | (this.get(0) & 0xFFFF);
    }

    default int capacity() {
        return (this.get(3) << 16) | (this.get(2) & 0xFFFF);
    }

    default float averageInputPerTick() {
        return this.get(4) / 10F;
    }

    default float averageOutputPerTick() {
        return this.get(5) / 10F;
    }

    class Clientside extends SimpleContainerData implements EnergyStorageContainerData {
        public Clientside() {
            super(COUNT);
        }
    }

    record Serverside(EnergyStorageBlockEntity entity) implements EnergyStorageContainerData {
        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> this.entity.getStoredEnergy() & 0xFFFF;
                case 1 -> this.entity.getStoredEnergy() >>> 16;
                case 2 -> this.entity.getEnergyCapacity() & 0xFFFF;
                case 3 -> this.entity.getEnergyCapacity() >>> 16;
                case 4 -> Math.round(this.entity.averageInputPerTick * 10);
                case 5 -> Math.round(this.entity.averageOutputPerTick * 10);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }
    }
}
