package hayo.nether_station;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.Hayo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class NetherStationStructure extends Structure {
    public static final MapCodec<NetherStationStructure> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            settingsCodec(i),
                            HeightProvider.CODEC.fieldOf("height").forGetter(c -> c.height)
                    )
                    .apply(i, NetherStationStructure::new)
    );
    public final HeightProvider height;

    public NetherStationStructure(StructureSettings settings, HeightProvider height) {
        super(settings);
        this.height = height;
    }

    @Override
    public StructureType<?> type() {
        return Hayo.StructureTypes.NETHER_STATION;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int seaLevel = context.chunkGenerator().getSeaLevel();

        var random = context.random();
        int blockX = context.chunkPos().getMinBlockX() + random.nextInt(16);
        int blockZ = context.chunkPos().getMinBlockZ() + random.nextInt(16);

        int y = this.height.sample(random, new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        var column = context.chunkGenerator().getBaseColumn(blockX, blockZ, context.heightAccessor(), context.randomState());
        var pos = new BlockPos.MutableBlockPos(blockX, y, blockZ);

        while (y > seaLevel) {
            var current = column.getBlock(y);
            var below = column.getBlock(--y);
            if (current.isAir() && below.isFaceSturdy(EmptyBlockGetter.INSTANCE, pos.setY(y), Direction.UP)) {
                break;
            }
        }

        if (y <= seaLevel) {
            return Optional.empty();
        }

        var position = new BlockPos(blockX, y, blockZ);
        return Optional.of(
                new Structure.GenerationStub(position, builder -> NetherStationPiece.addPiece(builder, random, position))
        );
    }
}
