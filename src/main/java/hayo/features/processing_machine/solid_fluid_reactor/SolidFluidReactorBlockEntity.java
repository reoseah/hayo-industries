package hayo.features.processing_machine.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.HayoContainerUtils;
import hayo.common.IntRange;
import hayo.energy.item.EnergyComponents;
import hayo.features.fluid_stack.FluidDrainingRecipe;
import hayo.features.fluid_stack.FluidFillingRecipe;
import hayo.features.fluid_stack.FluidStack;
import hayo.features.fluid_stack.ItemFluidRecipeInput;
import hayo.features.processing_machine.RecipeState;
import hayo.features.processing_machine.UpgradableMachineBlockEntity;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SolidFluidReactorBlockEntity extends UpgradableMachineBlockEntity<BaseSolidFluidReactingRecipe, ItemFluidRecipeInput> implements WorldlyContainer {
    public static final int SLOTS = 13;

    public static final int INPUT = 0, INPUT_TANK_INPUT = 1, OUTPUT_TANK_INPUT = 2;
    public static final int BATTERY = 3;
    public static final int OUTPUT_1 = 4, OUTPUT_2 = 5, OUTPUT_3 = 6, INPUT_TANK_OUTPUT = 7, OUTPUT_TANK_OUTPUT = 8;
    public static final int UPGRADE_1 = 9;
    public static final int UPGRADES = 4;

    public static final int FLUID_CAPACITY = 4000;

    public static final int REACTING_ENERGY_RATE = 2;

    @Getter
    protected SolidFluidReactorMode mode = SolidFluidReactorMode.REACTING;

    @Getter
    protected FluidStack inputFluid = FluidStack.EMPTY;
    @Getter
    protected FluidStack resultFluid = FluidStack.EMPTY;

    protected final RecipeState<FluidDrainingRecipe, SingleRecipeInput> inputDraining = new RecipeState<>();
    protected final RecipeState<FluidFillingRecipe, ItemFluidRecipeInput> resultFilling = new RecipeState<>();

    public SolidFluidReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.SOLID_FLUID_REACTOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SolidFluidReactorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.resetEnergyPerTick();

        entity.tickRecipe((ServerLevel) level, pos, state);
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
    protected int getEnergyTransferLimit() {
        return 32;
    }

    @Override
    protected int getBaseEnergyCapacity() {
        return 800;
    }

    @Override
    protected int getBaseEnergyUseRate() {
        return REACTING_ENERGY_RATE;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RecipeType<BaseSolidFluidReactingRecipe> getRecipeType() {
        return (RecipeType<BaseSolidFluidReactingRecipe>) this.mode.recipeType;
    }

    @Override
    protected boolean isInputSlot(int slot) {
        return slot == INPUT;
    }

    @Override
    protected ItemFluidRecipeInput createRecipeInput() {
        return new ItemFluidRecipeInput(this.getItem(INPUT), this.inputFluid);
    }

    @Override
    protected int getBaseEnergyCost(@Nullable BaseSolidFluidReactingRecipe recipe) {
        return recipe != null ? recipe.energyCost : 1000;
    }

    @Override
    protected IntRange getUpgradeSlots() {
        return new IntRange(UPGRADE_1, UPGRADE_1 + UPGRADES);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("fluid_ingredient", FluidStack.CODEC, this.inputFluid);
        output.store("fluid_result", FluidStack.CODEC, this.resultFluid);
        output.putInt("input_filling_progress", this.inputDraining.progress);
        output.putInt("result_draining_progress", this.resultFilling.progress);
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inputFluid = input.read("fluid_ingredient", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        this.resultFluid = input.read("fluid_result", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        this.inputDraining.progress = input.getIntOr("input_filling_progress", 0);
        this.resultFilling.progress = input.getIntOr("result_draining_progress", 0);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            case UP -> new int[]{INPUT};
            case DOWN -> new int[]{OUTPUT_1, OUTPUT_2, OUTPUT_3, INPUT_TANK_OUTPUT, OUTPUT_TANK_OUTPUT};
            case NORTH, WEST, SOUTH, EAST -> new int[]{BATTERY, INPUT_TANK_INPUT, OUTPUT_TANK_INPUT};
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

    @Override
    public boolean canCraft(RecipeHolder<BaseSolidFluidReactingRecipe> holder, ItemFluidRecipeInput input) {
        var recipe = holder.value();
        if (!HayoContainerUtils.canAlwaysInsertAllShapelessly(this.stacks, recipe.resultItems, OUTPUT_1, OUTPUT_3, this.getMaxStackSize())) {
            return false;
        }

        var resultFluid = recipe.resultFluid;
        if (!resultFluid.isEmpty()) {
            if (this.resultFluid.fluid() != Fluids.EMPTY && this.resultFluid.fluid() != resultFluid.fluid()) {
                return false;
            }
            return FLUID_CAPACITY - this.resultFluid.amount() >= resultFluid.amount();
        }

        return true;
    }

    @Override
    public void craft(RecipeHolder<BaseSolidFluidReactingRecipe> holder, ItemFluidRecipeInput input) {
        var recipe = holder.value();

        this.stacks.get(INPUT).shrink(1);
        if (recipe.inputFluid.amount() > 0) {
            this.inputFluid = new FluidStack(this.inputFluid.holder(), this.inputFluid.amount() - recipe.inputFluid.amount());
        }

        for (int i = 0; i < recipe.resultItems.size(); i++) {
            float chance = recipe.resultItems.get(i).chance();
            if (chance < 1 && this.level.getRandom().nextFloat() > chance) {
                continue;
            }

            var stack = recipe.resultItems.get(i).template().create();
            HayoContainerUtils.insertShapelessly(this.stacks, stack, OUTPUT_1, OUTPUT_3, this.getMaxStackSize());
        }

        var resultFluid = recipe.resultFluid;
        if (!resultFluid.isEmpty()) {
            this.resultFluid = new FluidStack(resultFluid.holder(), this.resultFluid.amount() + resultFluid.amount());
        }
    }

    @Override
    protected void updateUpgradeState() {
        super.updateUpgradeState();

        var mode = SolidFluidReactorMode.REACTING;
        for (int i = UPGRADE_1; i < UPGRADE_1 + UPGRADES; i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.ORE_WASHING_UPGRADE)) {
                mode = SolidFluidReactorMode.ORE_WASHING;
                break;
            } else if (stack.is(Hayo.Items.NUTRIENT_DISPENSER_UPGRADE)) {
                mode = SolidFluidReactorMode.NUTRIENT_PURIFYING;
                break;
            } else if (stack.is(Hayo.Items.CHEMFUEL_PROCESSING_UPGRADE)) {
                mode = SolidFluidReactorMode.CHEMFUEL_PROCESSING;
                break;
            }
        }
        if (mode != this.mode) {
            this.mode = mode;
            this.recipeState.progress = 0;
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
        public boolean canCraft(RecipeHolder<FluidDrainingRecipe> holder, SingleRecipeInput input) {
            if (!this.entity.canInsertToSlot(holder.value().assemble(input), INPUT_TANK_OUTPUT)) {
                return false;
            }
            if (this.entity.inputFluid.fluid() == Fluids.EMPTY) {
                return true;
            }
            if (this.entity.inputFluid.holder() == holder.value().resultFluid().holder()) {
                return FLUID_CAPACITY - this.entity.inputFluid.amount() >= holder.value().resultFluid().amount();
            }
            return false;
        }

        @Override
        public int getRecipeCost(FluidDrainingRecipe holder) {
            return holder.energyCost();
        }

        @Override
        public void craft(RecipeHolder<FluidDrainingRecipe> holder, SingleRecipeInput input) {
            var recipe = holder.value();

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
        public boolean canCraft(RecipeHolder<FluidFillingRecipe> holder, ItemFluidRecipeInput input) {
            return this.entity.canInsertToSlot(holder.value().assemble(input), OUTPUT_TANK_OUTPUT);
        }

        @Override
        public int getRecipeCost(FluidFillingRecipe holder) {
            return holder.energyCost();
        }

        @Override
        public void craft(RecipeHolder<FluidFillingRecipe> holder, ItemFluidRecipeInput input) {
            this.entity.stacks.get(OUTPUT_TANK_INPUT).shrink(1);
            this.entity.resultFluid = new FluidStack(this.entity.resultFluid.holder(), this.entity.resultFluid.amount() - holder.value().inputFluid().amount());

            var recipeOutput = holder.value().assemble(input);
            var existingStack = this.entity.stacks.get(OUTPUT_TANK_OUTPUT);
            if (!existingStack.isEmpty()) {
                existingStack.grow(recipeOutput.getCount());
            } else {
                this.entity.stacks.set(OUTPUT_TANK_OUTPUT, recipeOutput);
            }
        }
    }
}
