package hayo.generator;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public interface GeneratorContainerData extends ContainerData {
    int SIZE = 6;

    default int energy() {
        return (this.get(1) << 16) | (this.get(0) & 0xFFFF);
    }

    default int fuelLeft() {
        return (this.get(3) << 16) | (this.get(2) & 0xFFFF);
    }

    default int fuelTotal() {
        return (this.get(5) << 16) | (this.get(4) & 0xFFFF);
    }

    class Clientside extends SimpleContainerData implements GeneratorContainerData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(GeneratorBlockEntity entity) implements GeneratorContainerData {
        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> this.entity.storedEnergy & 0xFFFF;
                case 1 -> this.entity.storedEnergy >>> 16;
                case 2 -> this.entity.fuelEnergyLeft & 0xFFFF;
                case 3 -> this.entity.fuelEnergyLeft >>> 16;
                case 4 -> this.entity.fuelEnergyTotal & 0xFFFF;
                case 5 -> this.entity.fuelEnergyTotal >>> 16;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }
    }
}
