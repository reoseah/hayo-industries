package hayo.features.geothermal_generator;

import hayo.Hayo;
import hayo.common.blockentity.SimpleContainerBlockEntity;
import hayo.energy.block.EnergyGrid;
import hayo.features.fluid_stack.FluidDrainingRecipe;
import hayo.features.fluid_stack.FluidStack;
import hayo.features.processing_machine.RecipeState;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class GeothermalGeneratorBlockEntity extends SimpleContainerBlockEntity implements RecipeState.Owner<FluidDrainingRecipe, SingleRecipeInput> {
    public static final int INPUT = 0, OUTPUT = 1, SLOTS = 2;
    public static final int FLUID_CAPACITY = 4000;
    public static final int CAPACITY = 10000, ENERGY_PER_MILLIBUCKET = 30;
    public static final int TRANSFER_LIMIT = 32;

    protected FluidStack fluid = FluidStack.EMPTY;
    protected final RecipeState<FluidDrainingRecipe, SingleRecipeInput> drainingState = new RecipeState<>();
    @Getter
    protected int storedEnergy;

    public GeothermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.GEOTHERMAL_GENERATOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.geothermal_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GeothermalGeneratorMenu(containerId, this, inventory);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, GeothermalGeneratorBlockEntity entity) {
        boolean wasLit = !entity.fluid.isEmpty();

        entity.drainingState.tick(
                Hayo.RecipeTypes.FLUID_DRAINING,
                new SingleRecipeInput(entity.getItem(INPUT)),
                (ServerLevel) level,
                entity
        );

        if (!entity.fluid.isEmpty()) {
            entity.storedEnergy = Math.min(entity.storedEnergy + ENERGY_PER_MILLIBUCKET, CAPACITY);
            // TODO: create and use MutableFluidStack?
            entity.fluid = new FluidStack(entity.fluid.holder(), entity.fluid.amount() - 1);
            entity.setChanged();
        }

        boolean isLit = !entity.fluid.isEmpty();
        if (isLit != wasLit) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, isLit));
        }

        if (entity.storedEnergy > 0) {
            int sendable = Math.min(entity.storedEnergy, TRANSFER_LIMIT);
            int sent = EnergyGrid.trySendToAllSides(sendable, (ServerLevel) level, pos);
            if (sent > 0) {
                entity.storedEnergy -= sent;
                entity.setChanged();
            }
        }
    }

    @Override
    public RecipeState.EnergyState getEnergyState() {
        return new RecipeState.EnergyState.Sufficient(1);
    }

    @Override
    public boolean canCraft(RecipeHolder<FluidDrainingRecipe> holder, SingleRecipeInput input) {
        if (holder.value().resultFluid().fluid() != Fluids.LAVA) {
            return false;
        }

        if (!this.canInsertToSlot(holder.value().assemble(input), OUTPUT)) {
            return false;
        }
        if (this.fluid.fluid() == Fluids.EMPTY) {
            return true;
        }
        if (this.fluid.holder() == holder.value().resultFluid().holder()) {
            return FLUID_CAPACITY - this.fluid.amount() >= holder.value().resultFluid().amount();
        }
        return false;
    }

    @Override
    public int getRecipeCost(FluidDrainingRecipe holder) {
        return holder.ticks();
    }

    @Override
    public void craft(RecipeHolder<FluidDrainingRecipe> holder, SingleRecipeInput input) {
        var recipe = holder.value();

        this.stacks.get(INPUT).shrink(1);
        this.fluid = new FluidStack(recipe.resultFluid().holder(), this.fluid.amount() + recipe.resultFluid().amount());

        var recipeOutput = recipe.assemble(input);
        var existingStack = this.stacks.get(OUTPUT);
        if (!existingStack.isEmpty()) {
            existingStack.grow(recipeOutput.getCount());
        } else {
            this.stacks.set(OUTPUT, recipeOutput);
        }
    }
}
