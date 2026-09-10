package hayo.multifunctional_reactor;

import hayo.Hayo;
import hayo.common.blockentity.EnergyReceiverBlockEntity;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class MultifunctionalReactorBlockEntity extends EnergyReceiverBlockEntity {
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

    protected final RecipeHandler<DrainingRecipe, SingleRecipeInput> draining = new RecipeHandler<>();

    public MultifunctionalReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MULTIFUNCTIONAL_REACTOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MultifunctionalReactorBlockEntity entity) {
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
                        return entity.canInsertToSlot(recipe.value().assemble(input), DRAIN_OUTPUT);
                    }

                    @Override
                    public int getRecipeCost(DrainingRecipe recipe) {
                        return recipe.energyCost;
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
                    }
                }
        );
        entity.resetEnergyPerTick();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.multifunctional_reactor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MultifunctionalReactorMenu(containerId, this, inventory);
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
        this.draining.save(output.child("draining"));
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.draining.load(input.childOrEmpty("draining"));
    }

}
