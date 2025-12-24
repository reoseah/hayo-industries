package io.github.reoseah.hayo.feature.energy_crystal_array;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.api.energy.ElectricReceiverBlock;
import io.github.reoseah.hayo.base.block.DirectionalMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnergyCrystalArrayBlock extends DirectionalMachineBlock implements ElectricReceiverBlock {
    public static final MapCodec<EnergyCrystalArrayBlock> CODEC = simpleCodec(EnergyCrystalArrayBlock::new);

    public EnergyCrystalArrayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCrystalArrayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.ENERGY_CRYSTAL_ARRAY, world.isClientSide() ? null : EnergyCrystalArrayBlockEntity::tickServer);
    }

    @Override
    public boolean canReceiveEnergy(BlockState state, ServerLevel level, BlockPos pos, Direction side) {
        return side != state.getValue(FACING);
    }

    @Override
    public int receiveEnergy(int amount, ServerLevel level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof EnergyCrystalArrayBlockEntity entity) {
            return EnergyCrystalArrayBlockEntity.receiveEnergy(amount, level, pos, side, entity);
        }
        return 0;
    }
}
