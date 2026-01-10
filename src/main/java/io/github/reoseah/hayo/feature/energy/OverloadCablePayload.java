package io.github.reoseah.hayo.feature.energy;

import io.github.reoseah.hayo.Hayo;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;

public record OverloadCablePayload(ChunkPos chunkPos,
                                   Object2IntMap<BlockPos> destructionProgress) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, OverloadCablePayload> STREAM_CODEC = CustomPacketPayload.codec(OverloadCablePayload::write, OverloadCablePayload::read);

    public static OverloadCablePayload read(FriendlyByteBuf buffer) {
        var chunkPos = ChunkPos.STREAM_CODEC.decode(buffer);
        var destructionProgress = readBlockPosToIntMap(buffer, chunkPos);

        return new OverloadCablePayload(chunkPos, destructionProgress);
    }

    private void write(FriendlyByteBuf buf) {
        ChunkPos.STREAM_CODEC.encode(buf, this.chunkPos);
        writeBlockPosToIntMap(buf, this.chunkPos, this.destructionProgress);
    }

    private static void writeBlockPosToIntMap(FriendlyByteBuf buffer, ChunkPos chunkPos, Object2IntMap<BlockPos> values) {
        buffer.writeVarInt(values.size());

        for (var entry : values.object2IntEntrySet()) {
            var pos = entry.getKey();
            assert chunkPos.contains(pos);

            int serializedPos = (pos.getY() << 8) | ((pos.getZ() & 0b1111) << 4) | (pos.getX() & 0b1111);
            buffer.writeVarInt(serializedPos);

            buffer.writeVarInt(entry.getIntValue());
        }
    }

    private static Object2IntMap<BlockPos> readBlockPosToIntMap(FriendlyByteBuf buffer, ChunkPos chunkPos) {
        int size = buffer.readVarInt();
        var map = new Object2IntOpenHashMap<BlockPos>(size);

        for (int i = 0; i < size; i++) {
            var serializedPos = buffer.readVarInt();
            var x = serializedPos & 0b1111;
            var z = (serializedPos >> 4) & 0b1111;
            var y = serializedPos >>> 8;
            var pos = new BlockPos(chunkPos.x() * 16 + x, y, chunkPos.z() * 16 + z);

            var value = buffer.readVarInt();

            map.put(pos, value);
        }

        return map;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Hayo.CustomPayloads.OVERLOAD_CABLE;
    }
}
