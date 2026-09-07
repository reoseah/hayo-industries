package hayo.energy.impl;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;

public record CableDestroyProgressPayload(ChunkPos chunkPos,
                                          Object2ByteMap<BlockPos> destroyProgress) implements CustomPacketPayload {
    /// ClientLevel#destroyBlockProgress uses this to remove a crack overlay
    public static final byte CLEAR_STAGE = -1;

    public static final StreamCodec<FriendlyByteBuf, CableDestroyProgressPayload> STREAM_CODEC = CustomPacketPayload.codec(CableDestroyProgressPayload::write, CableDestroyProgressPayload::read);

    public static void receive(CableDestroyProgressPayload payload, ClientPlayNetworking.Context context) {
        var level = context.client().level;

        for (var destructionEntry : payload.destroyProgress().object2ByteEntrySet()) {
            var pos = destructionEntry.getKey();
            var value = destructionEntry.getByteValue();

            level.destroyBlockProgress(-Math.abs(pos.hashCode()), pos, value);
            if (value > 0) {
                for (int i = 0; i < 2; i++) {
                    float x = pos.getX() + 0.25F + level.getRandom().nextFloat() * 0.5F;
                    float y = pos.getY() + 0.25F + level.getRandom().nextFloat() * 0.5F;
                    float z = pos.getZ() + 0.25F + level.getRandom().nextFloat() * 0.5F;

                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
                    level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
                }
            }
        }
    }

    public static CableDestroyProgressPayload read(FriendlyByteBuf buffer) {
        var chunkPos = ChunkPos.STREAM_CODEC.decode(buffer);
        var destructionProgress = readBlockPosMap(buffer, chunkPos);

        return new CableDestroyProgressPayload(chunkPos, destructionProgress);
    }

    private void write(FriendlyByteBuf buf) {
        ChunkPos.STREAM_CODEC.encode(buf, this.chunkPos);
        writeBlockPosMap(buf, this.chunkPos, this.destroyProgress);
    }

    private static void writeBlockPosMap(FriendlyByteBuf buffer, ChunkPos chunkPos, Object2ByteMap<BlockPos> values) {
        buffer.writeVarInt(values.size());

        for (var entry : values.object2ByteEntrySet()) {
            var pos = entry.getKey();
            assert chunkPos.contains(pos);

            int serializedPos = (pos.getY() << 8) | ((pos.getZ() & 0b1111) << 4) | (pos.getX() & 0b1111);
            buffer.writeVarInt(serializedPos);

            buffer.writeByte(entry.getByteValue());
        }
    }

    private static Object2ByteMap<BlockPos> readBlockPosMap(FriendlyByteBuf buffer, ChunkPos chunkPos) {
        int size = buffer.readVarInt();
        var map = new Object2ByteOpenHashMap<BlockPos>(size);

        for (int i = 0; i < size; i++) {
            var serializedPos = buffer.readVarInt();
            var x = serializedPos & 0b1111;
            var z = (serializedPos >> 4) & 0b1111;
            var y = serializedPos >>> 8;
            var pos = new BlockPos(chunkPos.x() * 16 + x, y, chunkPos.z() * 16 + z);

            var value = buffer.readByte();

            map.put(pos, value);
        }

        return map;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return HayoEnergy.CABLE_DESTROY_PROGRESS;
    }
}
