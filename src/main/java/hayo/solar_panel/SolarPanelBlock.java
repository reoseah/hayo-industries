package hayo.solar_panel;

import hayo.energy.block.EnergySender;
import hayo.energy.block.EnergyTransferrer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SolarPanelBlock extends Block implements EnergySender, EnergyTransferrer {
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 6.0);

    public SolarPanelBlock(Properties properties) {
        super(properties);
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
    public int getTransferLimit(BlockState state) {
        return 32;
    }

    public boolean connectsToCables(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return direction != Direction.UP;
    }
}
