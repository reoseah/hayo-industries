package hayo.cable;

import hayo.energy.block.EnergyGrid;
import hayo.energy.block.EnergyHandler;
import hayo.energy.block.EnergyTransferrer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class CableBlock extends Block implements EnergyTransferrer {
    public final int transferLimit;
    protected final VoxelShape[] shapes;

    public CableBlock(int transferLimit, int radius, Properties settings) {
        super(settings);
        this.transferLimit = transferLimit;
        this.shapes = CableShapeCache.getOrCreate(radius);

        this.registerDefaultState(this.defaultBlockState()
                .setValue(CableShapeCache.DOWN, false)
                .setValue(CableShapeCache.UP, false)
                .setValue(CableShapeCache.NORTH, false)
                .setValue(CableShapeCache.SOUTH, false)
                .setValue(CableShapeCache.EAST, false)
                .setValue(CableShapeCache.WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CableShapeCache.DOWN, CableShapeCache.UP, CableShapeCache.NORTH, CableShapeCache.SOUTH, CableShapeCache.EAST, CableShapeCache.WEST);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes[CableShapeCache.getIndex(state)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.getStateForPos(ctx.getLevel(), ctx.getClickedPos());
    }

    public BlockState getStateForPos(Level level, BlockPos pos) {
        return this.defaultBlockState()
                .setValue(CableShapeCache.DOWN, this.connectsTo(level, pos, Direction.DOWN))
                .setValue(CableShapeCache.UP, this.connectsTo(level, pos, Direction.UP))
                .setValue(CableShapeCache.WEST, this.connectsTo(level, pos, Direction.WEST))
                .setValue(CableShapeCache.EAST, this.connectsTo(level, pos, Direction.EAST))
                .setValue(CableShapeCache.NORTH, this.connectsTo(level, pos, Direction.NORTH))
                .setValue(CableShapeCache.SOUTH, this.connectsTo(level, pos, Direction.SOUTH));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return state.setValue(CableShapeCache.getSideProperty(direction), this.connectsTo(level, pos, direction));
    }

    protected boolean connectsTo(LevelReader level, BlockPos pos, Direction side) {
        var neighborState = level.getBlockState(pos.relative(side));
        var block = neighborState.getBlock();
        if (block instanceof EnergyHandler electricBlock) {
            BlockPos pos1 = pos.relative(side);
            return electricBlock.connectsToCables(neighborState, level, pos1, side.getOpposite());
        }
        return false;
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
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.addOrUpdate(serverLevel, pos);
        }
    }

    @Override
    public int getTransferLimit(BlockState state) {
        return this.transferLimit;
    }
}
