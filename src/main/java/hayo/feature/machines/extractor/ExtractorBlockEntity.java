package hayo.feature.machines.extractor;

import hayo.Hayo;
import hayo.feature.machines.ClassicMachineBlockEntity;
import hayo.feature.universal_screen.RecipeProgressBar;
import hayo.menu.UniversalContainerMenu;
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

public class ExtractorBlockEntity extends ClassicMachineBlockEntity<ExtractingRecipe> {
    public static final int TRANSFER_LIMIT = 32;
    public static final int ENERGY_USE_RATE = 2;
    public static final int CAPACITY = 15 * 20 * ENERGY_USE_RATE; // 15s * 20tick/s * 2e/tick = 600e

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.EXTRACTOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, ExtractorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        entity.tickRecipe((ServerLevel) level, pos, state);
        entity.resetEnergyPerTick();
    }

    @Override
    protected RecipeType<ExtractingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.EXTRACTING;
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
    public int getBaseEnergyCost(RecipeHolder<ExtractingRecipe> holder) {
        return holder == null ? ExtractingRecipe.DEFAULT_ENERGY : holder.value().energyCost;
    }

    @Override
    protected TagKey<Item> getUpgradeTag() {
        return Hayo.ItemTags.EXTRACTOR_UPGRADES;
    }
    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.extractor");
    }

    @Override
    public UniversalContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return super.createMenu(containerId, inventory, player) //
                .addElement(RecipeProgressBar.extractor(70, 36, this::getProgressEnergy, this::getLastOrDefaultRecipeEnergy));
    }
}
