package io.github.reoseah.hayo.feature.electric_blocks;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hayo.Hayo;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectricBlockManager {
    public static final Logger LOGGER = Logger.getLogger("HAYO/ElectricBlockManager");

    protected final ServerLevel level;
    // TODO: maybe merge this and the persisted data (electric block positions)
    //   can save manually in chunk load/unload events instead of Fabric attachments on chunks
    protected final Map<ChunkPos, ChunkTickValues> tickData = new HashMap<>();
    protected final Map<Pair<BlockPos, @Nullable Direction>, List<ReceiverPath>> pathCache = new HashMap<>();

    public ElectricBlockManager(ServerLevel level) {
        this.level = level;
    }

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

    public static ElectricBlockManager get(ServerLevel level) {
        return level.getAttachedOrCreate(Hayo.ELECTRIC_BLOCKS, () -> new ElectricBlockManager(level));
    }

    public int sendEnergy(int amount, BlockPos pos, @Nullable Direction sendingFace) {
        assert amount >= 0;

        var paths = this.pathCache.get(Pair.of(pos, sendingFace));
        if (paths == null) {
            paths = discoverReceivers(this.level, pos, sendingFace);
            this.pathCache.put(Pair.of(pos.immutable(), sendingFace), paths);
        }

        int targetsTotal = 0;

        record Target(ElectricReceiverBlock handler, int maxAmount) {
        }
        var targets = new HashMap<ReceiverPath, Target>();

        for (var iter = paths.iterator(); iter.hasNext(); ) {
            var path = iter.next();
            var state = this.level.getBlockState(path.receiver);

            if (state.getBlock() instanceof ElectricReceiverBlock receiver) {
                int maxReceivableAmount = receiver.getReceivableEnergy(this.level, path.receiver, path.receivingFace);
                assert maxReceivableAmount >= 0;
                int maxSendableAmount = Math.min(amount, maxReceivableAmount);

                targetsTotal += maxSendableAmount;
                targets.put(path, new Target(receiver, maxSendableAmount));
            } else {
                LOGGER.log(Level.WARNING, "Found stale cache entry at " + path.receiver + " while trying to send energy. A block was removed or replaced without updating energy grid. Removing entry...");
                iter.remove();
            }
        }

        if (targetsTotal == 0) {
            return 0;
        }

        int totalSent = 0;

        if (amount >= targetsTotal) {
            for (var targetEntry : targets.entrySet()) {
                var path = targetEntry.getKey();
                var target = targetEntry.getValue();

                assert amount - totalSent >= target.maxAmount;
                var received = target.handler.receiveEnergy(target.maxAmount, this.level, path.receiver, path.receivingFace);
                assert received == target.maxAmount;

                this.increaseCurrent(pos, received);
                totalSent += received;
            }
        } else {
            var fraction = Mth.ceil(amount / (float) targets.size());
            for (var targetEntry : targets.entrySet()) {

                var path = targetEntry.getKey();
                var target = targetEntry.getValue();

                int sendable = Math.min(target.maxAmount, Math.min(amount - totalSent, fraction));
                var received = target.handler.receiveEnergy(sendable, this.level, path.receiver, path.receivingFace);
                assert 0 <= received && received <= sendable;

                this.increaseCurrent(pos, received);
                totalSent += received;
                if (totalSent == amount) {
                    break;
                }
            }
            if (amount - totalSent > 0) {
                for (var targetEntry : targets.entrySet()) {
                    var path = targetEntry.getKey();
                    var target = targetEntry.getValue();
                    var received = target.handler.receiveEnergy(amount - totalSent, this.level, path.receiver, path.receivingFace);
                    assert 0 <= received && received <= amount - totalSent;

                    this.increaseCurrent(pos, received);
                    totalSent += received;
                }
            }
        }

        assert totalSent <= amount;
        return totalSent;
    }

    protected void increaseCurrent(BlockPos cablePos, int amount) {
        var chunkPos = ChunkPos.containing(cablePos);
        var data = this.tickData.computeIfAbsent(chunkPos, _ -> new ChunkTickValues());
        data.cableCurrent.put(cablePos, data.cableCurrent.getOrDefault(cablePos, 0) + amount);
    }

    public void addOrUpdate(BlockPos pos) {
        this.deleteReachablePaths(this.level, pos);

        var chunkData = this.level.getChunk(pos).getAttachedOrCreate(Hayo.CHUNK_ELECTRIC_DATA);
        var state = this.level.getBlockState(pos);
        if (state.getBlock() instanceof ElectricBlock) {
            if (!chunkData.electricBlocks.contains(pos)) {
                chunkData.electricBlocks.add(pos.immutable());
            }
        } else {
            chunkData.electricBlocks.remove(pos);
            var tickData = this.tickData.get(ChunkPos.containing(pos));
            if (tickData != null) {
                tickData.cableCurrent.removeInt(pos);
            }
        }
    }

    public void remove(BlockPos pos) {
        this.deleteReachablePaths(this.level, pos);

        var chunkData = this.level.getChunk(pos).getAttached(Hayo.CHUNK_ELECTRIC_DATA);
        if (chunkData != null) {
            chunkData.electricBlocks.remove(pos);
        }
        var chunkValues = this.tickData.get(ChunkPos.containing(pos));
        if (chunkValues != null) {
            chunkValues.cableCurrent.removeInt(pos);
            chunkValues.ticksAboveMaxCurrent.removeInt(pos);
        }
    }

    public void onLevelTickEnd() {
        for (var tickValues : this.tickData.values()) {
            for (var currentEntry : tickValues.cableCurrent.object2IntEntrySet()) {
                var pos = currentEntry.getKey();
                var current = currentEntry.getIntValue();
                var state = this.level.getBlockState(pos);

                if (state.getBlock() instanceof ElectricCableBlock cableBlock) {
                    var maxCurrent = cableBlock.getTransferLimit(state);
                    if (current > maxCurrent) {
                        tickValues.ticksAboveMaxCurrent.put(pos, tickValues.ticksAboveMaxCurrent.getOrDefault(pos, 0) + 1);
                    }
                }
            }
            for (var ticksAboveMaxCurrentEntry : tickValues.ticksAboveMaxCurrent.object2IntEntrySet()) {
                var pos = ticksAboveMaxCurrentEntry.getKey();
                var current = tickValues.cableCurrent.getOrDefault(pos, 0);
                var state = this.level.getBlockState(pos);

                if (state.getBlock() instanceof ElectricCableBlock cableBlock) {
                    var maxCurrent = cableBlock.getTransferLimit(state);
                    if (current <= maxCurrent) {
                        int value = Math.max(0, tickValues.ticksAboveMaxCurrent.getOrDefault(pos, 0) - 1);
                        if (value > 0) {
                            tickValues.ticksAboveMaxCurrent.put(pos, value);
                        } else {
                            tickValues.ticksAboveMaxCurrent.removeInt(pos);
                        }
                    }
                }
            }
            tickValues.cableCurrent.clear();
        }

        if (this.level.getGameTime() % 10 == 0) {
            for (var chunkEntry : this.tickData.entrySet()) {
                var chunkPos = chunkEntry.getKey();
                var chunkValues = chunkEntry.getValue();

                var destructionProgressMap = new Object2IntOpenHashMap<BlockPos>();

                for (var iterator = chunkValues.ticksAboveMaxCurrent.object2IntEntrySet().iterator(); iterator.hasNext(); ) {
                    var ticksAboveMaxCurrentEntry = iterator.next();
                    var pos = ticksAboveMaxCurrentEntry.getKey();
                    var ticksAboveMaxCurrent = ticksAboveMaxCurrentEntry.getIntValue();

                    if (ticksAboveMaxCurrent > 100) {
                        // TODO: call a method on ElectricCableBlock?
                        iterator.remove();
                        destructionProgressMap.put(pos, -1);
                        this.level.destroyBlock(pos, false);
                    } else if (ticksAboveMaxCurrent > 0) {
                        int destructionProgress = Mth.clamp(ticksAboveMaxCurrent / 10, 0, 9);
                        destructionProgressMap.put(pos, destructionProgress);
                    } else {
                        destructionProgressMap.put(pos, -1);
                    }
                }

                for (var iterator = destructionProgressMap.object2IntEntrySet().iterator(); iterator.hasNext(); ) {
                    var entry = iterator.next();
                    var pos = entry.getKey();
                    var destructionProgress = entry.getIntValue();

                    if (destructionProgress == chunkValues.lastDestructionProgressMap.getOrDefault(pos, -1)) {
                        iterator.remove();
                    }
                }
                chunkValues.lastDestructionProgressMap = destructionProgressMap;
                if (!destructionProgressMap.isEmpty()) {
                    PlayerLookup.tracking(this.level, chunkPos).forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, new CableBreakPayload(chunkPos, destructionProgressMap)));
                }
            }
        }
    }

    public void onChunkLoad(LevelChunk chunk) {
        try {
            var chunkData = chunk.getAttached(Hayo.CHUNK_ELECTRIC_DATA);
            if (chunkData == null) {
                return;
            }

            var server = this.level.getServer();
            server.schedule(server.wrapRunnable(() -> {
                if (this.level.isLoaded(chunk.getPos().getWorldPosition())) {
                    this.deleteReachablePaths(this.level, chunkData.electricBlocks);
                }
            }));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while updating internal state for loaded chunk", e);
        }
    }

    public void onChunkUnload(LevelChunk chunk) {
        try {
            this.tickData.remove(chunk.getPos());

            var chunkData = chunk.getAttached(Hayo.CHUNK_ELECTRIC_DATA);
            if (chunkData == null) {
                return;
            }
            this.deleteReachablePaths(this.level, chunkData.electricBlocks);
        } catch (Exception e) {
            // wrapping in try-catch, otherwise whole chunk won't get saved if something errors
            LOGGER.log(Level.SEVERE, "Error while updating internal state for unloaded chunk", e);
        }
    }

    public record ReceiverPath(BlockPos receiver, Direction receivingFace, List<BlockPos> cables) {
    }

    public static List<ReceiverPath> discoverReceivers(ServerLevel level, BlockPos start, @Nullable Direction sendingFace) {
        var queue = new ArrayDeque<BlockPos>();

        var visited = new HashMap<BlockPos, PosData>();
        visited.put(start, new PosData(0, null, level.getBlockState(start)));

        if (sendingFace != null) {
            var pos = start.relative(sendingFace);
            visitReceiverOrCableAt(level, queue, visited, pos, sendingFace, 1);
        } else {
            queue.add(start);
        }

        while (!queue.isEmpty()) {
            var queuedPos = queue.removeFirst();
            var distance = visited.get(queuedPos).distance + 1;

            for (var direction : Direction.values()) {
                var pos = queuedPos.relative(direction);
                visitReceiverOrCableAt(level, queue, visited, pos, direction, distance);
            }
        }

        var paths = new ArrayList<ReceiverPath>();
        for (var entry : visited.entrySet()) {
            var pos = entry.getKey();
            var data = entry.getValue();
            if (pos.equals(start) //
                    || !(data.state.getBlock() instanceof ElectricReceiverBlock receiver) //
                    || !receiver.canReceiveEnergy(data.state, level, pos, data.direction) //
            ) {
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

    private static void visitReceiverOrCableAt(ServerLevel level, ArrayDeque<BlockPos> queue, HashMap<BlockPos, PosData> visited, BlockPos pos, Direction direction, int distance) {
        var existingEntry = visited.get(pos);
        if (existingEntry != null) {
            if (existingEntry.distance > distance) {
                visited.put(pos, new PosData(distance, direction, existingEntry.state));
                if (existingEntry.state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
            }
            return;
        }

        if (!level.isLoaded(pos)) {
            return;
        }

        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ElectricBlock electricBlock) || !electricBlock.connectsToCables(state, level, pos, direction)) {
            return;
        }

        visited.put(pos, new PosData(distance, direction, state));
        if (state.getBlock() instanceof ElectricCableBlock) {
            queue.add(pos);
        }
    }

    private record PosData(int distance, Direction direction, BlockState state) {
    }

    public void deleteReachablePaths(ServerLevel level, BlockPos start) {
        var queue = new ArrayDeque<BlockPos>();
        queue.add(start);

        deleteReachablePathsInternal(level, queue, this.pathCache);
    }

    public void deleteReachablePaths(ServerLevel level, Collection<BlockPos> starts) {
        var queue = new ArrayDeque<>(starts);

        deleteReachablePathsInternal(level, queue, this.pathCache);
    }

    private static void deleteReachablePathsInternal(ServerLevel level, ArrayDeque<BlockPos> queue, Map<Pair<BlockPos, @Nullable Direction>, ?> paths) {
        var visited = new HashSet<BlockPos>();

        while (!queue.isEmpty()) {
            var queuedPos = queue.poll();

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
                if (state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
                if (state.getBlock() instanceof ElectricSenderBlock) {
                    paths.remove(Pair.of(pos, null));
                    for (var side : Direction.values()) {
                        paths.remove(Pair.of(pos, side));
                    }
                }
            }
        }
    }

    // TODO: doesn't need relatively large BlockPos instances, a position inside a chunk can be represented with a single int
    public static class ChunkData {
        public static final MapCodec<ChunkData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance //
                .group(BlockPos.CODEC.listOf().fieldOf("electric_blocks").forGetter(data -> ImmutableList.copyOf(data.electricBlocks))) //
                .apply(instance, ChunkData::new));

        protected final Set<BlockPos> electricBlocks;

        public ChunkData() {
            this.electricBlocks = new HashSet<>();
        }

        public ChunkData(List<BlockPos> electricBlocks) {
            this.electricBlocks = new HashSet<>(electricBlocks);
        }
    }

    public static class ChunkTickValues {
        public final Object2IntMap<BlockPos> cableCurrent = new Object2IntOpenHashMap<>();
        public final Object2IntMap<BlockPos> ticksAboveMaxCurrent = new Object2IntOpenHashMap<>();
        public Object2IntOpenHashMap<BlockPos> lastDestructionProgressMap = new Object2IntOpenHashMap<>();
    }
}
