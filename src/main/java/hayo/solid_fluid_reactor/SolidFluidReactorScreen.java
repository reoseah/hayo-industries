package hayo.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.energy.client.EnergyGuiSprites;
import hayo.fluid_stack.FluidGuiRendering;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

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

        FluidGuiRendering.extractFluidTank(graphics, this.menu.data.inputFluid(), SolidFluidReactorBlockEntity.FLUID_CAPACITY, this.leftPos + 34, this.topPos + 15, mouseX, mouseY);
        HayoGuiSprites.blitDrainingArrow(graphics, this.leftPos + 13, this.topPos + 36, this.menu.data.inputDrainingProgress(), this.menu.data.inputDrainingMaxProgress());

        FluidGuiRendering.extractFluidTank(graphics, this.menu.data.resultFluid(), SolidFluidReactorBlockEntity.FLUID_CAPACITY, this.leftPos + 142, this.topPos + 15, mouseX, mouseY);
        HayoGuiSprites.blitFillingArrow(graphics, this.leftPos + 164, this.topPos + 36, this.menu.data.resultFillingProgress(), this.menu.data.resultFillingMaxProgress());

        HayoGuiSprites.blitRecipeArrow(graphics, this.leftPos + 85, this.topPos + 34, HayoGuiSprites.REACTING_ARROW, HayoGuiSprites.REACTING_ARROW_OVERLAY, this.menu.data.reactingProgress(), this.menu.data.reactingMaxProgress());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
    }
}
