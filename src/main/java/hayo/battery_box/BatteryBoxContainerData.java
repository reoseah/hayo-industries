package hayo.battery_box;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public interface BatteryBoxContainerData extends ContainerData {
    int SIZE = 6;

    default int energy() {
        return (this.get(1) << 16) | (this.get(0) & 0xFFFF);
    }

    default int capacity() {
        return (this.get(3) << 16) | (this.get(2) & 0xFFFF);
    }

    default float averageInput() {
        return this.get(4) / 10F;
    }

    default float averageOutput() {
        return this.get(5) / 10F;
    }

    class Clientside extends SimpleContainerData implements BatteryBoxContainerData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(BatteryBoxBlockEntity entity) implements BatteryBoxContainerData {
        @Override
        public int getCount() {
            return SIZE;
        }

        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0 -> this.entity.getStoredEnergy() & 0xFFFF;
                case 1 -> this.entity.getStoredEnergy() >>> 16;
                case 2 -> this.entity.getEnergyCapacity() & 0xFFFF;
                case 3 -> this.entity.getEnergyCapacity() >>> 16;
                case 4 -> Math.round(this.entity.getAverageInput() * 10);
                case 5 -> Math.round(this.entity.getAverageOutput() * 10);
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
        }
    }
}
