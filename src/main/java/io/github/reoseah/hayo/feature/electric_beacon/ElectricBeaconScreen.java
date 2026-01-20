package io.github.reoseah.hayo.feature.electric_beacon;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class ElectricBeaconScreen extends HayoContainerScreen<ElectricBeaconMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("textures/gui/container/electric_beacon.png");

    public ElectricBeaconScreen(ElectricBeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 194, 199);
        this.inventoryLabelX = 17;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        HayoGuiSprites.drawSlot(graphics, this.leftPos + this.menu.slots.getFirst().x - 1, this.topPos + this.menu.slots.getFirst().y - 1);
        HayoGuiSprites.drawMachineEnergy(graphics, this.leftPos + 170, this.topPos + 68, this.menu.getStoredEnergy(), 10_000);

        int levels = this.menu.getBeaconLevels();
//        System.out.println(levels);
        for (int tier = 0; tier < 4; tier++) {
            int x = this.leftPos + 34;
            int y = this.topPos + 17 + 22 * tier;

            var selectedOption = this.menu.getOption(tier);
            var options = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(tier, List.of());
            for (var option : options) {
                var sprite = HayoGuiSprites.RECIPE;
                if (tier >= levels) {
                    sprite = option == options.getFirst() ? HayoGuiSprites.RECIPE_SELECTED : HayoGuiSprites.RECIPE_DISABLED;
                } else if (option == selectedOption) {
                    sprite = HayoGuiSprites.RECIPE_SELECTED;
                } else if (mouseX >= x && mouseY >= y && mouseX < x + 18 && mouseY < y + 18) {
                    sprite = HayoGuiSprites.RECIPE_HIGHLIGHTED;
                }
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, option.sprite(), x + 1, y + 1, 16, 16);

                x += 18;
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovering(170, 68, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of(EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), 10000)), Optional.empty(), mouseX, mouseY);
            return;
        }
        if (mouseX > this.leftPos + 34) {
            for (int tier = 0; tier < 4; tier++) {
                int y = this.topPos + 17 + 22 * tier;
                if (mouseY > y && mouseY < y + 18) {
                    int column = (mouseX - this.leftPos - 34) / 18;
                    var tierOptions = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(tier, List.of());
                    if (column >= 0 && column < tierOptions.size()) {
                        var option = tierOptions.get(column);
                        graphics.setTooltipForNextFrame(this.font, option.tooltip(), Optional.empty(), mouseX, mouseY);
                        return;
                    }
                }
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        if (mouseX > this.leftPos + 34) {
            for (int tier = 0; tier < 4; tier++) {
                int y = this.topPos + 17 + 22 * tier;
                if (mouseY > y && mouseY < y + 18) {
                    int column = (mouseX - this.leftPos - 34) / 18;
                    var tierOptions = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(tier, List.of());
                    if (column >= 0 && column < tierOptions.size()) {
                        var option = tierOptions.get(column);

                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ElectricBeacon.OPTIONS.indexOf(option));

                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}
