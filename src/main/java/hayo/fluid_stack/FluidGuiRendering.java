package hayo.fluid_stack;

import hayo.common.HayoGuiSprites;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidGuiRendering {
    @Environment(EnvType.CLIENT)
    public static void extractFluidTank(GuiGraphicsExtractor graphics, FluidStack fluidStack, int maxAmount, int x, int y, int mouseX, int mouseY) {
        var fluid = fluidStack.fluid();
        var amount = fluidStack.amount();

        HayoGuiSprites.blitFluidTank(graphics, x, y);
        if (fluid != Fluids.EMPTY && amount != 0) {
            blitFluidColumn(graphics, fluid, amount, maxAmount, x + 1, y + 4, 48);
        }
        HayoGuiSprites.blitFluidTankOverlay(graphics, x, y);

        if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 56) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, createTankTooltip(fluid, amount, maxAmount), Optional.empty(), mouseX, mouseY);
        }
    }

    public static List<Component> createTankTooltip(Fluid fluid, int amount, int capacity) {
        return List.of(
                fluid != Fluids.EMPTY && amount != 0
                        ? getFluidName(fluid)
                        : Component.translatable("hayo.empty"),
                Component.translatable("hayo.millibuckets_with_capacity_and_percentage",
                        amount,
                        capacity,
                        capacity != 0 ? 100 * (float) amount / (float) capacity : Float.NaN
                ).withStyle(ChatFormatting.GRAY)
        );
    }

    public static MutableComponent getFluidName(Fluid fluid) {
        return Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId());
    }

    @Environment(EnvType.CLIENT)
    public static void extractFluidTank(GuiGraphicsExtractor graphics, FluidIngredientAmount fluidIngredient, int maxAmount, int x, int y, int mouseX, int mouseY) {
        var fluids = fluidIngredient.values().stream().map(Holder::value).toList();
        var amount = fluidIngredient.amount();

        var displayFluid = fluids.isEmpty() ? Fluids.EMPTY : fluids.get((int) Math.abs(System.currentTimeMillis() / 1000 % fluids.size()));

        HayoGuiSprites.blitFluidTank(graphics, x, y);
        if (displayFluid != Fluids.EMPTY && amount != 0) {
            blitFluidColumn(graphics, displayFluid, amount, maxAmount, x + 1, y + 4, 48);
        }
        HayoGuiSprites.blitFluidTankOverlay(graphics, x, y);

        if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 56) {
            var displayFluidTooltip = createTankTooltip(displayFluid, amount, maxAmount);
            if (fluids.size() <= 1) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, displayFluidTooltip, Optional.empty(), mouseX, mouseY);
            } else {
                var tooltip = new ArrayList<>(displayFluidTooltip);
                tooltip.add(Component.empty());

                // TODO: check if fluidIngredient.values is tag-baked (instanceof HolderSet.Named) and show tag name instead of listing fluids
                //   (like recipe-viewing mods do for item ingredients)
                tooltip.add(Component.translatable("hayo.accepts_fluids").withStyle(ChatFormatting.GRAY));
                for (var fluid : fluids) {
                    tooltip.add(Component.literal(" ").append(getFluidName(fluid).withStyle(ChatFormatting.GRAY)));
                }

                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static void blitFluidColumn(GuiGraphicsExtractor graphics, Fluid fluid, int amount, int maxAmount, int x, int y, int height) {
        var minecraft = Minecraft.getInstance();

        var fluidModel = minecraft.getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        var sprite = fluidModel.stillMaterial().sprite();
        int color = fluidModel.tintSource() != null
                ? fluidModel.tintSource().colorInWorld(fluid.defaultFluidState().createLegacyBlock(), minecraft.level, minecraft.player.blockPosition())
                : 0xFFFFFFFF;

        int fluidHeight = Math.clamp((long) amount * height / maxAmount, 1, height);

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
