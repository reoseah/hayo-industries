package hayo.energy_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import hayo.Hayo;
import hayo.energy.item.EnergyComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class EnergyArmorOverlayRenderer implements ArmorRenderer {
    public static final Identifier NANO = Hayo.modId("textures/entity/equipment/humanoid/nano.png");
    public static final Identifier LEGS_NANO = Hayo.modId("textures/entity/equipment/humanoid_leggings/nano.png");

    public static final Identifier QUANTUM = Hayo.modId("textures/entity/equipment/humanoid/quantum.png");
    public static final Identifier QUANTUM_OVERLAY = Hayo.modId("textures/entity/equipment/humanoid/quantum_emissive.png");

    public static final Identifier LEGS_QUANTUM = Hayo.modId("textures/entity/equipment/humanoid_leggings/quantum.png");
    public static final Identifier LEGS_QUANTUM_OVERLAY = Hayo.modId("textures/entity/equipment/humanoid_leggings/quantum_emissive.png");

    private final ArmorModelSet<HumanoidModel<HumanoidRenderState>> armorModel;
    private final Identifier texture;
    private final RenderType overlayRenderType;

    public EnergyArmorOverlayRenderer(EntityRendererProvider.Context context, Identifier texture, Identifier overlayTexture) {
        this.armorModel = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new);
        this.texture = texture;
        this.overlayRenderType = RenderTypes.eyes(overlayTexture);
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector nodeCollector, ItemStack stack, HumanoidRenderState state, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        var model = this.armorModel.get(slot);
        nodeCollector.order(1).submitModel(model, state, poseStack, contextModel.renderType(this.texture), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, 0, null);

        var threshold = stack.get(EnergyComponents.ENERGY_ARMOR).energyPerDamage();
        var energy = stack.getOrDefault(EnergyComponents.ENERGY, 0);
        if (energy >= threshold) {
            nodeCollector.order(2).submitModel(model, state, poseStack, this.overlayRenderType, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, 0, null);
        }
    }
}
