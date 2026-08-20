package hayo.processing_machine.classic;

import hayo.Hayo;
import hayo.old_menus.RecipeProgressBar;
import hayo.old_menus.UniversalContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MaceratorBlockEntity extends ClassicMachineBlockEntity<MaceratingRecipe> {
    public static final int TRANSFER_LIMIT = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 10 * 20 * ENERGY_USE_RATE; // 10s * 20tick/s * 2e/tick = 400e

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MACERATOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MaceratorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.tickRecipe((ServerLevel) level, pos, state);
        entity.resetEnergyPerTick();
    }

    @Override
    protected RecipeType<MaceratingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.MACERATING;
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
    public int getBaseEnergyCost(RecipeHolder<MaceratingRecipe> holder) {
        return holder == null ? MaceratingRecipe.DEFAULT_ENERGY : holder.value().energyCost;
    }

    @Override
    protected TagKey<Item> getUpgradeTag() {
        return Hayo.ItemTags.MACERATOR_UPGRADES;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.macerator");
    }

    @Override
    public UniversalContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return super.createMenu(containerId, inventory, player) //
                .addElement(RecipeProgressBar.macerator(70, 36, this::getRecipeProgress, this::getLastOrDefaultRecipeCost));
    }
}
