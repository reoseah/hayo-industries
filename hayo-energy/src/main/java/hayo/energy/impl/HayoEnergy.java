package hayo.energy.impl;

import hayo.energy.EnergyPreservingShapedRecipe;
import hayo.energy.client.EnergyModelProperty;
import hayo.energy.item.EnergyComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;

public class HayoEnergy {
    public static final String MOD_ID = "hayoenergy";
    public static final String MAIN_MOD_ID = "hayo";

    public static final AttachmentType<EnergyGrid> ENERGY_GRID = AttachmentRegistry.create(modId("energy_grid"));

    public static final AttachmentType<EnergyGrid.PersistentData> ENERGY_GRID_CHUNK = AttachmentRegistry.create(
            modId("energy_grid_chunk"),
            builder -> builder
                    .initializer(EnergyGrid.PersistentData::new)
                    .persistent(EnergyGrid.PersistentData.CODEC.codec()));

    public static final CustomPacketPayload.Type<CableDestroyProgressPayload> CABLE_DESTROY_PROGRESS = new CustomPacketPayload.Type<>(modId("cable_destroy_progress"));

    public static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(MAIN_MOD_ID, path);
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("stores_energy"), EnergyComponents.STORES_ENERGY);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("energy"), EnergyComponents.ENERGY);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("charges_blocks"), EnergyComponents.CHARGES_BLOCKS);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("charged_attributes"), EnergyComponents.ATTRIBUTES_WHEN_CHARGED);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("energy_tool"), EnergyComponents.ENERGY_TOOL);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("energy_armor"), EnergyComponents.ENERGY_ARMOR);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("charges_inventory"), EnergyComponents.CHARGES_INVENTORY);

        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, modId("energy_preserving_crafting"), EnergyPreservingShapedRecipe.SERIALIZER);

        PayloadTypeRegistry.clientboundPlay().register(CABLE_DESTROY_PROGRESS, CableDestroyProgressPayload.STREAM_CODEC);

        RecipeSynchronization.synchronizeRecipeSerializer(EnergyPreservingShapedRecipe.SERIALIZER);

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            if (generated) {
                return;
            }
            EnergyGrid.getOrCreate(level).onChunkLoad(chunk);
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            EnergyGrid.getOrCreate(level).onChunkUnload(chunk);
        });
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            EnergyGrid.getOrCreate(level).onLevelTickEnd();

            tickPlayerInventories(level);
        });
    }

    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CABLE_DESTROY_PROGRESS, CableDestroyProgressPayload::receive);

        RangeSelectItemModelProperties.ID_MAPPER.put(modId("energy"), EnergyModelProperty.MAP_CODEC);
    }

    public static void tickPlayerInventories(ServerLevel level) {
        for (var player : level.players()) {
            var chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.has(EnergyComponents.CHARGES_INVENTORY)) {
                var stats = chest.get(EnergyComponents.STORES_ENERGY);
                if (stats == null) continue;

                var limit = stats.transferLimit();
                if (limit == 0) continue;

                var energy = EnergyComponents.getEnergy(chest);
                if (energy == 0) continue;

                int moved = EnergyComponents.spreadEnergy(player, Math.min(energy, limit), EquipmentSlot.CHEST);
                EnergyComponents.setEnergy(chest, energy - moved);

                player.getInventory().setChanged();
                if (player.isCreative()) {
                    player.inventoryMenu.broadcastChanges();
                }
            }
        }
    }
}
