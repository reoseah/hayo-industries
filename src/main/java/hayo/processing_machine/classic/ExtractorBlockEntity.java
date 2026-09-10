package hayo.processing_machine.classic;

import hayo.Hayo;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ExtractorBlockEntity extends ClassicMachineBlockEntity<ClassicMachineRecipe> {
    public static final int TRANSFER_LIMIT = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 15 * 20 * ENERGY_USE_RATE; // 15s * 20tick/s * 2e/tick = 600e

    @Getter
    protected ExtractorMode mode = ExtractorMode.DEFAULT;

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.EXTRACTOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ExtractorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.tickRecipe((ServerLevel) level, pos, state);
        entity.resetEnergyPerTick();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RecipeType<ClassicMachineRecipe> getRecipeType() {
        return (RecipeType<ClassicMachineRecipe>) this.mode.recipeType;
    }

    @Override
    public int getBaseEnergyUseRate() {
        return ENERGY_USE_RATE;
    }

    @Override
    public int getBaseEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return TRANSFER_LIMIT;
    }

    @Override
    protected void updateUpgradeState() {
        super.updateUpgradeState();

        var mode = ExtractorMode.DEFAULT;
        for (int i = UPGRADE_1; i < UPGRADE_1 + UPGRADES; i++) {
            var stack = this.stacks.get(i);
            if (stack.is(Hayo.Items.NUTRIENT_DISPENSER_UPGRADE)) {
                mode = ExtractorMode.NUTRIENT_EXTRACTING;
                break;

            }
        }
        if (mode != this.mode) {
            this.mode = mode;
            this.recipeProgress = 0;
        }
    }

    @Override
    public int getBaseEnergyCost(RecipeHolder<ClassicMachineRecipe> holder) {
        return holder == null ? ExtractingRecipe.DEFAULT_ENERGY : holder.value().energyCost;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.extractor");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ExtractorMenu(containerId, this, inventory);
    }
}
