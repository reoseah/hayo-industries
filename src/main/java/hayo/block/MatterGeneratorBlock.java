package hayo.block;

import com.mojang.serialization.MapCodec;
import hayo.Hayo;
import hayo.energy.block.ElectricReceiverBlock;
import hayo.block.entity.MatterGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MatterGeneratorBlock extends HorizontalDirectionalElectricalBlock implements ElectricReceiverBlock {
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
        return new MatterGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.MATTER_GENERATOR, world.isClientSide() ? null : MatterGeneratorBlockEntity::tickServer);
    }
}
