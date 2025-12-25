package io.github.reoseah.hayo.feature.cable;

import com.mojang.serialization.Codec;
import io.github.reoseah.hayo.api.energy.ElectricBlock;
import io.github.reoseah.hayo.api.energy.ElectricCableBlock;
import io.github.reoseah.hayo.api.energy.ElectricReceiverBlock;
import io.github.reoseah.hayo.api.energy.ElectricSenderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class CableManager extends SavedData {
    public static final String ID = "HayoCables";
    public static final SavedDataType<CableManager> TYPE = new SavedDataType<>(ID, (ctx) -> new CableManager(ctx.level()), ctx -> Codec.unit(() -> new CableManager(ctx.level())), null);

    protected final ServerLevel level;
    protected final Map<BlockPos, SenderState> senders = new HashMap<>();

    public CableManager(ServerLevel level) {
        this.level = level;
    }

    public static CableManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int sendToAllSides(int amount, BlockPos pos) {
        var senderState = this.senders.computeIfAbsent(pos, (p) -> new SenderState(discoverReceivers(this.level, p)));

        int totalSent = 0;
        // TODO distribute energy equally..
        for (var iterator = senderState.receivers.iterator(); iterator.hasNext(); ) {
            var path = iterator.next();
            var state = this.level.getBlockState(path.receiver);
            if (state.getBlock() instanceof ElectricReceiverBlock receiver) {
                int sent = receiver.receiveEnergy(amount, this.level, path.receiver, path.receivingFace);
                totalSent += sent;
                amount -= sent;
            } else {
                iterator.remove();
            }
        }

        return totalSent;
    }

    public void updateState(BlockPos pos) {
        var potentialSenders = discoverSenders(this.level, pos);
        for (var senderPos : potentialSenders) {
            this.senders.remove(senderPos);
        }
    }

    public static class SenderState {
        // TODO: directions from which energy can be emitted, e.g. for energy storages
        public final List<ReceiverPath> receivers;

        public SenderState(List<ReceiverPath> receivers) {
            this.receivers = receivers;
        }
    }

    public static class ReceiverPath {
        public final BlockPos receiver;
        public final Direction receivingFace;
        public final List<BlockPos> cablePositions;

        public ReceiverPath(BlockPos receiver, Direction receivingFace, List<BlockPos> cablePositions) {
            this.receiver = receiver;
            this.receivingFace = receivingFace;
            this.cablePositions = cablePositions;
        }
    }

    public static List<ReceiverPath> discoverReceivers(ServerLevel level, BlockPos start) {
        var queue = new ArrayDeque<BlockPos>();
        queue.add(start);

        var visited = new HashMap<BlockPos, PosData>();
        visited.put(start, new PosData(0, null, level.getBlockState(start)));

        while (!queue.isEmpty()) {
            var queuedPos = queue.removeFirst();
            var distance = visited.get(queuedPos).distance + 1;

            for (var direction : Direction.values()) {
                var pos = queuedPos.relative(direction);

                var existingEntry = visited.get(pos);
                if (existingEntry != null) {
                    if (existingEntry.distance > distance) {
                        visited.put(pos, new PosData(distance, direction, existingEntry.state));
                        if (existingEntry.state.getBlock() instanceof ElectricCableBlock) {
                            queue.add(pos);
                        }
                    }
                    continue;
                }

                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof ElectricBlock electricBlock) || !electricBlock.connectsToCables(state, level, pos, direction)) {
                    continue;
                }

                visited.put(pos, new PosData(distance, direction, state));
                if (state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
            }
        }

        var paths = new ArrayList<ReceiverPath>();
        for (var entry : visited.entrySet()) {
            var pos = entry.getKey();
            var data = entry.getValue();
            if (!(data.state.getBlock() instanceof ElectricReceiverBlock receiver)  //
                    || !receiver.canReceiveEnergy(data.state, level, pos, data.direction)) {
                continue;
            }

            var cables = new ArrayList<BlockPos>();
            var ipos = pos.relative(data.direction.getOpposite());
            var idata = visited.get(ipos);
            while (idata.distance > 0) {
                cables.add(ipos);
                ipos = ipos.relative(idata.direction.getOpposite());
                idata = visited.get(ipos);
            }

            paths.add(new ReceiverPath(pos, data.direction, cables));
        }

        return paths;
    }

    private record PosData(int distance, Direction direction, BlockState state) {
    }

    public static List<BlockPos> discoverSenders(ServerLevel level, BlockPos start) {
        var queue = new ArrayDeque<BlockPos>();
        queue.add(start);

        var visited = new HashSet<BlockPos>();
        visited.add(start);

        var result = new ArrayList<BlockPos>();

        while (!queue.isEmpty()) {
            var queuedPos = queue.removeFirst();

            for (var direction : Direction.values()) {
                var pos = queuedPos.relative(direction);
                if (visited.contains(pos)) {
                    continue;
                }

                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof ElectricBlock electricBlock) //
                        || !electricBlock.connectsToCables(state, level, pos, direction)) {
                    continue;
                }

                visited.add(pos);
                if (state.getBlock() instanceof ElectricSenderBlock) {
                    result.add(pos);
                }
                if (state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
            }
        }

        return result;
    }
}
