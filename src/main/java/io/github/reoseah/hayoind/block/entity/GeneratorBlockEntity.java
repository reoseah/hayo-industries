package io.github.reoseah.hayoind.block.entity;

import io.github.reoseah.hayoind.HayoInd;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntity extends BlockEntity {
    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(HayoInd.BlockEntityTypes.GENERATOR, pos, state);
    }
}
