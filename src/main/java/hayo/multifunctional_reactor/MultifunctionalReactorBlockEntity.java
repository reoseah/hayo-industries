package hayo.multifunctional_reactor;

import hayo.Hayo;
import hayo.common.blockentity.SimpleContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MultifunctionalReactorBlockEntity extends SimpleContainerBlockEntity {
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
    public static final int FIRST_UPGRADE = 9;
    public static final int UPGRADES = 4;

    public MultifunctionalReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MULTIFUNCTIONAL_REACTOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.multifunctional_reactor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MultifunctionalReactorMenu(containerId, this, inventory);
    }
}
