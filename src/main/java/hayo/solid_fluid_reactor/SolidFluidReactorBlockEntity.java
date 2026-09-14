package hayo.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.blockentity.EnergyReceiverBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SolidFluidReactorBlockEntity extends EnergyReceiverBlockEntity {
    public static final int SLOTS = 13;
    public static final int INPUT = 0;
    public static final int DRAIN_INPUT = 1;
    public static final int FILL_INPUT = 2;

    public static final int BATTERY = 3;

    public static final int OUTPUT_1 = 4;
    public static final int OUTPUT_2 = 5;
    public static final int OUTPUT_3 = 6;
    public static final int[] OUTPUT_SLOTS = {OUTPUT_1, OUTPUT_2, OUTPUT_3};
    public static final int DRAIN_OUTPUT = 7;
    public static final int FILL_OUTPUT = 8;

    public static final int UPGRADE_1 = 9;
    public static final int UPGRADES = 4;

    public static final int FLUID_CAPACITY = 4000;

    public static final int REACTING_ENERGY_RATE = 2;

    @Getter
    protected FluidStack inputFluid = FluidStack.EMPTY;
    @Getter
    protected FluidStack resultFluid = FluidStack.EMPTY;

    protected final RecipeHandler<DrainingRecipe, SingleRecipeInput> draining = new RecipeHandler<>();
    protected final RecipeHandler<SolidFluidReactingRecipe, ItemFluidPairRecipeInput> reacting = new RecipeHandler<>();

    public SolidFluidReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.SOLID_FLUID_REACTOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SolidFluidReactorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.storedEnergy -= entity.reacting.tick(
                Hayo.RecipeTypes.SOLID_FLUID_REACTING,
                new ItemFluidPairRecipeInput(entity.getItem(INPUT), entity.inputFluid),
                (ServerLevel) level,
                entity.storedEnergy >= REACTING_ENERGY_RATE
                        ? new RecipeHandler.RecipeResourceState.Sufficient(REACTING_ENERGY_RATE)
                        : new RecipeHandler.RecipeResourceState.NotSufficient(-2 * REACTING_ENERGY_RATE),
                new RecipeHandler.Context<>() {
                    @Override
                    public void setChanged() {
                        entity.setChanged();
                    }

                    @Override
                    public boolean canCraft(RecipeHolder<SolidFluidReactingRecipe> recipe, ItemFluidPairRecipeInput input) {
                        if (!entity.canInsertResults(recipe.value().resultItems())) {
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
                    public void craft(RecipeHolder<SolidFluidReactingRecipe> recipe, ItemFluidPairRecipeInput input) {
                        entity.stacks.get(INPUT).shrink(1);
                        if (recipe.value().inputFluid().amount() > 0) {
                            int remaining = entity.inputFluid.amount() - recipe.value().inputFluid().amount();
                            entity.inputFluid = remaining > 0
                                    ? new FluidStack(entity.inputFluid.holder(), remaining)
                                    : FluidStack.EMPTY;
                        }

                        for (var template : recipe.value().resultItems()) {
                            var resultStack = template.create();
                            entity.insertIntoOutputSlots(entity.stacks, resultStack);
                        }

                        var resultFluid = recipe.value().resultFluid();
                        if (!resultFluid.isEmpty()) {
                            entity.resultFluid = new FluidStack(
                                    resultFluid.holder(),
                                    entity.resultFluid.amount() + resultFluid.amount()
                            );
                        }

                        entity.setChanged();
                    }
                }
        );
        entity.storedEnergy -= entity.draining.tick(
                Hayo.RecipeTypes.DRAINING,
                new SingleRecipeInput(entity.getItem(DRAIN_INPUT)),
                (ServerLevel) level,
                entity.storedEnergy >= 1
                        ? new RecipeHandler.RecipeResourceState.Sufficient(1)
                        : new RecipeHandler.RecipeResourceState.NotSufficient(-2),
                new RecipeHandler.Context<>() {
                    @Override
                    public void setChanged() {
                        entity.setChanged();
                    }

                    @Override
                    public boolean canCraft(RecipeHolder<DrainingRecipe> recipe, SingleRecipeInput input) {
                        if (!entity.canInsertToSlot(recipe.value().assemble(input), DRAIN_OUTPUT)) {
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
                    public int getRecipeCost(DrainingRecipe recipe) {
                        return recipe.energyCost();
                    }

                    @Override
                    public void craft(RecipeHolder<DrainingRecipe> recipe, SingleRecipeInput input) {
                        entity.stacks.get(DRAIN_INPUT).shrink(1);

                        var recipeOutput = recipe.value().assemble(input);
                        var outputStack = entity.stacks.get(DRAIN_OUTPUT);
                        if (outputStack.isEmpty()) {
                            entity.stacks.set(DRAIN_OUTPUT, recipeOutput);
                        } else {
                            outputStack.grow(recipeOutput.getCount());
                        }

                        entity.inputFluid = new FluidStack(recipe.value().resultFluid().holder(), entity.inputFluid.amount() + recipe.value().resultFluid().amount());
                        entity.setChanged();
                    }
                }
        );
        entity.resetEnergyPerTick();
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
        output.putInt("draining_progress", this.draining.progress);
        output.putInt("reacting_progress", this.reacting.progress);
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inputFluid = input.read("input_fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        this.resultFluid = input.read("result_fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        this.draining.progress = input.getIntOr("draining_progress", 0);
        this.reacting.progress = input.getIntOr("reacting_progress", 0);
    }

    private boolean canInsertResults(List<ItemStackTemplate> resultTemplates) {
        if (resultTemplates.isEmpty()) {
            return true;
        }

        var simulated = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int slot : OUTPUT_SLOTS) {
            simulated.set(slot, this.stacks.get(slot).copy());
        }

        for (var template : resultTemplates) {
            var resultStack = template.create();
            if (!this.insertIntoOutputSlots(simulated, resultStack)) {
                return false;
            }
        }

        return true;
    }

    private boolean insertIntoOutputSlots(NonNullList<ItemStack> inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        for (int slot : OUTPUT_SLOTS) {
            var current = inventory.get(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack)) {
                int maxCount = Math.min(stack.getMaxStackSize(), this.getMaxStackSize(stack));
                int available = maxCount - current.getCount();
                if (available > 0) {
                    int toAdd = Math.min(available, stack.getCount());
                    current.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        for (int slot : OUTPUT_SLOTS) {
            var current = inventory.get(slot);
            if (current.isEmpty()) {
                int maxCount = Math.min(stack.getMaxStackSize(), this.getMaxStackSize(stack));
                int toAdd = Math.min(maxCount, stack.getCount());
                inventory.set(slot, stack.copyWithCount(toAdd));
                stack.shrink(toAdd);
                if (stack.isEmpty()) {
                    return true;
                }
            }
        }

        return stack.isEmpty();
    }
}
