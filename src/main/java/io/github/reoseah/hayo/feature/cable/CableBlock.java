package io.github.reoseah.hayo.feature.cable;

import io.github.reoseah.hayo.api.energy.ElectricBlock;
import io.github.reoseah.hayo.api.energy.ElectricBlocks;
import io.github.reoseah.hayo.api.energy.ElectricCableBlock;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CableBlock extends Block implements ElectricCableBlock {
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public static BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            case DOWN -> DOWN;
            case UP -> UP;
        };
    }

    protected static final IntObjectMap<VoxelShape[]> SHAPE_CACHE = new IntObjectHashMap<>();

    public static VoxelShape[] getOrCreateShapes(int radius) {
        return SHAPE_CACHE.computeIfAbsent(radius, (rad) -> {
            var shapes = new VoxelShape[64];
            float min = 8 - rad;
            float max = 8 + rad;
            var center = Block.box(min, min, min, max, max, max);
            VoxelShape[] connections = { //
                    Block.box(min, 0, min, max, max, max), //
                    Block.box(min, min, min, max, 16, max), //
                    Block.box(min, min, 0, max, max, max), //
                    Block.box(min, min, min, max, max, 16), //
                    Block.box(0, min, min, max, max, max), //
                    Block.box(min, min, min, 16, max, max)};

            for (int i = 0; i < 64; i++) {
                var shape = center;
                for (int face = 0; face < 6; face++) {
                    if ((i & 1 << face) != 0) {
                        shape = Shapes.or(shape, connections[face]);
                    }
                }
                shapes[i] = shape;
            }
            return shapes;
        });
    }

    public final int transferLimit;
    public final VoxelShape[] shapes;

    public CableBlock(int transferLimit, int radius, Properties settings) {
        super(settings);
        this.transferLimit = transferLimit;
        this.shapes = getOrCreateShapes(radius);

        this.registerDefaultState(this.defaultBlockState() //
                .setValue(DOWN, false) //
                .setValue(UP, false) //
                .setValue(NORTH, false) //
                .setValue(SOUTH, false) //
                .setValue(EAST, false) //
                .setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.getStateForPos(ctx.getLevel(), ctx.getClickedPos());
    }

    public BlockState getStateForPos(Level level, BlockPos pos) {
        return this.defaultBlockState() //
                .setValue(DOWN, this.connectsTo(level, pos, Direction.DOWN)) //
                .setValue(UP, this.connectsTo(level, pos, Direction.UP)) //
                .setValue(WEST, this.connectsTo(level, pos, Direction.WEST)) //
                .setValue(EAST, this.connectsTo(level, pos, Direction.EAST)) //
                .setValue(NORTH, this.connectsTo(level, pos, Direction.NORTH)) //
                .setValue(SOUTH, this.connectsTo(level, pos, Direction.SOUTH));
    }


    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        // TODO update cable paths
        return state.setValue(getConnectionProperty(direction), this.connectsTo(level, pos, direction));
    }

    protected boolean connectsTo(LevelReader level, BlockPos pos, Direction side) {
        var neighborState = level.getBlockState(pos.relative(side));
        var block = neighborState.getBlock();
        if (block instanceof ElectricBlock electricBlock) {
            return electricBlock.connectsToCables(neighborState, level, pos.relative(side), side);
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int idx = (state.getValue(DOWN) ? 1 : 0) //
                | (state.getValue(UP) ? 2 : 0) //
                | (state.getValue(NORTH) ? 4 : 0) //
                | (state.getValue(SOUTH) ? 8 : 0) //
                | (state.getValue(WEST) ? 16 : 0) //
                | (state.getValue(EAST) ? 32 : 0);
        return this.shapes[idx];
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            ElectricBlocks.remove(serverLevel, pos);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel) {
            ElectricBlocks.remove(serverLevel, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            ElectricBlocks.addOrUpdate(serverLevel, pos);
        }
    }
}
