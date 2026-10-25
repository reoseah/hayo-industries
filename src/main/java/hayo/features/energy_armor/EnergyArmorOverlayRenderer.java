package hayo.features.energy_armor;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class EnergyArmorOverlayRenderer implements ArmorRenderer {
    private final ArmorModelSet<HumanoidModel<HumanoidRenderState>> playerArmorModels;
    private final Identifier texture;
    private final Identifier overlayTexture;

    public EnergyArmorOverlayRenderer(EntityRendererProvider.Context context, Identifier texture, Identifier overlayTexture) {
        this.playerArmorModels = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new);
        this.texture = texture;
        this.overlayTexture = overlayTexture;
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector nodeCollector, ItemStack stack, HumanoidRenderState state, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        var model = this.playerArmorModels.get(slot);
        nodeCollector.order(1).submitModel(model, state, poseStack, contextModel.renderType(this.texture), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, 0, null);

        var energy = stack.getOrDefault(EnergyComponents.ENERGY, 0);
        var threshold = stack.get(EnergyComponents.ENERGY_ARMOR).energyPerDamage();
        if (energy >= threshold) {
            nodeCollector.order(2).submitModel(model, state, poseStack, RenderTypes.eyes(this.overlayTexture), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, 0, null);
        }
    }
}
