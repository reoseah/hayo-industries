package hayo.solid_fluid_reactor;

import hayo.Hayo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface SolidFluidReactorContainerData extends ContainerData {
    int SIZE = 11;

    default int energy() {
        return (this.get(1) << 16) | (this.get(0) & 0xFFFF);
    }

    default int capacity() {
        return (this.get(3) << 16) | (this.get(2) & 0xFFFF);
    }

    default Holder<Fluid> inputFluid() {
        int id = (this.get(5) << 16) | (this.get(4) & 0xFFFF);
        return BuiltInRegistries.FLUID.get(id).orElse(Fluids.EMPTY.builtInRegistryHolder());
    }

    default int inputFluidAmount() {
        return this.get(6);
    }

    default int drainProgress() {
        return (this.get(8) << 16) | this.get(7) & 0xFFFF;
    }

    default int maxDrainProgress() {
        return (this.get(10) << 16) | this.get(9) & 0xFFFF;
    }

    class Clientside extends SimpleContainerData implements SolidFluidReactorContainerData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(SolidFluidReactorBlockEntity entity) implements SolidFluidReactorContainerData {
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
                case 4, 5 -> {
                    int id = BuiltInRegistries.FLUID.getId(this.entity.getInputFluid().fluid());
                    yield dataId == 4 ? id & 0xFFFF : id >>> 16;
                }
                case 6 -> (short) this.entity.getInputFluid().amount();
                case 7 -> this.entity.draining.progress & 0xFFFF;
                case 8 -> this.entity.draining.progress >>> 16;
                case 9, 10 -> {
                    var input = new SingleRecipeInput(this.entity.getItem(SolidFluidReactorBlockEntity.DRAIN_INPUT));
                    var match = ((ServerLevel) this.entity.getLevel())
                            .recipeAccess()
                            .getRecipeFor(Hayo.RecipeTypes.DRAINING, input, this.entity.getLevel(), this.entity.draining.lastMatch);
                    int maxProgress = match.isPresent() ? match.get().value().energyCost() : 0;
                    yield dataId == 9 ? maxProgress & 0xFFFF : maxProgress >>> 16;
                }
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {

        }
    }
}
