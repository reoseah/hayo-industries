package hayo.features.rubber_tree;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.Hayo;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class RubberFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<RubberFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> foliagePlacerParts((instance)).apply(instance, RubberFoliagePlacer::new));

    public RubberFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return Hayo.FoliagePlacerTypes.RUBBER;
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliageSetter foliageSetter, RandomSource random, TreeFeature tree, int treeHeight, FoliageAttachment foliageAttachment, int foliageHeight, int leafRadius, int offset) {
        int dy = offset;
        this.placeLeavesRow(level, foliageSetter, random, tree, foliageAttachment.pos(), 0, dy--, foliageAttachment.doubleTrunk());
        this.placeLeavesRow(level, foliageSetter, random, tree, foliageAttachment.pos(), 0, dy--, foliageAttachment.doubleTrunk());
        this.placeLeavesRow(level, foliageSetter, random, tree, foliageAttachment.pos(), 1, dy--, foliageAttachment.doubleTrunk());

        int thickHeight = 1 + random.nextInt(2);
        for (int i = 0; i < thickHeight; i++) {
            this.placeLeavesRow(level, foliageSetter, random, tree, foliageAttachment.pos(), 2, dy--, foliageAttachment.doubleTrunk());
        }

        for (int i = 0; dy >= -foliageHeight + 1; dy--, i++) {
            int r = i % 2 == 0 ? 1 : 0;
            this.placeLeavesRow(level, foliageSetter, random, tree, foliageAttachment.pos(), r, dy, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeFeature tree) {
        return treeHeight;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return localX == range && localZ == range && random.nextInt(2) == 0 && range != 0;
    }
}
