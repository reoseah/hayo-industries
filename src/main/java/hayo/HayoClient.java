package hayo;

import hayo.energy_armor.EnergyArmorOverlayRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.renderer.BiomeColors;

public class HayoClient {
    @Environment(EnvType.CLIENT)
    public static void initialize() {
        BlockColorRegistry.register((_, level, pos, tintValues) -> tintValues.add(level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : 0xff48b518), Hayo.Blocks.RUBBER_LEAVES);

        Hayo.MenuTypes.initializeClient();

        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.NANO, EnergyArmorOverlayRenderer.QUANTUM_OVERLAY), Hayo.Items.NANO_HELMET, Hayo.Items.NANO_CHESTPLATE, Hayo.Items.NANO_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.LEGS_NANO, EnergyArmorOverlayRenderer.LEGS_QUANTUM_OVERLAY), Hayo.Items.NANO_LEGGINGS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.QUANTUM, EnergyArmorOverlayRenderer.QUANTUM_OVERLAY), Hayo.Items.QUANTUM_HELMET, Hayo.Items.QUANTUM_CHESTPLATE, Hayo.Items.QUANTUM_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.LEGS_QUANTUM, EnergyArmorOverlayRenderer.LEGS_QUANTUM_OVERLAY), Hayo.Items.QUANTUM_LEGGINGS);
    }
}
