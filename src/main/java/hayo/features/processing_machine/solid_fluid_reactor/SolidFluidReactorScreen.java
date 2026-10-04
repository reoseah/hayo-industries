package hayo.features.processing_machine.solid_fluid_reactor;

import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.energy.client.EnergyGuiSprites;
import hayo.features.fluid_stack.FluidGuiRendering;
import hayo.features.processing_machine.MachineTexts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

import static hayo.Hayo.ItemTags.SOLID_FLUID_REACTOR_UPGRADES;
import static hayo.common.HayoGuiSprites.REACTING_ARROW;
import static hayo.common.HayoGuiSprites.REACTING_ARROW_OVERLAY;
import static hayo.features.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity.*;

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

        FluidGuiRendering.extractFluidTank(graphics, this.menu.reactorData.inputFluid(), FLUID_CAPACITY, this.leftPos + 34, this.topPos + 15);
        HayoGuiSprites.blitDrainingArrow(graphics, this.leftPos + 13, this.topPos + 36, this.menu.reactorData.inputDrainingProgress(), this.menu.reactorData.inputDrainingMaxProgress());

        FluidGuiRendering.extractFluidTank(graphics, this.menu.reactorData.resultFluid(), FLUID_CAPACITY, this.leftPos + 142, this.topPos + 15);
        HayoGuiSprites.blitFillingArrow(graphics, this.leftPos + 164, this.topPos + 36, this.menu.reactorData.resultFillingProgress(), this.menu.reactorData.resultFillingMaxProgress());

        HayoGuiSprites.blitRecipeArrow(graphics, this.leftPos + 85, this.topPos + 34, REACTING_ARROW, REACTING_ARROW_OVERLAY, this.menu.data.recipeProgress(), this.menu.data.recipeCost());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.isHovering(34, 15, 18, 56, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, FluidGuiRendering.createTooltip(this.menu.reactorData.inputFluid(), FLUID_CAPACITY), Optional.empty(), mouseX, mouseY);
        } else if (this.isHovering(142, 15, 18, 56, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, FluidGuiRendering.createTooltip(this.menu.reactorData.resultFluid(), FLUID_CAPACITY), Optional.empty(), mouseX, mouseY);
        } else if (this.isHovering(63, 36, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, MachineTexts.createUpgradableMachineTooltip(this.menu.data, 600, REACTING_ENERGY_RATE), Optional.empty(), mouseX, mouseY);
        } else {
            super.extractTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var tooltip = super.getTooltipFromContainerItem(stack);

        if (stack.is(Hayo.ItemTags.UPGRADES)) {
            MachineTexts.addUpgradeTooltip(stack, tooltip, this.menu, SOLID_FLUID_REACTOR_UPGRADES, UPGRADE_1, UPGRADE_1 + UPGRADES);
        }

        return tooltip;
    }
}
