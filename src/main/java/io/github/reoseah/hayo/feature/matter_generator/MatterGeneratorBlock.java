package io.github.reoseah.hayo.feature.matter_generator;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.base.block.OrientableMachineBlock;
import io.github.reoseah.hayo.feature.energy.ElectricReceiverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MatterGeneratorBlock extends OrientableMachineBlock implements ElectricReceiverBlock {
    public static final MapCodec<MatterGeneratorBlock> CODEC = simpleCodec(MatterGeneratorBlock::new);

    public MatterGeneratorBlock(Properties properties) {
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
