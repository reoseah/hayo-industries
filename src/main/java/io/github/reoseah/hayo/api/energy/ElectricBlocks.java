package io.github.reoseah.hayo.api.energy;

import io.github.reoseah.hayo.feature.cable.ElectricBlockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ElectricBlocks {
    public static int trySendToAllSides(int amount, ServerLevel level, BlockPos pos) {
        var manager = ElectricBlockManager.get(level);
        return manager.sendToAllSides(amount, pos);
    }

    public static void addOrUpdate(ServerLevel level, BlockPos pos) {
        var manager = ElectricBlockManager.get(level);
        manager.addOrUpdate(pos);
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        ElectricBlockManager.get(level).remove(pos);
    }
}
