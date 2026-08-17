package hayo.energy.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class HayoEnergy {
    public static final String MOD_ID = "hayoenergy";

    public static final AttachmentType<EnergyGridImpl> ENERGY_GRID = AttachmentRegistry.create(modId("energy_grid"));

    public static final AttachmentType<EnergyGridImpl.PersistentData> ENERGY_GRID_CHUNK = AttachmentRegistry.create(
            modId("energy_grid_chunk"),
            builder -> builder
                    .initializer(EnergyGridImpl.PersistentData::new)
                    .persistent(EnergyGridImpl.PersistentData.CODEC.codec()));

    public static final CustomPacketPayload.Type<CableDestroyProgressPayload> CABLE_DESTROY_PROGRESS = new CustomPacketPayload.Type<>(modId("cable_destroy_progress"));

    public static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void initialize() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            if (generated) {
                return;
            }
            EnergyGridImpl.get(level).onChunkLoad(chunk);
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            EnergyGridImpl.get(level).onChunkUnload(chunk);
        });
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            EnergyGridImpl.get(level).onLevelTickEnd();
        });

        PayloadTypeRegistry.clientboundPlay().register(CABLE_DESTROY_PROGRESS, CableDestroyProgressPayload.STREAM_CODEC);
    }

    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CABLE_DESTROY_PROGRESS, CableDestroyProgressPayload::receive);
    }
}
