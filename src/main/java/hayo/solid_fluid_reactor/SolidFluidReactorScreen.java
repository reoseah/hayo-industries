package hayo.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.energy.client.EnergyGuiSprites;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;

public class SolidFluidReactorScreen extends AbstractContainerScreen<SolidFluidReactorMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("textures/gui/container/solid_fluid_reactor.png");

    public SolidFluidReactorScreen(SolidFluidReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 230, 166);
    }

    @Override
    public void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 5;
        this.inventoryLabelX = 35;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        EnergyGuiSprites.blitZap(graphics, this.leftPos + 63, this.topPos + 36, this.menu.data.energy(), this.menu.data.capacity());

        var inputFluid = this.menu.data.inputFluid();
        this.extractFluidColumn(graphics, mouseX, mouseY, inputFluid.fluid(), inputFluid.amount(), 34, 15);
        HayoGuiSprites.blitDrainingArrow(graphics, this.leftPos + 13, this.topPos + 36, this.menu.data.inputDrainingProgress(), this.menu.data.inputDrainingMaxProgress());

        var resultFluid = this.menu.data.resultFluid();
        this.extractFluidColumn(graphics, mouseX, mouseY, resultFluid.fluid(), resultFluid.amount(), 142, 15);
        HayoGuiSprites.blitFillingArrow(graphics, this.leftPos + 164, this.topPos + 36, this.menu.data.resultFillingProgress(), this.menu.data.resultFillingMaxProgress());

        HayoGuiSprites.blitRecipeArrow(graphics, this.leftPos + 85, this.topPos + 34, HayoGuiSprites.REACTING_ARROW, HayoGuiSprites.REACTING_ARROW_OVERLAY, this.menu.data.reactingProgress(), this.menu.data.reactingMaxProgress());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    private void extractFluidColumn(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Fluid fluid, int fluidAmount, int x, int y) {
        boolean isEmpty = fluid == Fluids.EMPTY || fluidAmount == 0;
        if (!isEmpty) {
            extractFluidColumn(graphics, fluid, fluidAmount, SolidFluidReactorBlockEntity.FLUID_CAPACITY, this.leftPos + x + 1, this.topPos + y + 4, 48);
        }

        HayoGuiSprites.blitFluidOverlay(graphics, this.leftPos + x, this.topPos + y);

        if (mouseX >= this.leftPos + x && mouseX < this.leftPos + x + 18 && mouseY >= this.topPos + y && mouseY < this.topPos + y + 56) {
            graphics.setTooltipForNextFrame(this.font, List.of(
                    isEmpty
                            ? Component.translatable("hayo.empty")
                            : Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()),
                    Component.translatable("hayo.millibuckets_with_capacity_and_percentage",
                            fluidAmount,
                            SolidFluidReactorBlockEntity.FLUID_CAPACITY,
                            100 * (float) fluidAmount / (float) SolidFluidReactorBlockEntity.FLUID_CAPACITY
                    ).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
        }
    }

    private static void extractFluidColumn(GuiGraphicsExtractor graphics, Fluid fluid, int amount, int capacity, int x, int y, int height) {
        var minecraft = Minecraft.getInstance();

        var fluidModel = minecraft.getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        var sprite = fluidModel.stillMaterial().sprite();
        int color = fluidModel.tintSource() != null
                ? fluidModel.tintSource().colorInWorld(fluid.defaultFluidState().createLegacyBlock(), minecraft.level, minecraft.player.blockPosition())
                : 0xFFFFFFFF;

        int fluidHeight = Math.clamp(amount * height / capacity, 1, height);

        int tiles = fluidHeight / 16;
        for (int i = 0; i < tiles; i++) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y + height - i * 16 - 16, 16, 16, color);
        }

        int remainder = fluidHeight - 16 * tiles;
        if (remainder != 0) {
            graphics.enableScissor(x, y + height - tiles * 16 - remainder, x + 16, y + height - tiles * 16);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y + height - (tiles + 1) * 16, 16, 16, color);
            graphics.disableScissor();
        }
    }
}
