package io.github.reoseah.hayo.feature.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ElectricBlocks {
    public static int trySendToAllSides(int amount, ServerLevel level, BlockPos pos) {
        return ElectricBlockManager.get(level).sendToAllSides(amount, pos);
    }

    public static void addOrUpdate(ServerLevel level, BlockPos pos) {
        ElectricBlockManager.get(level).addOrUpdate(pos);
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        ElectricBlockManager.get(level).remove(pos);
    }
}
