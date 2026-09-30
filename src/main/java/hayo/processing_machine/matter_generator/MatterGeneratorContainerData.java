package hayo.processing_machine.matter_generator;

import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public interface MatterGeneratorContainerData extends ContainerData {
    int SIZE = 10;

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

    default float recipeDuration() {
        return Mth.ceil(this.recipeCost() / (float) MatterGeneratorBlockEntity.ENERGY_USE_RATE) / 20F;
    }

    class Clientside extends SimpleContainerData implements MatterGeneratorContainerData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(MatterGeneratorBlockEntity entity) implements MatterGeneratorContainerData {
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
                case 4 -> this.entity.recipeState.progress & 0xFFFF;
                case 5 -> this.entity.recipeState.progress >>> 16;
                case 6 ->
                        this.entity.getRecipeCost(this.entity.recipeState.lastMatch != null ? this.entity.recipeState.lastMatch.value() : null) & 0xFFFF;
                case 7 ->
                        this.entity.getRecipeCost(this.entity.recipeState.lastMatch != null ? this.entity.recipeState.lastMatch.value() : null) >>> 16;
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
        }
    }
}
