package io.github.reoseah.hayo.feature.machines.extractor;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.block.HorizontalDirectionalElectricalBlock;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricReceiverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ExtractorBlock extends HorizontalDirectionalElectricalBlock implements ElectricReceiverBlock {
    public static final MapCodec<ExtractorBlock> CODEC = simpleCodec(ExtractorBlock::new);

    public ExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtractorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.EXTRACTOR, world.isClientSide() ? null : ExtractorBlockEntity::tickServer);
    }
}
