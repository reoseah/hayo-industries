package io.github.reoseah.hayo.feature.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectricBlocks {
    public static final Logger LOGGER = Logger.getLogger("Hayo/ElectricBlocks");

    public static int trySendToAllSides(int amount, ServerLevel level, BlockPos pos) {
        return trySend(amount, level, pos, null);
    }

    public static int trySend(int amount, ServerLevel level, BlockPos pos, @Nullable Direction direction) {
        try {
            return ElectricBlockManager.get(level).sendEnergy(amount, pos, direction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while trying to send energy", e);
            return 0;
        }
    }

    public static void addOrUpdate(ServerLevel level, BlockPos pos) {
        try {
            ElectricBlockManager.get(level).addOrUpdate(pos);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while adding or updating electric block", e);
        }
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        try {
            ElectricBlockManager.get(level).remove(pos);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while removing electric block", e);
        }
    }
}
