package io.github.reoseah.hayo.feature.cable;

import com.mojang.serialization.Codec;
import io.github.reoseah.hayo.api.energy.ElectricBlock;
import io.github.reoseah.hayo.api.energy.ElectricCableBlock;
import io.github.reoseah.hayo.api.energy.ElectricReceiverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class CableManager extends SavedData {
    public static final String ID = "HayoCables";
    public static final SavedDataType<CableManager> TYPE = new SavedDataType<>(ID, (ctx) -> new CableManager(ctx.level()), ctx -> Codec.unit(() -> new CableManager(ctx.level())), null);

    protected final ServerLevel level;
    protected final Map<BlockPos, SenderCache> senders = new HashMap<>();

    public CableManager(ServerLevel level) {
        this.level = level;
    }

    public static CableManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int sendToAllSides(int amount, ServerLevel level, BlockPos pos) {
        // FIXME cache these
        var paths = CablePath.build(pos, level);

        int totalSent = 0;
        // TODO distribute energy equally..
        for (var path : paths) {
            var state = level.getBlockState(path.end);
            if (state.getBlock() instanceof ElectricReceiverBlock receiver) {
                int sent = receiver.receiveEnergy(amount, level, path.end, path.endFace);
                totalSent += sent;
                amount -= sent;
            }
        }

        return totalSent;
    }

    public static class SenderCache {
        // TODO: directions from which energy can be emitted, e.g. for energy storages
        public final List<CablePath> destinations;

        public SenderCache(List<CablePath> destinations) {
            this.destinations = destinations;
        }
    }

    public static class CablePath {
        public final BlockPos end;
        public final Direction endFace;
        public final List<BlockPos> cablePositions;

        public CablePath(BlockPos end, Direction endFace, List<BlockPos> cablePositions) {
            this.end = end;
            this.endFace = endFace;
            this.cablePositions = cablePositions;
        }

        public static List<CablePath> build(BlockPos start, ServerLevel level) {
            var queue = new ArrayDeque<BlockPos>();
            queue.add(start);

            var visited = new HashMap<BlockPos, PosData>();
            visited.put(start, new PosData(0, null));

            while (!queue.isEmpty()) {
                var pos = queue.removeFirst();
                var nextDistance = visited.get(pos).distance + 1;

                for (var direction : Direction.values()) {
                    var next = pos.relative(direction);
                    var state = level.getBlockState(next); // TODO: "cache" state in PosData field?
                    if (!(state.getBlock() instanceof ElectricBlock)) {
                        continue;
                    }

                    if (!visited.containsKey(next) || nextDistance < visited.get(next).distance) {
                        visited.put(next, new PosData(nextDistance, direction.getOpposite()));
                        if (state.getBlock() instanceof ElectricCableBlock) {
                            queue.add(next); // FIXME: handle loops, avoid excessive work?
                        }
                    }
                }
            }

//            System.out.println(visited);

            var paths = new ArrayList<CablePath>();
            for (var entry : visited.entrySet()) {
                var pos = entry.getKey();
                var data = entry.getValue();
                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof ElectricReceiverBlock receiver)) {
                    continue;
                }
                if (!receiver.canReceiveEnergy(state, level, pos, data.back)) {
                    continue;
                }

                var cables = new ArrayList<BlockPos>();
                var ipos = pos.relative(data.back);
                var idata = visited.get(ipos);
                while (idata.distance > 0) {
                    cables.add(ipos);
                    ipos = ipos.relative(idata.back);
                    idata = visited.get(ipos);
                }

                var path = new CablePath(pos, data.back, cables);
                paths.add(path);
            }

            return paths;
        }

        private record PosData(int distance, Direction back) {
        }
    }

}
