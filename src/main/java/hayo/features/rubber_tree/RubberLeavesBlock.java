package hayo.features.rubber_tree;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingParticlesLeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;

public class RubberLeavesBlock extends FallingParticlesLeavesBlock {
    public RubberLeavesBlock(float leafParticleChance, Properties properties) {
        super(leafParticleChance, AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        int foliageColor = BiomeColors.getAverageFoliageColor((ClientLevel) level, pos);
        var particle = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, foliageColor);
        ParticleUtils.spawnParticleBelow(level, pos, random, particle);
    }
}
