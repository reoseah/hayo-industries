package hayo.cable;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum CableShapeCache {
    ;

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private static final IntObjectMap<VoxelShape[]> SHAPES_BY_RADIUS = new IntObjectHashMap<>();

    public static VoxelShape[] getOrCreate(int radius) {
        return SHAPES_BY_RADIUS.computeIfAbsent(radius, (r) -> {
            var shapes = new VoxelShape[64];

            float min = 8 - r;
            float max = 8 + r;
            var center = Block.box(min, min, min, max, max, max);
            var connections = new VoxelShape[]{
                    Block.box(min, 0, min, max, max, max),
                    Block.box(min, min, min, max, 16, max),
                    Block.box(min, min, 0, max, max, max),
                    Block.box(min, min, min, max, max, 16),
                    Block.box(0, min, min, max, max, max),
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

    public static BooleanProperty getSideProperty(Direction side) {
        return switch (side) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            case DOWN -> DOWN;
            case UP -> UP;
        };
    }

    public static int getIndex(BlockState state) {
        return (state.getValue(DOWN) ? 1 : 0)
                | (state.getValue(UP) ? 2 : 0)
                | (state.getValue(NORTH) ? 4 : 0)
                | (state.getValue(SOUTH) ? 8 : 0)
                | (state.getValue(WEST) ? 16 : 0)
                | (state.getValue(EAST) ? 32 : 0);
    }
}
