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
    protected final Map<ChunkPos, TickData> tickData = new HashMap<>();
    protected final Map<Pair<BlockPos, @Nullable Direction>, List<TransferPath>> pathCache = new HashMap<>();

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
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

        var paths = this.pathCache.get(Pair.of(pos, sendingFace));
        if (paths == null) {
            paths = discoverPaths(this.level, pos, sendingFace);
            this.pathCache.put(Pair.of(pos.immutable(), sendingFace), paths);
        }

        int targetsTotal = 0;

        record Target(ElectricReceiverBlock handler, int maxAmount) {
        }
        var targets = new HashMap<TransferPath, Target>();

        for (var iter = paths.iterator(); iter.hasNext(); ) {
            var path = iter.next();
            var state = this.level.getBlockState(path.receiver);

            if (state.getBlock() instanceof ElectricReceiverBlock receiver) {
                int maxReceivableAmount = receiver.getReceivableEnergy(this.level, path.receiver, path.receivingFace);
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

                var received = target.handler.receiveEnergy(target.maxAmount, this.level, path.receiver, path.receivingFace);

                this.increaseCurrent(path, received);
                totalSent += received;
            }
        } else {
            var fraction = Mth.ceil(amount / (float) targets.size());
            for (var targetEntry : targets.entrySet()) {
                var path = targetEntry.getKey();
                var target = targetEntry.getValue();

                int sendable = Math.min(target.maxAmount, Math.min(amount - totalSent, fraction));
                var received = target.handler.receiveEnergy(sendable, this.level, path.receiver, path.receivingFace);

                this.increaseCurrent(path, received);
                totalSent += received;
                if (totalSent == amount) {
                    break;
                }
            }
            if (amount - totalSent > 0) {
                for (var targetEntry : targets.entrySet()) {
                    var path = targetEntry.getKey();
                    var target = targetEntry.getValue();
                    int sendable = amount - totalSent;
                    var received = target.handler.receiveEnergy(sendable, this.level, path.receiver, path.receivingFace);

                    this.increaseCurrent(path, received);
                    totalSent += received;
                }
            }
        }

        assert totalSent <= amount;
        return totalSent;
    }

    protected void increaseCurrent(TransferPath path, int amount) {
        for (var cablePos : path.cables) {
            var chunkPos = ChunkPos.containing(cablePos);
            var data = this.tickData.computeIfAbsent(chunkPos, _ -> new TickData());
            data.transferPerTick.put(cablePos, data.transferPerTick.getOrDefault(cablePos, 0) + amount);
        }
    }

    public void addOrUpdate(BlockPos pos) {
        this.deletePaths(pos);

        var data = this.level.getChunk(pos).getAttachedOrCreate(Hayo.CHUNK_ELECTRIC_DATA);
        var state = this.level.getBlockState(pos);
        if (state.getBlock() instanceof ElectricBlock) {
            if (!data.electricBlocks.contains(pos)) {
                data.electricBlocks.add(pos.immutable());
            }
        } else {
            data.electricBlocks.remove(pos);
            var tickData = this.tickData.get(ChunkPos.containing(pos));
            if (tickData != null) {
                tickData.transferPerTick.removeInt(pos);
            }
        }
    }

    public void remove(BlockPos pos) {
        this.deletePaths(pos);

        var data = this.level.getChunk(pos).getAttached(Hayo.CHUNK_ELECTRIC_DATA);
        if (data != null) {
            data.electricBlocks.remove(pos);
        }
        var tickData = this.tickData.get(ChunkPos.containing(pos));
        if (tickData != null) {
            tickData.transferPerTick.removeInt(pos);
            tickData.destroyTicks.removeInt(pos);
            // TODO: reset destroy stage on clients
        }
    }

    public void onLevelTickEnd() {
        for (var data : this.tickData.values()) {
            for (var entry : data.transferPerTick.object2IntEntrySet()) {
                var pos = entry.getKey();
                var transfer = entry.getIntValue();
                var state = this.level.getBlockState(pos);

                if (state.getBlock() instanceof ElectricCableBlock cableBlock) {
                    var limit = cableBlock.getTransferLimit(state);
                    if (transfer > limit) {
                        data.destroyTicks.put(pos, data.destroyTicks.getOrDefault(pos, 0) + 1);
                    }
                }
            }
            for (var entry1 : data.destroyTicks.object2IntEntrySet()) {
                var pos1 = entry1.getKey();
                var transfer = data.transferPerTick.getOrDefault(pos1, 0);

                var state = this.level.getBlockState(pos1);
                if (state.getBlock() instanceof ElectricCableBlock cableBlock) {
                    var limit = cableBlock.getTransferLimit(state);
                    if (transfer <= limit) {
                        int prevDestroyTicks = data.destroyTicks.getOrDefault(pos1, 0);
                        int destroyTicks = Math.max(0, prevDestroyTicks - 1);
                        if (destroyTicks == 0) {
                            data.destroyTicks.removeInt(pos1);
                        } else {
                            data.destroyTicks.put(pos1, destroyTicks);
                        }
                    }
                }
            }
            data.transferPerTick.clear();
        }

        if (this.level.getGameTime() % 10 == 0) {
            for (var dataEntry : this.tickData.entrySet()) {
                var chunkPos = dataEntry.getKey();
                var data = dataEntry.getValue();

                var newDestroyStage = new Object2IntOpenHashMap<BlockPos>();

                for (var iter = data.destroyTicks.object2IntEntrySet().iterator(); iter.hasNext(); ) {
                    var entry = iter.next();
                    var pos = entry.getKey();
                    var ticks = entry.getIntValue();

                    if (ticks > 100) {
                        iter.remove();
                        newDestroyStage.put(pos, -1);
                        this.level.destroyBlock(pos, false);
                    } else if (ticks > 0) {
                        int stage = Math.clamp(ticks / 10, 0, 9);
                        newDestroyStage.put(pos, stage);
                    } else {
                        newDestroyStage.put(pos, -1);
                    }
                }

                for (var iter = newDestroyStage.object2IntEntrySet().iterator(); iter.hasNext(); ) {
                    var entry = iter.next();
                    var pos = entry.getKey();
                    var stage = entry.getIntValue();

                    if (stage == data.destroyStage.getOrDefault(pos, -1)) {
                        iter.remove();
                    }
                }
                data.destroyStage = newDestroyStage;
                if (!newDestroyStage.isEmpty()) {
                    PlayerLookup.tracking(this.level, chunkPos).forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, new CableBreakPayload(chunkPos, newDestroyStage)));
                }
            }
        }
    }

    public void onChunkLoad(LevelChunk chunk) {
        try {
            var data = chunk.getAttached(Hayo.CHUNK_ELECTRIC_DATA);
            if (data == null) {
                return;
            }

            var server = this.level.getServer();
            server.schedule(server.wrapRunnable(() -> {
                if (this.level.isLoaded(chunk.getPos().getWorldPosition())) {
                    this.deletePaths(data.electricBlocks);
                }
            }));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while updating internal state for loaded chunk", e);
        }
    }

    public void onChunkUnload(LevelChunk chunk) {
        try {
            this.tickData.remove(chunk.getPos());

            var data = chunk.getAttached(Hayo.CHUNK_ELECTRIC_DATA);
            if (data == null) {
                return;
            }
            this.deletePaths(data.electricBlocks);
        } catch (Exception e) {
            // wrapping in try-catch, otherwise whole chunk won't get saved if something errors
            LOGGER.log(Level.SEVERE, "Error while updating internal state for unloaded chunk", e);
        }
    }

    public record TransferPath(BlockPos receiver, Direction receivingFace, List<BlockPos> cables) {
    }

    public static List<TransferPath> discoverPaths(ServerLevel level, BlockPos start, @Nullable Direction sendingFace) {
        if (!level.isLoaded(start)) {
            return List.of();
        }

        record VisitedPos(int distance, Direction direction, BlockState state) {
        }
        var visited = new HashMap<BlockPos, VisitedPos>();
        visited.put(start, new VisitedPos(0, null, level.getBlockState(start)));

        var queue = new ArrayDeque<BlockPos>();
        if (sendingFace == null) {
            queue.add(start);
        } else {
            var pos = start.relative(sendingFace);
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof ElectricBlock electricBlock //
                    && electricBlock.connectsToCables(state, level, pos, sendingFace)) {
                visited.put(pos, new VisitedPos(1, sendingFace, state));
                if (state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
            }
        }

        while (!queue.isEmpty()) {
            var queuedPos = queue.removeFirst();
            var distance = visited.get(queuedPos).distance + 1;

            for (var direction : Direction.values()) {
                var pos = queuedPos.relative(direction);
                var existingEntry = visited.get(pos);
                if (existingEntry != null) {
                    if (existingEntry.distance > distance) {
                        visited.put(pos, new VisitedPos(distance, direction, existingEntry.state));
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

                visited.put(pos, new VisitedPos(distance, direction, state));
                if (state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
            }
        }

        var paths = new ArrayList<TransferPath>();
        for (var entry : visited.entrySet()) {
            var pos = entry.getKey();
            var data = entry.getValue();
            if (!pos.equals(start) //
                    && data.state.getBlock() instanceof ElectricReceiverBlock receiver //
                    && receiver.canReceiveEnergy(data.state, level, pos, data.direction) //
            ) {
                var cables = new ArrayList<BlockPos>();
                var ipos = pos.relative(data.direction.getOpposite());
                var idata = visited.get(ipos);
                while (idata.distance > 0) {
                    cables.add(ipos);
                    ipos = ipos.relative(idata.direction.getOpposite());
                    idata = visited.get(ipos);
                }

                paths.add(new TransferPath(pos, data.direction, List.copyOf(cables)));
            }
        }

        return paths;
    }

    public void deletePaths(BlockPos end) {
        var queue = new ArrayDeque<BlockPos>();
        queue.add(end);

        this.deletePathsInternal(queue);
    }

    public void deletePaths(Collection<BlockPos> ends) {
        var queue = new ArrayDeque<>(ends);

        this.deletePathsInternal(queue);
    }

    private void deletePathsInternal(ArrayDeque<BlockPos> queue) {
        var visited = new HashSet<BlockPos>();

        while (!queue.isEmpty()) {
            var queuedPos = queue.poll();

            for (var direction : Direction.values()) {
                var pos = queuedPos.relative(direction);
                if (visited.contains(pos)) {
                    continue;
                }

                if (!this.level.isLoaded(pos)) {
                    continue;
                }

                var state = this.level.getBlockState(pos);
                if (!(state.getBlock() instanceof ElectricBlock electricBlock) //
                        || !electricBlock.connectsToCables(state, this.level, pos, direction)) {
                    continue;
                }

                visited.add(pos);
                if (state.getBlock() instanceof ElectricCableBlock) {
                    queue.add(pos);
                }
                if (state.getBlock() instanceof ElectricSenderBlock) {
                    this.pathCache.remove(Pair.of(pos, null));
                    for (var side : Direction.values()) {
                        this.pathCache.remove(Pair.of(pos, side));
                    }
                }
            }
        }
    }

    // TODO: doesn't need relatively large BlockPos instances, a position inside a chunk can be represented with a single int
    public static class PersistentData {
        public static final MapCodec<PersistentData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance //
                .group(BlockPos.CODEC.listOf().fieldOf("electric_blocks").forGetter(data -> ImmutableList.copyOf(data.electricBlocks))) //
                .apply(instance, PersistentData::new));

        protected final Set<BlockPos> electricBlocks;

        public PersistentData() {
            this.electricBlocks = new HashSet<>();
        }

        public PersistentData(List<BlockPos> electricBlocks) {
            this.electricBlocks = new HashSet<>(electricBlocks);
        }
    }

    public static class TickData {
        public final Object2IntMap<BlockPos> transferPerTick = new Object2IntOpenHashMap<>();
        public final Object2IntMap<BlockPos> destroyTicks = new Object2IntOpenHashMap<>();
        public Object2IntMap<BlockPos> destroyStage = new Object2IntOpenHashMap<>();
    }
}
