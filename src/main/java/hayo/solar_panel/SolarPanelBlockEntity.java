package hayo.solar_panel;

import hayo.Hayo;
import hayo.energy.block.EnergyAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SolarPanelBlockEntity extends BlockEntity {
    public static final int ENERGY_PRODUCTION = 1;

    public SolarPanelBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(Hayo.BlockEntityTypes.SOLAR_PANEL, worldPosition, blockState);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity be) {
        if (level.canSeeSky(pos.above()) && level.isBrightOutside() && level.getSkyDarken() == 0) {
            EnergyAPI.trySendToAllSides(ENERGY_PRODUCTION, (ServerLevel) level, pos);
        }
    }
}
