package io.github.reoseah.hayo.feature.machines.electric_furnace;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.block.OrientableMachineBlock;
import io.github.reoseah.hayo.feature.energy.blocks.ElectricReceiverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlock extends OrientableMachineBlock implements ElectricReceiverBlock {
    public static final MapCodec<ElectricFurnaceBlock> CODEC = simpleCodec(ElectricFurnaceBlock::new);

    public ElectricFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.ELECTRIC_FURNACE, world.isClientSide() ? null : ElectricFurnaceBlockEntity::tickServer);
    }
}
