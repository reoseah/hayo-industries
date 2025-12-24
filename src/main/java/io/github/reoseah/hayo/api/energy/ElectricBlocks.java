package io.github.reoseah.hayo.api.energy;

import io.github.reoseah.hayo.feature.cable.CableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ElectricBlocks {
    public static int trySendToAllSides(int amount, ServerLevel level, BlockPos pos) {
        var manager = CableManager.get(level);
        return manager.sendToAllSides(amount, level, pos);
    }
}
