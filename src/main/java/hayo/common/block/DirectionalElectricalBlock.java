package hayo.common.block;

import hayo.common.blockentity.EnergyReceiverBlockEntity;
import hayo.energy.block.BaseEnergyBlock;
import hayo.energy.block.EnergyGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public abstract class DirectionalElectricalBlock extends BaseEntityBlock implements BaseEnergyBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    protected DirectionalElectricalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            var factory = state.getMenuProvider(level, pos);
            if (factory == null) {
                return InteractionResult.PASS;
            }

            player.openMenu(factory);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.remove(serverLevel, pos);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.remove(serverLevel, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getBlock() != oldState.getBlock() && level instanceof ServerLevel serverLevel) {
            EnergyGrid.addOrUpdate(serverLevel, pos);
        }
    }

    public int getReceivableEnergy(ServerLevel level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof EnergyReceiverBlockEntity entity) {
            return entity.getReceivableEnergy();
        }
        return 0;
    }

    public int receiveEnergy(int amount, ServerLevel level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof EnergyReceiverBlockEntity entity) {
            return entity.receiveEnergy(amount);
        }
        return 0;
    }
}
