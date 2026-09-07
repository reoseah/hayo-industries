package hayo.energy.block;

import hayo.energy.impl.EnergyGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public class EnergyAPI {
    public static int trySendToAllSides(int amount, ServerLevel level, BlockPos pos) {
        return trySend(amount, level, pos, null);
    }

    public static int trySend(int amount, ServerLevel level, BlockPos pos, @Nullable Direction face) {
        return EnergyGrid.getOrCreate(level).sendEnergy(amount, pos, face);
    }

    public static void addOrUpdate(ServerLevel level, BlockPos pos) {
        EnergyGrid.getOrCreate(level).addOrUpdate(pos);
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        EnergyGrid.getOrCreate(level).remove(pos);
    }

    public static boolean isTracked(ServerLevel level, BlockPos pos) {
        return EnergyGrid.getOrCreate(level).isTracked(pos);
    }
}
