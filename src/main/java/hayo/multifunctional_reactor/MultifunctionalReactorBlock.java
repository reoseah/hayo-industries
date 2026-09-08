package hayo.multifunctional_reactor;

import com.mojang.serialization.MapCodec;
import hayo.common.block.HorizontalDirectionalElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MultifunctionalReactorBlock extends HorizontalDirectionalElectricalBlock {
    public static final MapCodec<MultifunctionalReactorBlock> CODEC = simpleCodec(MultifunctionalReactorBlock::new);

    public MultifunctionalReactorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultifunctionalReactorBlockEntity(pos, state);
    }
}
