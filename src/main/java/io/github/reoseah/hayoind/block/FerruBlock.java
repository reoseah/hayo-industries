package io.github.reoseah.hayoind.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FerruBlock extends CropBlock {
    public static final MapCodec<FerruBlock> CODEC = simpleCodec(FerruBlock::new);
    private static final VoxelShape[] SHAPES = Block.boxes(7, i -> Block.column(16, 0, 2 + i));

    @Override
    public MapCodec<FerruBlock> codec() {
        return CODEC;
    }

    public FerruBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

//    @Override
//    protected ItemLike getBaseSeedId() {
//        return Items.CARROT;
//    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPES[this.getAge(state)];
    }
}
