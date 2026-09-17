package hayo.solid_fluid_reactor;

import hayo.Hayo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.material.Fluids;

public interface SolidFluidReactorContainerData extends ContainerData {
    int SIZE = 22;

    default int energy() {
        return (this.get(1) << 16) | (this.get(0) & 0xFFFF);
    }

    default int capacity() {
        return (this.get(3) << 16) | (this.get(2) & 0xFFFF);
    }

    default FluidStack inputFluid() {
        int id = (this.get(5) << 16) | (this.get(4) & 0xFFFF);
        var fluid = BuiltInRegistries.FLUID.get(id).orElse(Fluids.EMPTY.builtInRegistryHolder());
        return new FluidStack(fluid, this.get(6));
    }

    default FluidStack resultFluid() {
        int id = (this.get(8) << 16) | (this.get(7) & 0xFFFF);
        var fluid = BuiltInRegistries.FLUID.get(id).orElse(Fluids.EMPTY.builtInRegistryHolder());
        return new FluidStack(fluid, this.get(9));
    }

    default int inputDrainingProgress() {
        return (this.get(11) << 16) | this.get(10) & 0xFFFF;
    }

    default int inputDrainingMaxProgress() {
        return (this.get(13) << 16) | this.get(12) & 0xFFFF;
    }

    default int reactingProgress() {
        return (this.get(15) << 16) | this.get(14) & 0xFFFF;
    }

    default int reactingMaxProgress() {
        return (this.get(17) << 16) | this.get(16) & 0xFFFF;
    }

    default int resultFillingProgress() {
        return (this.get(19) << 16) | this.get(18) & 0xFFFF;
    }

    default int resultFillingMaxProgress() {
        return (this.get(21) << 16) | this.get(20) & 0xFFFF;
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

                case 7, 8 -> {
                    int id = BuiltInRegistries.FLUID.getId(this.entity.getResultFluid().fluid());
                    yield dataId == 7 ? id & 0xFFFF : id >>> 16;
                }
                case 9 -> (short) this.entity.getResultFluid().amount();

                case 10 -> this.entity.inputDraining.progress & 0xFFFF;
                case 11 -> this.entity.inputDraining.progress >>> 16;
                case 12, 13 -> {
                    var input = new SingleRecipeInput(this.entity.getItem(SolidFluidReactorBlockEntity.INPUT_TANK_INPUT));
                    var match = ((ServerLevel) this.entity.getLevel())
                            .recipeAccess()
                            .getRecipeFor(Hayo.RecipeTypes.FLUID_DRAINING, input, this.entity.getLevel(), this.entity.inputDraining.lastMatch);
                    int maxProgress = match.isPresent() ? match.get().value().energyCost() : 0;
                    yield dataId == 12 ? maxProgress & 0xFFFF : maxProgress >>> 16;
                }

                case 14 -> this.entity.reacting.progress & 0xFFFF;
                case 15 -> this.entity.reacting.progress >>> 16;
                case 16, 17 -> {
                    var input = new ItemFluidPairRecipeInput(
                            this.entity.getItem(SolidFluidReactorBlockEntity.INPUT),
                            this.entity.inputFluid
                    );
                    var match = ((ServerLevel) this.entity.getLevel())
                            .recipeAccess()
                            .getRecipeFor(Hayo.RecipeTypes.SOLID_FLUID_REACTING, input, this.entity.getLevel(), this.entity.reacting.lastMatch);
                    int maxProgress = match.isPresent() ? match.get().value().energyCost() : 0;
                    yield dataId == 16 ? maxProgress & 0xFFFF : maxProgress >>> 16;
                }

                case 18 -> this.entity.resultFilling.progress & 0xFFFF;
                case 19 -> this.entity.resultFilling.progress >>> 16;
                case 20, 21 -> {
                    var input = new ItemFluidPairRecipeInput(
                            this.entity.getItem(SolidFluidReactorBlockEntity.OUTPUT_TANK_INPUT),
                            this.entity.resultFluid
                    );
                    var match = ((ServerLevel) this.entity.getLevel())
                            .recipeAccess()
                            .getRecipeFor(Hayo.RecipeTypes.FLUID_FILLING, input, this.entity.getLevel(), this.entity.resultFilling.lastMatch);
                    int maxProgress = match.isPresent() ? match.get().value().energyCost() : 0;
                    yield dataId == 20 ? maxProgress & 0xFFFF : maxProgress >>> 16;
                }
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {

        }
    }
}
