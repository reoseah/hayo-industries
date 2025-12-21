package io.github.reoseah.hayo.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
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
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter blockSetter, RandomSource random, TreeConfiguration config, int maxFreeTreeHeight, FoliageAttachment attachment, int foliageHeight, int foliageRadius, int offset) {
        int dy = offset;
        this.placeLeavesRow(level, blockSetter, random, config, attachment.pos(), 0, dy--, attachment.doubleTrunk());
        this.placeLeavesRow(level, blockSetter, random, config, attachment.pos(), 0, dy--, attachment.doubleTrunk());
        this.placeLeavesRow(level, blockSetter, random, config, attachment.pos(), 1, dy--, attachment.doubleTrunk());

        int thickHeight = 1 + random.nextInt(2);
        for (int i = 0; i < thickHeight; i++) {
            this.placeLeavesRow(level, blockSetter, random, config, attachment.pos(), 2, dy--, attachment.doubleTrunk());
        }

        for (int i = 0; dy >= -foliageHeight + 1; dy--, i++) {
            int r = i % 2 == 0 ? 1 : 0;
            this.placeLeavesRow(level, blockSetter, random, config, attachment.pos(), r, dy, attachment.doubleTrunk());
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return height;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return localX == range && localZ == range && random.nextInt(2) == 0 && range != 0;
    }
}
