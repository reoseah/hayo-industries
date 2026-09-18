package hayo.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.blockentity.EnergyReceiverBlockEntity;
import hayo.energy.item.EnergyComponents;
import hayo.fluid_stack.FluidDrainingRecipe;
import hayo.fluid_stack.FluidFillingRecipe;
import hayo.fluid_stack.FluidStack;
import hayo.fluid_stack.ItemFluidRecipeInput;
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
                entity.storedEnergy >= REACTING_ENERGY_RATE
                        ? new RecipeState.RecipeResourceState.Sufficient(REACTING_ENERGY_RATE)
                        : new RecipeState.RecipeResourceState.NotSufficient(-2 * REACTING_ENERGY_RATE),
                new RecipeState.Context<>() {
                    @Override
                    public void setChanged() {
                        entity.setChanged();
                    }

                    @Override
                    public boolean canCraft(RecipeHolder<SolidFluidReactingRecipe> recipe, ItemFluidRecipeInput input) {
                        if (!canInsertShapelessly(entity.stacks, recipe.value().resultItems(), OUTPUT_1, OUTPUT_3, entity.getMaxStackSize())) {
                            return false;
                        }

                        var resultFluid = recipe.value().resultFluid();
                        if (!resultFluid.isEmpty()) {
                            if (entity.resultFluid.fluid() != Fluids.EMPTY
                                    && entity.resultFluid.holder() != resultFluid.holder()) {
                                return false;
                            }
                            return FLUID_CAPACITY - entity.resultFluid.amount() >= resultFluid.amount();
                        }

                        return true;
                    }

                    @Override
                    public int getRecipeCost(SolidFluidReactingRecipe recipe) {
                        return recipe.energyCost();
                    }

                    @Override
                    public void craft(RecipeHolder<SolidFluidReactingRecipe> recipeHolder, ItemFluidRecipeInput input) {
                        var recipe = recipeHolder.value();

                        entity.stacks.get(INPUT).shrink(1);
                        if (recipe.inputFluid().amount() > 0) {
                            entity.inputFluid = new FluidStack(entity.inputFluid.holder(), entity.inputFluid.amount() - recipe.inputFluid().amount());
                        }

                        for (var result : recipe.resultItems()) {
                            insertShapeless(entity.stacks, result.create(), OUTPUT_1, OUTPUT_3, entity.getMaxStackSize());
                        }

                        var resultFluid = recipe.resultFluid();
                        if (!resultFluid.isEmpty()) {
                            entity.resultFluid = new FluidStack(resultFluid.holder(), entity.resultFluid.amount() + resultFluid.amount());
                        }
                    }
                }
        );
        entity.storedEnergy -= entity.inputDraining.tick(
                Hayo.RecipeTypes.FLUID_DRAINING,
                new SingleRecipeInput(entity.getItem(INPUT_TANK_INPUT)),
                (ServerLevel) level,
                entity.storedEnergy >= 1
                        ? new RecipeState.RecipeResourceState.Sufficient(1)
                        : new RecipeState.RecipeResourceState.NotSufficient(-2),
                new RecipeState.Context<>() {
                    @Override
                    public void setChanged() {
                        entity.setChanged();
                    }

                    @Override
                    public boolean canCraft(RecipeHolder<FluidDrainingRecipe> recipe, SingleRecipeInput input) {
                        if (!entity.canInsertToSlot(recipe.value().assemble(input), INPUT_TANK_OUTPUT)) {
                            return false;
                        }
                        if (entity.inputFluid.fluid() == Fluids.EMPTY) {
                            return true;
                        }
                        if (entity.inputFluid.holder() == recipe.value().resultFluid().holder()) {
                            return FLUID_CAPACITY - entity.inputFluid.amount() >= recipe.value().resultFluid().amount();
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

                        entity.stacks.get(INPUT_TANK_INPUT).shrink(1);
                        entity.inputFluid = new FluidStack(recipe.resultFluid().holder(), entity.inputFluid.amount() + recipe.resultFluid().amount());

                        var recipeOutput = recipe.assemble(input);
                        var existingStack = entity.stacks.get(INPUT_TANK_OUTPUT);
                        if (!existingStack.isEmpty()) {
                            existingStack.grow(recipeOutput.getCount());
                        } else {
                            entity.stacks.set(INPUT_TANK_OUTPUT, recipeOutput);
                        }
                    }
                }
        );
        entity.storedEnergy -= entity.resultFilling.tick(
                Hayo.RecipeTypes.FLUID_FILLING,
                new ItemFluidRecipeInput(entity.getItem(OUTPUT_TANK_INPUT), entity.resultFluid),
                (ServerLevel) level,
                entity.storedEnergy >= 1
                        ? new RecipeState.RecipeResourceState.Sufficient(1)
                        : new RecipeState.RecipeResourceState.NotSufficient(-2),
                new RecipeState.Context<>() {
                    @Override
                    public void setChanged() {
                        entity.setChanged();
                    }

                    @Override
                    public boolean canCraft(RecipeHolder<FluidFillingRecipe> recipe, ItemFluidRecipeInput input) {
                        return entity.canInsertToSlot(recipe.value().assemble(input), OUTPUT_TANK_OUTPUT);
                    }

                    @Override
                    public int getRecipeCost(FluidFillingRecipe recipe) {
                        return recipe.energyCost();
                    }

                    @Override
                    public void craft(RecipeHolder<FluidFillingRecipe> recipe, ItemFluidRecipeInput input) {
                        entity.stacks.get(OUTPUT_TANK_INPUT).shrink(1);
                        entity.resultFluid = new FluidStack(entity.resultFluid.holder(), entity.resultFluid.amount() - recipe.value().inputFluid().amount());

                        var recipeOutput = recipe.value().assemble(input);
                        var existingStack = entity.stacks.get(OUTPUT_TANK_OUTPUT);
                        if (!existingStack.isEmpty()) {
                            existingStack.grow(recipeOutput.getCount());
                        } else {
                            entity.stacks.set(OUTPUT_TANK_OUTPUT, recipeOutput);
                        }
                    }
                }
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
}
