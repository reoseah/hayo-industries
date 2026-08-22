package hayo.processing_machine;

import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public interface MachineContainerData extends ContainerData {
    int SIZE = 9;

    default int energy() {
        return (this.get(1) << 16) | (this.get(0) & 0xFFFF);
    }

    default int capacity() {
        return (this.get(3) << 16) | (this.get(2) & 0xFFFF);
    }

    default int recipeProgress() {
        return (this.get(5) << 16) | (this.get(4) & 0xFFFF);
    }

    default int recipeCost() {
        return (this.get(7) << 16) | (this.get(6) & 0xFFFF);
    }

    default int energyUseRate() {
        return this.get(8);
    }

    default float recipeDuration() {
        return Mth.ceil(this.recipeCost() / (float) this.energyUseRate()) / 20F;
    }

    class Clientside extends SimpleContainerData implements MachineContainerData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(MachineBlockEntity<?, ?> entity) implements MachineContainerData {
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
                case 4 -> this.entity.getRecipeProgress() & 0xFFFF;
                case 5 -> this.entity.getRecipeProgress() >>> 16;
                case 6 -> this.entity.getLastOrDefaultRecipeCost() & 0xFFFF;
                case 7 -> this.entity.getLastOrDefaultRecipeCost() >>> 16;
                case 8 -> this.entity.getEnergyUseRate();
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
        }
    }
}
