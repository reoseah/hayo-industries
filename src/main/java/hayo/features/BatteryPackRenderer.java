package hayo.features;

import com.mojang.blaze3d.vertex.PoseStack;
import hayo.HayoClient;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public record BatteryPackRenderer(HumanoidModel<HumanoidRenderState> model,
                                  Identifier texture) implements ArmorRenderer {
    public BatteryPackRenderer(EntityRendererProvider.Context context, Identifier texture) {
        this(new HumanoidModel<>(context.bakeLayer(HayoClient.BATTERY_PACK_MODEL)), texture);
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector nodeCollector, ItemStack stack, HumanoidRenderState state, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        var renderType = contextModel.renderType(this.texture);
        nodeCollector.submitModel(this.model, state, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null, 0, null);
    }

    public static LayerDefinition createLayerDefinition() {
        var mesh = createMesh(new CubeDeformation(0));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static MeshDefinition createMesh(CubeDeformation deformation) {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();

        var head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4, 0, 2, 8, 10, 4, deformation), PartPose.offset(0, 0, 0));

        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        return mesh;
    }
}
