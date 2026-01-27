package io.github.reoseah.hayo.feature.energy_storages.advanced;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.DirectionalMachineBlock;
import io.github.reoseah.hayo.feature.energy.blocks.ElectricReceiverBlock;
import io.github.reoseah.hayo.feature.energy.blocks.ElectricSenderBlock;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancedEnergyStorageBlock extends DirectionalMachineBlock implements ElectricReceiverBlock, ElectricSenderBlock {
    public static final MapCodec<AdvancedEnergyStorageBlock> CODEC = simpleCodec(AdvancedEnergyStorageBlock::new);

    public AdvancedEnergyStorageBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedEnergyStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.ENERGY_CRYSTAL_ARRAY, world.isClientSide() ? null : EnergyStorageBlockEntity::tickServer);
    }

    @Override
    public boolean canReceiveEnergy(BlockState state, ServerLevel level, BlockPos pos, Direction side) {
        return side.getOpposite() != state.getValue(FACING);
    }
}
