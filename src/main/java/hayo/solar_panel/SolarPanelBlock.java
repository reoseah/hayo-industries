package hayo.solar_panel;

import com.mojang.serialization.MapCodec;
import hayo.Hayo;
import hayo.energy.block.EnergyCable;
import hayo.energy.block.EnergyGrid;
import hayo.energy.block.EnergySender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SolarPanelBlock extends BaseEntityBlock implements EnergySender, EnergyCable {
    public static final MapCodec<SolarPanelBlock> CODEC = simpleCodec(SolarPanelBlock::new);
    public static final VoxelShape SHAPE = Block.column(16.0, 0.0, 6.0);

    public SolarPanelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, Hayo.BlockEntityTypes.SOLAR_PANEL, world.isClientSide() ? null : SolarPanelBlockEntity::tickServer);
    }

    @Override
    public int getTransferLimit(BlockState state) {
        return 8;
    }

    @Override
    public boolean connectsToCables(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return direction != Direction.UP;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.remove(serverLevel, pos);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.remove(serverLevel, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getBlock() != oldState.getBlock() && level instanceof ServerLevel serverLevel) {
            EnergyGrid.addOrUpdate(serverLevel, pos);
        }
    }
}
