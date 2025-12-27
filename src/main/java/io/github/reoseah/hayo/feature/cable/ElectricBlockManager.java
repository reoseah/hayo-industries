package io.github.reoseah.hayo.feature.cable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.api.energy.ElectricBlock;
import io.github.reoseah.hayo.api.energy.ElectricCableBlock;
import io.github.reoseah.hayo.api.energy.ElectricReceiverBlock;
import io.github.reoseah.hayo.api.energy.ElectricSenderBlock;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class ElectricBlockManager extends SavedData {
    public static final String ID = "HayoElectricBlocks";
    public static final SavedDataType<ElectricBlockManager> TYPE = new SavedDataType<>(ID, (ctx) -> new ElectricBlockManager(ctx.level()), ctx -> Codec.unit(() -> new ElectricBlockManager(ctx.level())), null);

    protected final ServerLevel level;
    protected final Map<ChunkPos, ChunkTickData> tickData = new HashMap<>();
    protected final Map<BlockPos, SenderState> senders = new HashMap<>();

    public ElectricBlockManager(ServerLevel level) {
        this.level = level;
    }

    public static ElectricBlockManager get(ServerLevel level) {
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
                int received = receiver.receiveEnergy(amount, this.level, path.receiver, path.receivingFace);

                for (var cablePos : path.cablePositions) {
                    this.increaseCurrent(cablePos, received);
                }
                totalSent += received;
                amount -= received;
            } else {
                iterator.remove();
            }
        }

        return totalSent;
    }

    protected void increaseCurrent(BlockPos cablePos, int amount) {
        var chunkPos = new ChunkPos(cablePos);
        var data = this.tickData.computeIfAbsent(chunkPos, p -> new ChunkTickData());
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
            if (state.getBlock() instanceof ElectricCableBlock cable) {
                var tickData = this.tickData.computeIfAbsent(new ChunkPos(pos), p -> new ChunkTickData());
                int transferLimit = cable.getTransferLimit(state);

                tickData.cableTransferLimits.put(pos, transferLimit);
            }
        } else {
            chunkData.electricBlocks.remove(pos);
            var tickData = this.tickData.get(new ChunkPos(pos));
            if (tickData != null) {
                tickData.cableTransferLimits.removeInt(pos);
                tickData.cableCurrent.removeInt(pos);
            }
        }
    }

    public void remove(BlockPos pos) {
        deleteReachableSenders(this.level, pos, this.senders);

        var chunkData = this.level.getChunk(pos).getAttachedOrCreate(Hayo.CHUNK_ELECTRIC_DATA);
        chunkData.electricBlocks.remove(pos);
    }

    public void onLevelTickEnd() {
        if (this.level.getGameTime() % 20 == 0) {
            // FIXME debugging
            System.out.println(this.tickData);
        }

        for (var chunkData : this.tickData.values()) {
            for (var cableEntry : chunkData.cableCurrent.object2IntEntrySet()) {
                var cablePos = cableEntry.getKey();
                var current = cableEntry.getIntValue();
                var maxCurrent = chunkData.cableTransferLimits.getOrDefault(cablePos, 0);
                if (current > maxCurrent) {
                    // FIXME test
                    this.level.destroyBlockProgress(-Math.abs(cablePos.hashCode()), cablePos, 5);
                }
            }
            chunkData.cableCurrent.clear();
        }
    }

    public void onChunkLoad(LevelChunk chunk) {
        var data = chunk.getAttached(Hayo.CHUNK_ELECTRIC_DATA);
        if (data == null) {
            return;
        }

        for (var pos : data.electricBlocks) {
            var state = chunk.getBlockState(pos);
            System.out.println(pos + " " + state);

            if (state.getBlock() instanceof ElectricCableBlock cable) {
                var tickData = this.tickData.computeIfAbsent(chunk.getPos(), p -> new ChunkTickData());
                int transferLimit = cable.getTransferLimit(state);
                System.out.println("chunk load cable transfer limit: " + pos + " " + transferLimit);
                tickData.cableTransferLimits.put(pos, transferLimit);
            }
        }
    }

    public void onChunkUnload(LevelChunk chunk) {
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

    public static class ChunkSavedData {
        public static final MapCodec<ChunkSavedData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance //
                .group(BlockPos.CODEC.listOf().fieldOf("electric_blocks").forGetter(data -> data.electricBlocks)) //
                .apply(instance, ChunkSavedData::new));

        protected final List<BlockPos> electricBlocks;

        public ChunkSavedData() {
            this.electricBlocks = new ArrayList<>();
        }

        public ChunkSavedData(@Unmodifiable List<BlockPos> electricBlocks) {
            this.electricBlocks = new ArrayList<>(electricBlocks);
        }

        @Override
        public String toString() {
            return "ChunkElectricData{" + //
                    "electricBlocks=" + this.electricBlocks + //
                    '}';
        }
    }

    public static class ChunkTickData {
        public final Object2IntMap<BlockPos> cableTransferLimits = new Object2IntOpenHashMap<>();
        public final Object2IntMap<BlockPos> cableCurrent = new Object2IntOpenHashMap<>();

        @Override
        public String toString() {
            return "ChunkTickData{" + //
                    "cableCurrent=" + this.cableCurrent + //
                    "cableMaxCurrent=" + this.cableTransferLimits + //
                    '}';
        }
    }
}
