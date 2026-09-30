package hayo.processing_machine.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.HayoContainerUtils;
import hayo.common.blockentity.EnergyReceiverBlockEntity;
import hayo.energy.item.EnergyComponents;
import hayo.fluid_stack.*;
import hayo.processing_machine.RecipeState;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SolidFluidReactorBlockEntity extends EnergyReceiverBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 13;

    public static final int INPUT = 0, INPUT_TANK_INPUT = 1, OUTPUT_TANK_INPUT = 2;
    public static final int BATTERY = 3;
    public static final int OUTPUT_1 = 4, OUTPUT_2 = 5, OUTPUT_3 = 6, INPUT_TANK_OUTPUT = 7, OUTPUT_TANK_OUTPUT = 8;
    public static final int UPGRADE_1 = 9;
    public static final int UPGRADES = 4;

    public static final int FLUID_CAPACITY = 4000;

    public static final int REACTING_ENERGY_RATE = 2;

    @Getter
    protected FluidStack inputFluid = FluidStack.EMPTY;
    @Getter
    protected FluidStack resultFluid = FluidStack.EMPTY;

    protected final RecipeState<FluidDrainingRecipe, SingleRecipeInput> inputDraining = new RecipeState<>();
    protected final RecipeState<SolidFluidReactingRecipe, ItemFluidRecipeInput> reacting = new RecipeState<>();
    protected final RecipeState<FluidFillingRecipe, ItemFluidRecipeInput> resultFilling = new RecipeState<>();

    public SolidFluidReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.SOLID_FLUID_REACTOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SolidFluidReactorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.resetEnergyPerTick();
        entity.storedEnergy -= entity.reacting.tick(
                Hayo.RecipeTypes.SOLID_FLUID_REACTING,
                new ItemFluidRecipeInput(entity.getItem(INPUT), entity.inputFluid),
                (ServerLevel) level,
                new ReactingHelper(entity)
        );
        entity.storedEnergy -= entity.inputDraining.tick(
                Hayo.RecipeTypes.FLUID_DRAINING,
                new SingleRecipeInput(entity.getItem(INPUT_TANK_INPUT)),
                (ServerLevel) level,
                new InputDrainingHelper(entity)
        );
        entity.storedEnergy -= entity.resultFilling.tick(
                Hayo.RecipeTypes.FLUID_FILLING,
                new ItemFluidRecipeInput(entity.getItem(OUTPUT_TANK_INPUT), entity.resultFluid),
                (ServerLevel) level,
                new ResultFillingHelper(entity)
        );
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.solid_fluid_reactor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SolidFluidReactorMenu(containerId, this, inventory);
    }

    @Override
    public int getEnergyCapacity() {
        return 1000;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return 32;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("input_fluid", FluidStack.CODEC, this.inputFluid);
        output.store("result_fluid", FluidStack.CODEC, this.resultFluid);
        output.putInt("input_filling_progress", this.inputDraining.progress);
        output.putInt("reacting_progress", this.reacting.progress);
        output.putInt("result_draining_progress", this.resultFilling.progress);
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inputFluid = input.read("input_fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        this.resultFluid = input.read("result_fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        this.inputDraining.progress = input.getIntOr("input_filling_progress", 0);
        this.reacting.progress = input.getIntOr("reacting_progress", 0);
        this.resultFilling.progress = input.getIntOr("result_draining_progress", 0);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            case UP -> new int[]{INPUT, INPUT_TANK_INPUT, OUTPUT_TANK_INPUT};
            case DOWN -> new int[]{OUTPUT_1, OUTPUT_2, OUTPUT_3, INPUT_TANK_OUTPUT, OUTPUT_TANK_OUTPUT};
            case NORTH, WEST, SOUTH, EAST -> new int[]{BATTERY};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return switch (slot) {
            case INPUT -> true;
            case INPUT_TANK_INPUT -> this.level instanceof ServerLevel serverLevel
                    && serverLevel.recipeAccess()
                    .getRecipeFor(Hayo.RecipeTypes.FLUID_DRAINING, new SingleRecipeInput(stack), this.level, this.inputDraining.lastMatch)
                    .isPresent();
            case OUTPUT_TANK_INPUT -> this.level instanceof ServerLevel serverLevel
                    && serverLevel.recipeAccess()
                    .getRecipeFor(Hayo.RecipeTypes.FLUID_FILLING, new ItemFluidRecipeInput(stack, this.resultFluid), this.level, this.resultFilling.lastMatch)
                    .isPresent();
            case BATTERY -> EnergyComponents.chargesBlocks(stack);
            default -> false;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot >= OUTPUT_1 && slot < UPGRADE_1;
    }

    protected record ReactingHelper(
            SolidFluidReactorBlockEntity entity) implements RecipeState.Context<SolidFluidReactingRecipe, ItemFluidRecipeInput> {

        @Override
        public void setChanged() {
            this.entity.setChanged();
        }

        @Override
        public RecipeState.EnergyState getEnergyState() {
            return this.entity.storedEnergy >= REACTING_ENERGY_RATE
                    ? new RecipeState.EnergyState.Sufficient(REACTING_ENERGY_RATE)
                    : new RecipeState.EnergyState.NotSufficient(-2 * REACTING_ENERGY_RATE);
        }

        @Override
        public boolean canCraft(RecipeHolder<SolidFluidReactingRecipe> recipe, ItemFluidRecipeInput input) {
            if (!HayoContainerUtils.canInsertShapelessly(this.entity.stacks, recipe.value().resultItems, OUTPUT_1, OUTPUT_3, this.entity.getMaxStackSize())) {
                return false;
            }

            var resultFluid = recipe.value().resultFluid;
            if (!resultFluid.isEmpty()) {
                if (this.entity.resultFluid.fluid() != Fluids.EMPTY
                        && this.entity.resultFluid.holder() != resultFluid.holder()) {
                    return false;
                }
                return FLUID_CAPACITY - this.entity.resultFluid.amount() >= resultFluid.amount();
            }

            return true;
        }

        @Override
        public int getRecipeCost(SolidFluidReactingRecipe recipe) {
            return recipe.energyCost;
        }

        @Override
        public void craft(RecipeHolder<SolidFluidReactingRecipe> recipeHolder, ItemFluidRecipeInput input) {
            var recipe = recipeHolder.value();

            this.entity.stacks.get(INPUT).shrink(1);
            if (recipe.inputFluid.amount() > 0) {
                this.entity.inputFluid = new FluidStack(this.entity.inputFluid.holder(), this.entity.inputFluid.amount() - recipe.inputFluid.amount());
            }

            for (var result : recipe.resultItems) {
                HayoContainerUtils.insertShapelessly(this.entity.stacks, result.create(), OUTPUT_1, OUTPUT_3, this.entity.getMaxStackSize());
            }

            var resultFluid = recipe.resultFluid;
            if (!resultFluid.isEmpty()) {
                this.entity.resultFluid = new FluidStack(resultFluid.holder(), this.entity.resultFluid.amount() + resultFluid.amount());
            }
        }
    }

    protected record InputDrainingHelper(
            SolidFluidReactorBlockEntity entity) implements RecipeState.Context<FluidDrainingRecipe, SingleRecipeInput> {
        @Override
        public void setChanged() {
            this.entity.setChanged();
        }

        @Override
        public RecipeState.EnergyState getEnergyState() {
            return this.entity.storedEnergy >= 1
                    ? new RecipeState.EnergyState.Sufficient(1)
                    : new RecipeState.EnergyState.NotSufficient(-2);
        }

        @Override
        public boolean canCraft(RecipeHolder<FluidDrainingRecipe> recipe, SingleRecipeInput input) {
            if (!this.entity.canInsertToSlot(recipe.value().assemble(input), INPUT_TANK_OUTPUT)) {
                return false;
            }
            if (this.entity.inputFluid.fluid() == Fluids.EMPTY) {
                return true;
            }
            if (this.entity.inputFluid.holder() == recipe.value().resultFluid().holder()) {
                return FLUID_CAPACITY - this.entity.inputFluid.amount() >= recipe.value().resultFluid().amount();
            }
            return false;
        }

        @Override
        public int getRecipeCost(FluidDrainingRecipe recipe) {
            return recipe.energyCost();
        }

        @Override
        public void craft(RecipeHolder<FluidDrainingRecipe> recipeHolder, SingleRecipeInput input) {
            var recipe = recipeHolder.value();

            this.entity.stacks.get(INPUT_TANK_INPUT).shrink(1);
            this.entity.inputFluid = new FluidStack(recipe.resultFluid().holder(), this.entity.inputFluid.amount() + recipe.resultFluid().amount());

            var recipeOutput = recipe.assemble(input);
            var existingStack = this.entity.stacks.get(INPUT_TANK_OUTPUT);
            if (!existingStack.isEmpty()) {
                existingStack.grow(recipeOutput.getCount());
            } else {
                this.entity.stacks.set(INPUT_TANK_OUTPUT, recipeOutput);
            }
        }
    }

    protected record ResultFillingHelper(
            SolidFluidReactorBlockEntity entity) implements RecipeState.Context<FluidFillingRecipe, ItemFluidRecipeInput> {
        @Override
        public void setChanged() {
            this.entity.setChanged();
        }

        @Override
        public RecipeState.EnergyState getEnergyState() {
            return this.entity.storedEnergy >= 1
                    ? new RecipeState.EnergyState.Sufficient(1)
                    : new RecipeState.EnergyState.NotSufficient(-2);
        }

        @Override
        public boolean canCraft(RecipeHolder<FluidFillingRecipe> recipe, ItemFluidRecipeInput input) {
            return this.entity.canInsertToSlot(recipe.value().assemble(input), OUTPUT_TANK_OUTPUT);
        }

        @Override
        public int getRecipeCost(FluidFillingRecipe recipe) {
            return recipe.energyCost();
        }

        @Override
        public void craft(RecipeHolder<FluidFillingRecipe> recipe, ItemFluidRecipeInput input) {
            this.entity.stacks.get(OUTPUT_TANK_INPUT).shrink(1);
            this.entity.resultFluid = new FluidStack(this.entity.resultFluid.holder(), this.entity.resultFluid.amount() - recipe.value().inputFluid().amount());

            var recipeOutput = recipe.value().assemble(input);
            var existingStack = this.entity.stacks.get(OUTPUT_TANK_OUTPUT);
            if (!existingStack.isEmpty()) {
                existingStack.grow(recipeOutput.getCount());
            } else {
                this.entity.stacks.set(OUTPUT_TANK_OUTPUT, recipeOutput);
            }
        }
    }
}
