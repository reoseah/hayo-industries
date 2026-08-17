package io.github.reoseah.hayo.block;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.block.entity.BatteryBoxBlockEntity;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricReceiverBlock;
import io.github.reoseah.hayo.feature.electric_blocks.ElectricSenderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.Nullable;

public class BatteryBoxBlock extends HorizontalDirectionalElectricalBlock implements ElectricReceiverBlock, ElectricSenderBlock {
    public static final MapCodec<BatteryBoxBlock> CODEC = simpleCodec(BatteryBoxBlock::new);
    public static final IntegerProperty BATTERIES = IntegerProperty.create("batteries", 0, 6);

    public BatteryBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BATTERIES, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BATTERIES);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatteryBoxBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.BATTERY_BOX, world.isClientSide() ? null : BatteryBoxBlockEntity::tickServer);
    }

    @Override
    public boolean canReceiveEnergy(BlockState state, ServerLevel level, BlockPos pos, Direction side) {
        return side.getOpposite() != state.getValue(FACING);
    }
}
