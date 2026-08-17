package io.github.reoseah.hayo.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricReceiverBlock;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricSenderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CrystalEnergyStorageBlock extends DirectionalElectricalBlock implements ElectricReceiverBlock, ElectricSenderBlock {
    public static final MapCodec<CrystalEnergyStorageBlock> CODEC = simpleCodec(CrystalEnergyStorageBlock::new);

    public CrystalEnergyStorageBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalEnergyStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.CRYSTAL_ENERGY_STORAGE, world.isClientSide() ? null : EnergyStorageBlockEntity::tickServer);
    }

    @Override
    public boolean canReceiveEnergy(BlockState state, ServerLevel level, BlockPos pos, Direction side) {
        return side.getOpposite() != state.getValue(FACING);
    }
}
