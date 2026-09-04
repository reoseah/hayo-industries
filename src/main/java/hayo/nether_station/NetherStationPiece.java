package hayo.nether_station;

import hayo.Hayo;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.List;

public class NetherStationPiece extends StructurePiece {
    public static final char[][][] LAYERS = {{
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
            {' ', ' ', '#', '#', '#', '#', '#', '#', '#', ' ', ' '},
            {' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' '},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' '},
            {' ', ' ', '#', '#', '#', '#', '#', '#', '#', ' ', ' '},
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
    }, {
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
            {' ', ' ', '#', '.', '#', '.', '#', '.', '#', ' ', ' '},
            {' ', '#', '.', '.', '.', '.', '.', '.', '.', '#', ' '},
            {'#', '.', '.', '.', '#', '.', '#', '.', '.', '.', '#'},
            {'#', '#', '#', '#', '#', '.', '#', '#', '#', '#', '#'},
            {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
            {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
            {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
            {' ', '#', '.', '.', '#', 'D', '#', '.', '.', '#', ' '},
            {' ', ' ', '#', '.', 'X', '.', 'X', '.', '#', ' ', ' '},
            {' ', ' ', ' ', '#', '#', 'R', '#', '#', ' ', ' ', ' '},
    }, {
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
            {' ', ' ', '#', '.', '#', '.', '#', '.', '#', ' ', ' '},
            {' ', '#', '.', '.', '.', '.', '.', '.', '.', '#', ' '},
            {'#', '.', '.', '.', '#', '.', '#', '.', '.', '.', '#'},
            {'#', '#', '#', '#', '#', '.', '#', '#', '#', '#', '#'},
            {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
            {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
            {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
            {' ', '#', '.', '.', '#', 'd', '#', '.', '.', '#', ' '},
            {' ', ' ', '#', '.', 'X', '.', 'G', '.', '#', ' ', ' '},
            {' ', ' ', ' ', '#', '#', 'r', '#', '#', ' ', ' ', ' '},
    }, {
            {' ', ' ', ' ', '#', 'X', 'G', 'X', '#', ' ', ' ', ' '},
            {' ', ' ', 'X', '.', '#', '.', '#', '.', 'X', ' ', ' '},
            {' ', 'X', '.', '.', '#', '.', '#', '.', '.', 'X', ' '},
            {'#', '.', '.', '.', '#', '.', '#', '.', '.', '.', '#'},
            {'X', '#', 'X', 'X', '#', '#', '#', 'X', 'X', '#', 'X'},
            {'G', '.', '.', '.', '.', '.', '.', '.', '.', '.', 'G'},
            {'X', '.', '.', '.', '.', '.', '.', '.', '.', '.', 'X'},
            {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
            {' ', 'X', '.', '.', '#', '#', '#', '.', '.', 'X', ' '},
            {' ', ' ', 'X', '.', '#', '.', '#', '.', 'X', ' ', ' '},
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
    }, {
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
            {' ', ' ', '#', 'T', '#', 'T', '#', 'T', '#', ' ', ' '},
            {' ', '#', 'T', '.', '#', '.', '#', '.', 'T', '#', ' '},
            {'#', 'T', '.', '.', '#', '.', '#', '.', '.', 'T', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', 'T', '.', '.', '#', '.', '#', '.', '.', 'T', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', 'T', '.', '.', '#', '.', '#', '.', '.', 'T', '#'},
            {' ', '#', 'T', '.', '#', '#', '#', '.', 'T', '#', ' '},
            {' ', ' ', '#', 'T', '#', 'X', '#', 'T', '#', ' ', ' '},
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
    }, {
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
            {' ', ' ', '#', '#', '#', '#', '#', '#', '#', ' ', ' '},
            {' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' '},
            {' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' '},
            {' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' '},
            {' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' '},
            {' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' '},
            {' ', ' ', '#', '#', '#', '#', '#', '#', '#', ' ', ' '},
            {' ', ' ', ' ', '#', '#', '#', '#', '#', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
    }};

    public static final Char2ObjectMap<BlockState> MAP = Char2ObjectMap.ofEntries(
            Char2ObjectMap.entry('.', Blocks.CAVE_AIR.defaultBlockState()),
            Char2ObjectMap.entry('#', Hayo.Blocks.REINFORCED_STONE.defaultBlockState()),
            Char2ObjectMap.entry('X', Hayo.Blocks.REINFORCED_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE)),
            Char2ObjectMap.entry('T', Hayo.Blocks.REINFORCED_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)),
            Char2ObjectMap.entry('G', Hayo.Blocks.REINFORCED_GLASS.defaultBlockState()),
            Char2ObjectMap.entry('D', Hayo.Blocks.REINFORCED_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)),
            Char2ObjectMap.entry('d', Hayo.Blocks.REINFORCED_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)),
            Char2ObjectMap.entry('R', Hayo.Blocks.REINFORCED_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING, Direction.SOUTH)),
            Char2ObjectMap.entry('r', Hayo.Blocks.REINFORCED_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING, Direction.SOUTH))
    );

    public static final List<Pair<Vec3i, Direction>> CHEST_POSITIONS = List.of(
            Pair.of(new Vec3i(1, 1, 6), Direction.EAST),
            Pair.of(new Vec3i(9, 1, 6), Direction.WEST)
    );

    public NetherStationPiece(CompoundTag tag) {
        super(Hayo.StructurePieceTypes.NETHER_STATION, tag);
    }

    public NetherStationPiece(BlockPos pos, Direction direction) {
        var bounds = StructurePiece.makeBoundingBox(pos.getX(), pos.getY(), pos.getZ(), direction, LAYERS[0][0].length, LAYERS.length, LAYERS[0].length);
        super(Hayo.StructurePieceTypes.NETHER_STATION, 0, bounds);
        this.setOrientation(direction);
    }

    public static void addPiece(StructurePiecesBuilder builder, RandomSource random, BlockPos position) {
        var direction = Direction.from2DDataValue(random.nextInt(4));
        builder.addPiece(new NetherStationPiece(position, direction));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        for (int y = 0; y < LAYERS.length; y++) {
            for (int z = 0; z < LAYERS[0].length; z++) {
                for (int x = 0; x < LAYERS[0][0].length; x++) {
                    char ch = LAYERS[y][z][x];
                    if (ch == ' ') {
                        continue;
                    }
                    if (!MAP.containsKey(ch)) {
                        continue;
                    }
                    this.placeBlock(level, MAP.get(ch), x, y - 1, z, chunkBB);
                }
            }
        }

        var positionalRandom = RandomSource.createThreadLocalInstance(level.getSeed()).forkPositional().at(this.getBoundingBox().getCenter());
        var chestEntry = CHEST_POSITIONS.get(positionalRandom.nextInt(CHEST_POSITIONS.size()));

        var chestPos = chestEntry.first();
        var worldPos = this.getWorldPos(chestPos.getX(), chestPos.getY() - 1, chestPos.getZ());
        if (chunkBB.isInside(worldPos)) {
            var chestState = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, chestEntry.second());
            this.placeBlock(level, chestState, chestPos.getX(), chestPos.getY() - 1, chestPos.getZ(), chunkBB);
            if (level.getBlockEntity(worldPos) instanceof ChestBlockEntity chestBlockEntity) {
                chestBlockEntity.setLootTable(Hayo.NETHER_STATION_CHEST, random.nextLong());
            }
        }
    }
}
