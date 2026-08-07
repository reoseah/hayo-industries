package io.github.reoseah.hayo.feature.quantum_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.reoseah.hayo.Hayo;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import static io.github.reoseah.hayo.Hayo.QUANTUM_ARMOR;

public class QuantumArmorRenderer implements ArmorRenderer {
    private static final Identifier REGULAR_TEXTURE = Hayo.modId("textures/entity/equipment/humanoid/quantum.png");
    private static final Identifier GLOW_TEXTURE = Hayo.modId("textures/entity/quantum_glow.png");

    private final HumanoidModel<HumanoidRenderState> armorModel;
    private final HumanoidModel<HumanoidRenderState> glowModel;

    public QuantumArmorRenderer(EntityRendererProvider.Context context) {
        this.armorModel = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new).get(EquipmentSlot.CHEST);
        this.glowModel = new HumanoidModel<>(context.bakeLayer(QUANTUM_ARMOR));
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector nodeCollector, ItemStack stack, HumanoidRenderState state, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        RenderType renderType = contextModel.renderType(REGULAR_TEXTURE);
        nodeCollector.submitModel(this.armorModel, state, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, 0, null);

        var outlineLayer = getQuantumGlowRenderType(GLOW_TEXTURE);
        ArmorRenderer.submitTransformCopyingModel(
                contextModel, //
                state, //
                this.glowModel, //
                state, //
                true, //
                nodeCollector, //
                poseStack, //
                outlineLayer, //
                light, //
                OverlayTexture.NO_OVERLAY, //
                0xFFFFFFFF, //
                null, //
                0, //
                null //
        );
    }

    public static RenderType getQuantumGlowRenderType(Identifier texture) {
        var state = RenderSetup.builder(RenderPipelines.EYES) //
                .withTexture("Sampler0", texture) //
                .useOverlay() //
                .createRenderSetup();

        return RenderType.create("hayo_quantum_armor_glow", state);
    }

    public static class QuantumGlowModel {
        public static LayerDefinition createLayerDefinition() {
            var mesh = createInvertedMesh(new CubeDeformation(-2));
            return LayerDefinition.create(mesh, 64, 32);
        }

        public static MeshDefinition createInvertedMesh(CubeDeformation deformation) {
            var mesh = new MeshDefinition();
            var root = mesh.getRoot();

            var head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 16).addBox(4, 0, 4, -8, -8, -8, deformation), PartPose.offset(0, 0, 0));
            head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, deformation.extend(0.5F)), PartPose.ZERO);

            root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 32).addBox(4, 12, 2, -8, -12, -4, deformation), PartPose.offset(0, 0, 0));
            root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 32).addBox(1, 10, 2, -4, -12, -4, deformation), PartPose.offset(-5, 2, 0));
            root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 32).mirror().addBox(3, 10, 2, -4, -12, -4, deformation), PartPose.offset(5, 2, 0));
            root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 32).addBox(2, 12, 2, -4, -12, -4, deformation), PartPose.offset(-1.9F, 12, 0));
            root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 32).mirror().addBox(2, 12, 2, -4, -12, -4, deformation), PartPose.offset(1.9F, 12, 0));

            return mesh;
        }
    }
}
