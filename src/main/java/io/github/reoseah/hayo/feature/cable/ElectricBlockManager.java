package io.github.reoseah.hayo.feature.cable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.api.energy.ElectricBlock;
import io.github.reoseah.hayo.api.energy.ElectricCableBlock;
import io.github.reoseah.hayo.api.energy.ElectricReceiverBlock;
import io.github.reoseah.hayo.api.energy.ElectricSenderBlock;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectricBlockManager extends SavedData {
    public static final Logger LOGGER = Logger.getLogger("Hayo/ElectricBlockManager");

    protected final ServerLevel level;
    protected final Map<ChunkPos, ChunkTickValues> tickData = new HashMap<>();
    protected final Map<BlockPos, SenderState> senders = new HashMap<>();

    public ElectricBlockManager(ServerLevel level) {
        this.level = level;
    }

    public static ElectricBlockManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(Hayo.ELECTRIC_DATA);
    }

    public int sendToAllSides(int amount, BlockPos pos) {
        var senderState = this.senders.computeIfAbsent(pos, (p) -> new SenderState(discoverReceivers(this.level, p)));

        int totalReceivable = 0;
        var receivables = new Object2IntArrayMap<ReceiverPath>();

        for (var iterator = senderState.receivers.iterator(); iterator.hasNext(); ) {
            var path = iterator.next();
            var state = this.level.getBlockState(path.receiver);

            if (state.getBlock() instanceof ElectricReceiverBlock receiver) {
                int receivable = receiver.getReceivableEnergy(this.level, path.receiver, path.receivingFace);

                receivables.put(path, receivable);
                totalReceivable += receivable;
            } else {
                iterator.remove();
            }
        }

        if (totalReceivable == 0) {
            return 0;
        }

        int totalSent = 0;
        if (amount >= totalReceivable) {
            for (var path : senderState.receivers) {
                var state = this.level.getBlockState(path.receiver);

                if (state.getBlock() instanceof ElectricReceiverBlock receiver) {
                    int received = receiver.receiveEnergy(amount, this.level, path.receiver, path.receivingFace);

                    for (var cablePos : path.cables) {
                        this.increaseCurrent(cablePos, received);
                    }
                    totalSent += received;
                    amount -= received;
                }
            }
        } else {
            var offer = Mth.ceil(amount / (float) receivables.size());

            for (var path : senderState.receivers) {
                var state = this.level.getBlockState(path.receiver);

                if (state.getBlock() instanceof ElectricReceiverBlock receiver) {
                    int received = receiver.receiveEnergy(Math.min(amount, offer), this.level, path.receiver, path.receivingFace);

                    for (var cablePos : path.cables) {
                        this.increaseCurrent(cablePos, received);
                    }
                    totalSent += received;
                    amount -= received;
                }
            }
        }

        return totalSent;
    }

    protected void increaseCurrent(BlockPos cablePos, int amount) {
        var chunkPos = new ChunkPos(cablePos);
        var data = this.tickData.computeIfAbsent(chunkPos, p -> new ChunkTickValues());
        data.cableCurrent.put(cablePos, data.cableCurrent.getOrDefault(cablePos, 0) + amount);
    }

    public void addOrUpdate(BlockPos pos) {
        deleteReachableSenders(this.level, pos, this.senders);

        var chunkData = this.level.getChunk(pos).getAttachedOrCreate(Hayo.CHUNK_ELECTRIC_DATA);
        var state = this.level.getBlockState(pos);
        if (state.getBlock() instanceof ElectricBlock) {
            if (!chunkData.electricBlocks.contains(pos)) {
                chunkData.electricBlocks.add(pos.immutable());
            }
        } else {
            chunkData.electricBlocks.remove(pos);
            var tickData = this.tickData.get(new ChunkPos(pos));
            if (tickData != null) {
                tickData.cableCurrent.removeInt(pos);
            }
        }
    }

    public void remove(BlockPos pos) {
        deleteReachableSenders(this.level, pos, this.senders);

        var chunkData = this.level.getChunk(pos).getAttached(Hayo.CHUNK_ELECTRIC_DATA);
        if (chunkData != null) {
            chunkData.electricBlocks.remove(pos);
        }
        var chunkValues = this.tickData.get(new ChunkPos(pos));
        if (chunkValues != null) {
            chunkValues.cableCurrent.removeInt(pos);
            chunkValues.ticksAboveMaxCurrent.removeInt(pos);
        }
    }

    public void onLevelTickEnd() {
        for (var chunkValues : this.tickData.values()) {
            for (var currentEntry : chunkValues.cableCurrent.object2IntEntrySet()) {
                var pos = currentEntry.getKey();
                var current = currentEntry.getIntValue();
                var state = this.level.getBlockState(pos);

                if (state.getBlock() instanceof ElectricCableBlock cableBlock) {
                    var maxCurrent = cableBlock.getTransferLimit(state);
                    if (current > maxCurrent) {
                        chunkValues.ticksAboveMaxCurrent.put(pos, chunkValues.ticksAboveMaxCurrent.getOrDefault(pos, 0) + 1);
                    }
                }
            }
            for (var ticksAboveMaxCurrentEntry : chunkValues.ticksAboveMaxCurrent.object2IntEntrySet()) {
                var pos = ticksAboveMaxCurrentEntry.getKey();
                var current = chunkValues.cableCurrent.getOrDefault(pos, 0);
                var state = this.level.getBlockState(pos);

                if (state.getBlock() instanceof ElectricCableBlock cableBlock) {
                    var maxCurrent = cableBlock.getTransferLimit(state);
                    if (current <= maxCurrent) {
                        chunkValues.ticksAboveMaxCurrent.put(pos, Math.max(0, chunkValues.ticksAboveMaxCurrent.getOrDefault(pos, 0) - 1));
                    }
                }
            }
            chunkValues.cableCurrent.clear();
        }

        if (this.level.getGameTime() % 20 == 0) {
            for (var chunkValues : this.tickData.values()) {
                for (var ticksAboveMaxCurrentEntry : chunkValues.ticksAboveMaxCurrent.object2IntEntrySet()) {
                    var pos = ticksAboveMaxCurrentEntry.getKey();
                    var ticksAboveMaxCurrent = ticksAboveMaxCurrentEntry.getIntValue();

                    if (ticksAboveMaxCurrent > 100) {
                        // TODO: call a method on ElectricCableBlock?
                        ticksAboveMaxCurrentEntry.setValue(0);
                        this.level.destroyBlockProgress(-Mth.abs(pos.hashCode()), pos, -1);
                        this.level.destroyBlock(pos, false);
                    } else if (ticksAboveMaxCurrent > 0) {
                        int destructionProgress = Mth.clamp(ticksAboveMaxCurrent / 10, 0, 9);
                        // FIXME group into one custom packet per chunk, spawn smoke particles on client
                        this.level.destroyBlockProgress(-Mth.abs(pos.hashCode()), pos, destructionProgress);
                    } else {
                        this.level.destroyBlockProgress(-Mth.abs(pos.hashCode()), pos, -1);
                    }
                }
            }
        }
    }

    public void onChunkLoad(LevelChunk chunk) {
        var data = chunk.getAttached(Hayo.CHUNK_ELECTRIC_DATA);
        if (data == null) {
            return;
        }

        // TODO: update potential senders somehow, it seems block information can't be accessed at this point,
        //  (and doing world.getBlockState gets stuck in a thread lock...)
    }

    public void onChunkUnload(LevelChunk chunk) {
        // wrapping in try-catch because if something errors the chunk won't be saved
        try {
            this.tickData.remove(chunk.getPos());

            var data = chunk.getAttached(Hayo.CHUNK_ELECTRIC_DATA);
            if (data == null) {
                return;
            }

            for (var pos : data.electricBlocks) {
                // TODO: probably more efficient to make one BFS search,
                //    with `query.addAll(data.electricBlocks)` instead of BFS for every pos
                this.remove(pos);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while updating internal state for unloaded chunk", e);
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
        public final List<BlockPos> cables;

        public ReceiverPath(BlockPos receiver, Direction receivingFace, List<BlockPos> cables) {
            this.receiver = receiver;
            this.receivingFace = receivingFace;
            this.cables = cables;
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

                if (!level.isLoaded(pos)) {
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

    public static void deleteReachableSenders(ServerLevel level, BlockPos start, Map<BlockPos, ?> data) {
        var queue = new ArrayDeque<BlockPos>();
        queue.add(start);

        var visited = new HashSet<BlockPos>();
        visited.add(start);

        while (!queue.isEmpty()) {
            var queuedPos = queue.removeFirst();

            for (var direction : Direction.values()) {
                var pos = queuedPos.relative(direction);
                if (visited.contains(pos)) {
                    continue;
                }

                if (!level.isLoaded(pos)) {
                    continue;
                }

                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof ElectricBlock electricBlock) //
                        || !electricBlock.connectsToCables(state, level, pos, direction)) {
                    continue;
                }

                visited.add(pos);
                if (state.getBlock() instanceof ElectricSenderBlock) {
                    data.remove(pos);
                }
                if (state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
            }
        }
    }

    public static class ChunkData {
        public static final MapCodec<ChunkData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance //
                .group(BlockPos.CODEC.listOf().fieldOf("electric_blocks").forGetter(data -> data.electricBlocks)) //
                .apply(instance, ChunkData::new));

        // don't need to be full BlockPos instance, knowing a chunk position it should be possible to
        // represent any position within as a `short` or at least an `int` with bit fiddling...
        protected final List<BlockPos> electricBlocks;

        public ChunkData() {
            this.electricBlocks = new ArrayList<>();
        }

        public ChunkData(@Unmodifiable List<BlockPos> electricBlocks) {
            this.electricBlocks = new ArrayList<>(electricBlocks);
        }
    }

    public static class ChunkTickValues {
        public final Object2IntMap<BlockPos> cableCurrent = new Object2IntOpenHashMap<>();
        public final Object2IntMap<BlockPos> ticksAboveMaxCurrent = new Object2IntOpenHashMap<>();
    }
}
