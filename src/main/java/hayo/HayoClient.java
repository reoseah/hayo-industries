package hayo;

import hayo.energy_armor.EnergyArmorOverlayRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;

public class HayoClient {
    @Environment(EnvType.CLIENT)
    public static void initialize() {
        BlockColorRegistry.register((_, level, pos, tintValues) -> tintValues.add(BiomeColors.getAverageFoliageColor(level, pos)), Hayo.Blocks.RUBBER_LEAVES);

        Hayo.MenuTypes.initializeClient();

        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.NANO, EnergyArmorOverlayRenderer.QUANTUM_OVERLAY), Hayo.Items.NANO_HELMET, Hayo.Items.NANO_CHESTPLATE, Hayo.Items.NANO_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.LEGS_NANO, EnergyArmorOverlayRenderer.LEGS_QUANTUM_OVERLAY), Hayo.Items.NANO_LEGGINGS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.QUANTUM, EnergyArmorOverlayRenderer.QUANTUM_OVERLAY), Hayo.Items.QUANTUM_HELMET, Hayo.Items.QUANTUM_CHESTPLATE, Hayo.Items.QUANTUM_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.LEGS_QUANTUM, EnergyArmorOverlayRenderer.LEGS_QUANTUM_OVERLAY), Hayo.Items.QUANTUM_LEGGINGS);

        for (var chemfuelFluid : new FlowingFluid[]{Hayo.Fluids.CHEMFUEL, Hayo.Fluids.FLOWING_CHEMFUEL}) {
            FluidRenderingRegistry.register(chemfuelFluid, new FluidModel.Unbaked(
                    new Material(Hayo.modId("block/chemfuel_still"), false),
                    new Material(Hayo.modId("block/chemfuel_flow"), false),
                    null,
                   null
            ));
        }
    }
}
