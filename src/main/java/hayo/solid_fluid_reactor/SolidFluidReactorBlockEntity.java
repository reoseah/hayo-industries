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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SolidFluidReactorBlockEntity extends EnergyReceiverBlockEntity {
    public static final int SLOTS = 13;
    public static final int INPUT = 0;
    public static final int DRAIN_INPUT = 1;
    public static final int FILL_INPUT = 2;

    public static final int BATTERY = 3;

    public static final int OUTPUT_1 = 4;
    public static final int OUTPUT_2 = 5;
    public static final int OUTPUT_3 = 6;
    public static final int DRAIN_OUTPUT = 7;
    public static final int FILL_OUTPUT = 8;

    public static final int UPGRADE_1 = 9;
    public static final int UPGRADES = 4;

    public static final int FLUID_CAPACITY = 4000;

    @Getter
    protected FluidStack inputFluid = FluidStack.EMPTY;

    protected final RecipeHandler<DrainingRecipe, SingleRecipeInput> draining = new RecipeHandler<>();

    public SolidFluidReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.SOLID_FLUID_REACTOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SolidFluidReactorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.storedEnergy -= entity.draining.tick(
                Hayo.RecipeTypes.DRAINING,
                new SingleRecipeInput(entity.getItem(DRAIN_INPUT)),
                (ServerLevel) level,
                entity.storedEnergy >= 1 ? new RecipeHandler.RecipeResourceState.Sufficient(1) : new RecipeHandler.RecipeResourceState.Sufficient(-2),
                new RecipeHandler.Context<DrainingRecipe, SingleRecipeInput>() {
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
                        if (entity.inputFluid.holder() == recipe.value().fluid().holder()) {
                            return FLUID_CAPACITY - entity.inputFluid.amount() >= recipe.value().fluid().amount();
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

                        entity.inputFluid = new FluidStack(recipe.value().fluid().holder(), entity.inputFluid.amount() + recipe.value().fluid().amount());
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
        output.store("input_fluid", FluidStack.MAP_CODEC.codec(), this.inputFluid);
        output.putInt("draining_progress", this.draining.progress);
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inputFluid = input.read("input_fluid", FluidStack.MAP_CODEC.codec()).orElse(FluidStack.EMPTY);
        this.draining.progress = input.getIntOr("draining_progress", 0);
    }
}
