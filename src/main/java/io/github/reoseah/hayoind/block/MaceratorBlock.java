package io.github.reoseah.hayoind.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaceratorBlock extends OrientableMachineBlock {
    public static final MapCodec<MaceratorBlock> CODEC = simpleCodec(MaceratorBlock::new);

    public MaceratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            float x = pos.getX() + 0.25F + 0.5F * random.nextFloat();
            float y = pos.getY() + 1F + 0.1F * random.nextFloat();
            float z = pos.getZ() + 0.25F + 0.5F * random.nextFloat();

            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        }
    }
}
