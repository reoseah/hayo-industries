package hayo;

import hayo.features.BatteryPackRenderer;
import hayo.features.EnergyArmorRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;

public class HayoClient {
    public static final ModelLayerLocation BATTERY_PACK_MODEL = new ModelLayerLocation(Hayo.modId("battery_pack"), "main");
    public static final Identifier BATTERY_PACK_TEXTURE = Hayo.modId("textures/entity/battery_pack.png");
    public static final Identifier ADVANCED_BATTERY_PACK_TEXTURE = Hayo.modId("textures/entity/advanced_battery_pack.png");

    public static final Identifier NANO_TEXTURE = Hayo.modId("textures/entity/equipment/humanoid/nano.png");
    public static final Identifier NANO_LEGGINGS_TEXTURE = Hayo.modId("textures/entity/equipment/humanoid_leggings/nano.png");
    public static final Identifier QUANTUM_TEXTURE = Hayo.modId("textures/entity/equipment/humanoid/quantum.png");
    public static final Identifier QUANTUM_LEGGINGS_TEXTURE = Hayo.modId("textures/entity/equipment/humanoid_leggings/quantum.png");
    public static final Identifier ENERGY_ARMOR_OVERLAY_TEXTURE = Hayo.modId("textures/entity/equipment/humanoid/energy_armor_overlay.png");
    public static final Identifier ENERGY_ARMOR_OVERLAY_LEGGINGS_TEXTURE = Hayo.modId("textures/entity/equipment/humanoid_leggings/energy_armor_overlay.png");

    @Environment(EnvType.CLIENT)
    public static void initialize() {
        BlockColorRegistry.register((_, level, pos, tintValues) -> tintValues.add(BiomeColors.getAverageFoliageColor(level, pos)), Hayo.Blocks.RUBBER_LEAVES);

        for (var chemfuelFluid : new FlowingFluid[]{Hayo.Fluids.CHEMFUEL, Hayo.Fluids.FLOWING_CHEMFUEL}) {
            FluidRenderingRegistry.register(chemfuelFluid, new FluidModel.Unbaked(
                    new Material(Hayo.modId("block/chemfuel_still"), false),
                    new Material(Hayo.modId("block/chemfuel_flow"), false),
                    null,
                    null
            ));
        }

        Hayo.MenuTypes.initializeClient();

        ModelLayerRegistry.registerModelLayer(BATTERY_PACK_MODEL, BatteryPackRenderer::createLayerDefinition);
        ArmorRenderer.register(ctx -> new BatteryPackRenderer(ctx, BATTERY_PACK_TEXTURE), Hayo.Items.BATTERY_PACK);
        ArmorRenderer.register(ctx -> new BatteryPackRenderer(ctx, ADVANCED_BATTERY_PACK_TEXTURE), Hayo.Items.ADVANCED_BATTERY_PACK);

        ArmorRenderer.register(ctx -> new EnergyArmorRenderer(ctx, NANO_TEXTURE, ENERGY_ARMOR_OVERLAY_TEXTURE), Hayo.Items.NANO_HELMET, Hayo.Items.NANO_CHESTPLATE, Hayo.Items.NANO_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorRenderer(ctx, NANO_LEGGINGS_TEXTURE, ENERGY_ARMOR_OVERLAY_LEGGINGS_TEXTURE), Hayo.Items.NANO_LEGGINGS);
        ArmorRenderer.register(ctx -> new EnergyArmorRenderer(ctx, QUANTUM_TEXTURE, ENERGY_ARMOR_OVERLAY_TEXTURE), Hayo.Items.QUANTUM_HELMET, Hayo.Items.QUANTUM_CHESTPLATE, Hayo.Items.QUANTUM_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorRenderer(ctx, QUANTUM_LEGGINGS_TEXTURE, ENERGY_ARMOR_OVERLAY_LEGGINGS_TEXTURE), Hayo.Items.QUANTUM_LEGGINGS);
    }
}
