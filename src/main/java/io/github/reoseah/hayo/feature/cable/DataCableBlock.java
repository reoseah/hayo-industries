package io.github.reoseah.hayo.feature.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DataCableBlock extends Block {
    protected final VoxelShape[] shapes;

    public DataCableBlock(int radius, Properties properties) {
        super(properties);

        this.shapes = CableShapes.getOrCreate(radius);

        this.registerDefaultState(this.defaultBlockState() //
                .setValue(CableShapes.DOWN, false) //
                .setValue(CableShapes.UP, false) //
                .setValue(CableShapes.NORTH, false) //
                .setValue(CableShapes.SOUTH, false) //
                .setValue(CableShapes.EAST, false) //
                .setValue(CableShapes.WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CableShapes.DOWN, CableShapes.UP, CableShapes.NORTH, CableShapes.SOUTH, CableShapes.EAST, CableShapes.WEST);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes[CableShapes.getIndex(state)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.getStateForPos(ctx.getLevel(), ctx.getClickedPos());
    }

    public BlockState getStateForPos(Level level, BlockPos pos) {
        return this.defaultBlockState() //
                .setValue(CableShapes.DOWN, this.connectsTo(level, pos, Direction.DOWN)) //
                .setValue(CableShapes.UP, this.connectsTo(level, pos, Direction.UP)) //
                .setValue(CableShapes.WEST, this.connectsTo(level, pos, Direction.WEST)) //
                .setValue(CableShapes.EAST, this.connectsTo(level, pos, Direction.EAST)) //
                .setValue(CableShapes.NORTH, this.connectsTo(level, pos, Direction.NORTH)) //
                .setValue(CableShapes.SOUTH, this.connectsTo(level, pos, Direction.SOUTH));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return state.setValue(CableShapes.getSideProperty(direction), this.connectsTo(level, pos, direction));
    }

    protected boolean connectsTo(LevelReader level, BlockPos pos, Direction side) {
        var neighborState = level.getBlockState(pos.relative(side));
        var block = neighborState.getBlock();
        if (block instanceof DataCableBlock dataCable) {
            return true; // TODO
        }
        return false;
    }
}
