package io.github.reoseah.hayo.feature.wrench;

import io.github.reoseah.hayo.Hayo;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class WrenchItem extends Item {
    public static final TagKey<Block> WRENCHABLE = TagKey.create(Registries.BLOCK, Hayo.modId("wrenchable"));

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);

        if (state.is(WRENCHABLE)) {
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                var currentFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                var playerFacing = context.getClickedFace().getAxis() != Direction.Axis.Y //
                        ? context.getClickedFace() //
                        : context.getHorizontalDirection().getOpposite();
                if (currentFacing != playerFacing) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing));
                    return InteractionResult.SUCCESS;
                }
            } else if (state.hasProperty(BlockStateProperties.FACING)) {
                var currentFacing = state.getValue(BlockStateProperties.FACING);
                var playerFacing = context.getClickedFace();
                if (currentFacing != playerFacing) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.FACING, playerFacing));
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.useOn(context);
    }
}
