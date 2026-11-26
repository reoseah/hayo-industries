package hayo.features.cable;

import hayo.common.item.HayoBlockItem;
import hayo.energy.EnergyTexts;
import hayo.energy.block.BaseEnergyBlock;
import hayo.energy.block.EnergyCable;
import hayo.energy.block.EnergyGrid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

// TODO: extend vanilla PipeBlock class?
public class CableBlock extends Block implements EnergyCable, HayoBlockItem.TooltipProvider {
    public final int transferLimit;
    protected final VoxelShape[] shapes;

    public CableBlock(int transferLimit, int radius, Properties settings) {
        super(settings);
        this.transferLimit = transferLimit;
        this.shapes = CableShapes.getOrCreate(radius);

        this.registerDefaultState(this.defaultBlockState()
                .setValue(CableShapes.DOWN, false)
                .setValue(CableShapes.UP, false)
                .setValue(CableShapes.NORTH, false)
                .setValue(CableShapes.SOUTH, false)
                .setValue(CableShapes.EAST, false)
                .setValue(CableShapes.WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CableShapes.DOWN, CableShapes.UP, CableShapes.NORTH, CableShapes.SOUTH, CableShapes.EAST, CableShapes.WEST);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes[CableShapes.getIndex(state)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.getStateForPos(ctx.getLevel(), ctx.getClickedPos());
    }

    public BlockState getStateForPos(Level level, BlockPos pos) {
        return this.defaultBlockState()
                .setValue(CableShapes.DOWN, this.connectsTo(level, pos, Direction.DOWN))
                .setValue(CableShapes.UP, this.connectsTo(level, pos, Direction.UP))
                .setValue(CableShapes.WEST, this.connectsTo(level, pos, Direction.WEST))
                .setValue(CableShapes.EAST, this.connectsTo(level, pos, Direction.EAST))
                .setValue(CableShapes.NORTH, this.connectsTo(level, pos, Direction.NORTH))
                .setValue(CableShapes.SOUTH, this.connectsTo(level, pos, Direction.SOUTH));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return state.setValue(CableShapes.getSideProperty(direction), this.connectsTo(level, pos, direction));
    }

    protected boolean connectsTo(LevelReader level, BlockPos pos, Direction side) {
        var neighborState = level.getBlockState(pos.relative(side));
        var block = neighborState.getBlock();
        if (block instanceof BaseEnergyBlock electricBlock) {
            return electricBlock.connectsToCables(neighborState, level, pos.relative(side), side.getOpposite());
        }
        return false;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.remove(serverLevel, pos);
        }
    }

    @Override
    public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack destroyedWith) {
        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.remove(serverLevel, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            EnergyGrid.addOrUpdate(serverLevel, pos);
        }
    }

    @Override
    public int getTransferLimit(BlockState state) {
        return this.transferLimit;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(EnergyTexts.maxRate(this.transferLimit).withStyle(ChatFormatting.GRAY));
    }
}
