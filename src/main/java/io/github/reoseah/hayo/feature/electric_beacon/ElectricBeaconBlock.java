package io.github.reoseah.hayo.feature.electric_beacon;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy.blocks.ElectricBlocks;
import io.github.reoseah.hayo.feature.energy.blocks.ElectricReceiverBlock;
import io.github.reoseah.hayo.feature.processing_machines.matter_generator.MatterGeneratorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class ElectricBeaconBlock extends BaseEntityBlock implements ElectricReceiverBlock {
    public static final MapCodec<MatterGeneratorBlock> CODEC = simpleCodec(MatterGeneratorBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty TRANSFERRING = BooleanProperty.create("transferring");

    public ElectricBeaconBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(TRANSFERRING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, TRANSFERRING);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricBeaconBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.ELECTRIC_BEACON, world.isClientSide() ? null : ElectricBeaconBlockEntity::tickServer);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            MenuProvider factory = state.getMenuProvider(level, pos);
            if (factory == null) {
                return InteractionResult.PASS;
            }
            player.openMenu(factory);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            ElectricBlocks.remove(serverLevel, pos);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel) {
            ElectricBlocks.remove(serverLevel, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getBlock() != oldState.getBlock() && level instanceof ServerLevel serverLevel) {
            ElectricBlocks.addOrUpdate(serverLevel, pos);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || !state.getValue(TRANSFERRING)) {
            return;
        }

        float x = pos.getX() + random.nextFloat();
        float z = pos.getZ() + random.nextFloat();
        float y = pos.getY() + 1F + 0.125F * random.nextFloat();

        level.addParticle(Hayo.Particles.ELECTRIC_BEACON, x, y, z, 0, 0, 0);
    }
}
