package hayo.processing_machine;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public interface UpgradableMachineContainerData extends ContainerData {
    int SIZE = 5;

    default float extraCraftingSpeed() {
        return this.get(0);
    }

    default float extraRecipeCost() {
        return this.get(1);
    }

    default int extraCapacity() {
        return this.get(2);
    }

    default boolean hasInductionUpgrade() {
        return this.get(3) == 1;
    }

    default int inductionHeat() {
        return this.get(4);
    }

    class Clientside extends SimpleContainerData implements UpgradableMachineContainerData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(UpgradableMachineBlockEntity<?, ?> entity) implements UpgradableMachineContainerData {
        @Override
        public int getCount() {
            return SIZE;
        }

        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0 -> (int) (this.entity.extraCraftingSpeed * 100);
                case 1 -> (int) (this.entity.extraRecipeCost * 100);
                case 2 -> this.entity.extraCapacity;
                case 3 -> this.entity.hasInductionUpgrade ? 1 : 0;
                case 4 -> (this.entity.inductionHeat / 10) * 10;
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
        }
    }
}
