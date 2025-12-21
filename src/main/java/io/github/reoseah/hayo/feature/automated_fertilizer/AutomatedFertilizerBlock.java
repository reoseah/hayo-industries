package io.github.reoseah.hayo.feature.automated_fertilizer;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.base.block.OrientableMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AutomatedFertilizerBlock extends OrientableMachineBlock {
    public static final MapCodec<AutomatedFertilizerBlock> CODEC = simpleCodec(AutomatedFertilizerBlock::new);

    public AutomatedFertilizerBlock(Properties properties) {
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
}
