package hayo.wrench;

import hayo.Hayo;
import hayo.energy.block.BaseEnergyBlock;
import hayo.energy.block.EnergyGrid;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);

        boolean change = false;

        if (state.is(Hayo.HBlockTags.ROTATABLE_WITH_WRENCH)) {
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                var currentFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                var playerFacing = context.getClickedFace().getAxis() != Direction.Axis.Y
                        ? context.getClickedFace()
                        : context.getHorizontalDirection().getOpposite();
                if (currentFacing != playerFacing) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing));
                    level.playSound(context.getPlayer(), pos, Hayo.SoundEvents.WRENCH_USE, SoundSource.PLAYERS);
                    change = true;
                }
            } else if (state.hasProperty(BlockStateProperties.FACING)) {
                var currentFacing = state.getValue(BlockStateProperties.FACING);
                var playerFacing = context.getClickedFace();
                if (currentFacing != playerFacing) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.FACING, playerFacing));
                    level.playSound(context.getPlayer(), pos, Hayo.SoundEvents.WRENCH_USE, SoundSource.PLAYERS);
                    change = true;
                }
            }
        }

        if (change
                && state.getBlock() instanceof BaseEnergyBlock
                && level instanceof ServerLevel serverLevel
                && EnergyGrid.isTracked(serverLevel, pos)) {
            EnergyGrid.addOrUpdate(serverLevel, pos);
        }

        if (change) {
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }
}
