package hayo.features.processing_machine.solid_fluid_reactor;

import hayo.Hayo;
import hayo.features.fluid_stack.FluidStack;
import hayo.features.fluid_stack.ItemFluidRecipeInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.material.Fluids;

public interface SolidFluidReactorContainerData extends ContainerData {
    int SIZE = 15;

    default FluidStack inputFluid() {
        int id = (this.get(1) << 16) | (this.get(0) & 0xFFFF);
        var fluid = BuiltInRegistries.FLUID.get(id).orElse(Fluids.EMPTY.builtInRegistryHolder());
        return new FluidStack(fluid, this.get(2));
    }

    default FluidStack resultFluid() {
        int id = (this.get(4) << 16) | (this.get(3) & 0xFFFF);
        var fluid = BuiltInRegistries.FLUID.get(id).orElse(Fluids.EMPTY.builtInRegistryHolder());
        return new FluidStack(fluid, this.get(5));
    }

    default int inputDrainingProgress() {
        return (this.get(7) << 16) | this.get(6) & 0xFFFF;
    }

    default int inputDrainingMaxProgress() {
        return (this.get(9) << 16) | this.get(8) & 0xFFFF;
    }

    default int resultFillingProgress() {
        return (this.get(11) << 16) | this.get(10) & 0xFFFF;
    }

    default int resultFillingMaxProgress() {
        return (this.get(13) << 16) | this.get(12) & 0xFFFF;
    }

    default SolidFluidReactorMode mode() {
        return SolidFluidReactorMode.values()[Math.clamp(this.get(14), 0, SolidFluidReactorMode.values().length - 1)];
    }

    default RecipeType<? extends BaseSolidFluidReactingRecipe> recipeType() {
        return this.mode().recipeType;
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
                case 0, 1 -> {
                    int id = BuiltInRegistries.FLUID.getId(this.entity.getInputFluid().fluid());
                    yield dataId == 0 ? id & 0xFFFF : id >>> 16;
                }
                case 2 -> (short) this.entity.getInputFluid().amount();

                case 3, 4 -> {
                    int id = BuiltInRegistries.FLUID.getId(this.entity.getResultFluid().fluid());
                    yield dataId == 3 ? id & 0xFFFF : id >>> 16;
                }
                case 5 -> (short) this.entity.getResultFluid().amount();

                case 6 -> this.entity.inputDraining.progress & 0xFFFF;
                case 7 -> this.entity.inputDraining.progress >>> 16;
                case 8, 9 -> {
                    var input = new SingleRecipeInput(this.entity.getItem(SolidFluidReactorBlockEntity.INPUT_TANK_INPUT));
                    var match = ((ServerLevel) this.entity.getLevel())
                            .recipeAccess()
                            .getRecipeFor(Hayo.RecipeTypes.FLUID_DRAINING, input, this.entity.getLevel(), this.entity.inputDraining.lastMatch);
                    int maxProgress = match.isPresent() ? match.get().value().energyCost() : 0;
                    yield dataId == 8 ? maxProgress & 0xFFFF : maxProgress >>> 16;
                }

                case 10 -> this.entity.resultFilling.progress & 0xFFFF;
                case 11 -> this.entity.resultFilling.progress >>> 16;
                case 12, 13 -> {
                    var input = new ItemFluidRecipeInput(
                            this.entity.getItem(SolidFluidReactorBlockEntity.OUTPUT_TANK_INPUT),
                            this.entity.resultFluid
                    );
                    var match = ((ServerLevel) this.entity.getLevel())
                            .recipeAccess()
                            .getRecipeFor(Hayo.RecipeTypes.FLUID_FILLING, input, this.entity.getLevel(), this.entity.resultFilling.lastMatch);
                    int maxProgress = match.isPresent() ? match.get().value().energyCost() : 0;
                    yield dataId == 12 ? maxProgress & 0xFFFF : maxProgress >>> 16;
                }
                case 14 -> this.entity.mode.ordinal();

                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {

        }

        @Override
        public FluidStack inputFluid() {
            return this.entity.inputFluid;
        }

        @Override
        public FluidStack resultFluid() {
            return this.entity.resultFluid;
        }
    }
}
