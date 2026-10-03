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

public class FluidGuiRendering {
    public static MutableComponent getFluidName(Fluid fluid) {
        return fluid != Fluids.EMPTY
                ? Component.translatable(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId())
                : Component.translatable("hayo.empty");
    }

    public static MutableComponent getFluidName(Fluid fluid, int amount) {
        return amount != 0 ? getFluidName(fluid) : Component.translatable("hayo.empty");
    }

    public static List<Component> createTooltip(Fluid fluid, int amount) {
        return List.of(
                getFluidName(fluid),
                Component.translatable("hayo.millibuckets", amount).withStyle(ChatFormatting.GRAY)
        );
    }

    public static List<Component> createTooltip(Fluid fluid, int amount, int maxAmount) {
        return List.of(
                getFluidName(fluid, amount),
                Component.translatable("hayo.millibuckets_with_capacity_and_percentage",
                                amount,
                                maxAmount,
                                100 * (float) amount / (float) maxAmount
                        )
                        .withStyle(ChatFormatting.GRAY)
        );
    }

    public static List<Component> createTooltip(FluidStack stack) {
        return createTooltip(stack.fluid(), stack.amount());
    }

    public static List<Component> createTooltip(FluidStack stack, int maxAmount) {
        return createTooltip(stack.fluid(), stack.amount(), maxAmount);
    }

    public static List<Component> createTooltip(FluidIngredientAmount ingredient) {
        var fluids = ingredient.values().stream().map(Holder::value).toList();
        var amount = ingredient.amount();
        var displayFluid = fluids.isEmpty() ? Fluids.EMPTY : fluids.get((int) Math.abs(System.currentTimeMillis() / 1000 % fluids.size()));

        var tooltip = createTooltip(displayFluid, amount);
        if (fluids.size() > 1) {
            tooltip = new ArrayList<>(tooltip);

            // TODO: check if fluidIngredient.values is tag-baked (instanceof HolderSet.Named) and show tag name instead of listing fluids
            //   (like recipe-viewing mods do for item ingredients)
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("hayo.accepts_fluids").withStyle(ChatFormatting.GRAY));
            for (var fluid : fluids) {
                tooltip.add(Component.literal(" ").append(getFluidName(fluid).withStyle(ChatFormatting.GRAY)));
            }

        }
        return tooltip;
    }

    public static boolean isHoveringTank(int tankX, int tankY, double mouseX, double mouseY) {
        return mouseX >= tankX && mouseX < tankX + 18 && mouseY >= tankY && mouseY < tankY + 56;
    }

    @Environment(EnvType.CLIENT)
    public static void extractFluidTank(GuiGraphicsExtractor graphics, FluidStack stack, int maxAmount, int x, int y) {
        HayoGuiSprites.blitFluidTank(graphics, x, y);

        var fluid = stack.fluid();
        var amount = stack.amount();
        if (fluid != Fluids.EMPTY && amount != 0) {
            blitFluidColumn(graphics, fluid, amount, maxAmount, x + 1, y + 4, 48);
        }

        HayoGuiSprites.blitFluidTankOverlay(graphics, x, y);
    }


    @Environment(EnvType.CLIENT)
    public static void extractFluidTank(GuiGraphicsExtractor graphics, FluidIngredientAmount ingredient, int maxAmount, int x, int y) {
        HayoGuiSprites.blitFluidTank(graphics, x, y);

        var fluids = ingredient.values().stream().map(Holder::value).toList();
        var amount = ingredient.amount();

        var displayFluid = fluids.isEmpty() ? Fluids.EMPTY : fluids.get((int) Math.abs(System.currentTimeMillis() / 1000 % fluids.size()));
        if (displayFluid != Fluids.EMPTY && amount != 0) {
            blitFluidColumn(graphics, displayFluid, amount, maxAmount, x + 1, y + 4, 48);
        }

        HayoGuiSprites.blitFluidTankOverlay(graphics, x, y);
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
