package hayo.processing_machine;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public interface UpgradableMachineData extends ContainerData {
    int SIZE = 15;

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

    default boolean matchesRecipe() {
        return this.get(9) == 1;
    }

    default float extraCraftingSpeed() {
        return this.get(10);
    }

    default float extraRecipeCost() {
        return this.get(11);
    }

    default int extraCapacity() {
        return this.get(12);
    }

    default boolean hasInductionUpgrade() {
        return this.get(13) == 1;
    }

    default int inductionHeat() {
        return this.get(14);
    }

    class Clientside extends SimpleContainerData implements UpgradableMachineData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(UpgradableMachineBlockEntity<?, ?> entity) implements UpgradableMachineData {
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
                case 6 -> this.entity.getLastOrDefaultRecipeCost() & 0xFFFF;
                case 7 -> this.entity.getLastOrDefaultRecipeCost() >>> 16;
                case 8 -> this.entity.getEnergyUseRate();
                case 9 -> this.entity.hasRecipe() ? 1 : 0; case 10 -> (int) (this.entity.extraCraftingSpeed * 100);
                case 11 -> (int) (this.entity.extraRecipeCost * 100);
                case 12 -> this.entity.extraCapacity;
                case 13 -> this.entity.hasInductionUpgrade ? 1 : 0;
                case 14 -> (this.entity.inductionHeat / 10) * 10;
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
        }
    }
}
