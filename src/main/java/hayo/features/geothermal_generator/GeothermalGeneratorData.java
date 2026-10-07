package hayo.features.geothermal_generator;

import hayo.Hayo;
import hayo.features.fluid_stack.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.material.Fluids;

public interface GeothermalGeneratorData extends ContainerData {
    int SIZE = 7;

    default FluidStack fluid() {
        int id = (this.get(1) << 16) | (this.get(0) & 0xFFFF);
        var fluid = BuiltInRegistries.FLUID.get(id).orElse(Fluids.EMPTY.builtInRegistryHolder());
        return new FluidStack(fluid, this.get(2));
    }

    default int drainingProgress() {
        return (this.get(4) << 16) | this.get(3) & 0xFFFF;
    }

    default int drainingMaxProgress() {
        return (this.get(6) << 16) | this.get(5) & 0xFFFF;
    }

    class Clientside extends SimpleContainerData implements GeothermalGeneratorData {
        public Clientside() {
            super(SIZE);
        }
    }

    record Serverside(GeothermalGeneratorBlockEntity entity) implements GeothermalGeneratorData {
        @Override
        public int getCount() {
            return SIZE;
        }

        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0, 1 -> {
                    int id = BuiltInRegistries.FLUID.getId(this.entity.fluid.fluid());
                    yield dataId == 0 ? id & 0xFFFF : id >>> 16;
                }
                case 2 -> (short) this.entity.fluid.amount();

                case 3, 4 -> {
                    int progress = this.entity.drainingState.progress;
                    yield dataId == 3 ? (progress & 0xFFFF) : (progress >>> 16);
                }
                case 5, 6 -> {
                    var input = new SingleRecipeInput(this.entity.getItem(GeothermalGeneratorBlockEntity.INPUT));
                    var match = ((ServerLevel) this.entity.getLevel())
                            .recipeAccess()
                            .getRecipeFor(Hayo.RecipeTypes.FLUID_DRAINING, input, this.entity.getLevel(), this.entity.drainingState.lastMatch);
                    int maxProgress = match.isPresent() ? match.get().value().ticks() : 0;
                    yield dataId == 5 ? maxProgress & 0xFFFF : maxProgress >>> 16;
                }

                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {

        }
    }
}
