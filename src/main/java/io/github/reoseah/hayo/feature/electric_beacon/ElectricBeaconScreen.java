package io.github.reoseah.hayo.feature.electric_beacon;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class ElectricBeaconScreen extends HayoContainerScreen<ElectricBeaconMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("textures/gui/container/electric_beacon.png");

    public ElectricBeaconScreen(ElectricBeaconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 194, 198);
        this.inventoryLabelX = 17;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        for (int tier = 0; tier < 4; tier++) {
            int x = this.leftPos + 34;
            int y = this.topPos + 17 + 22 * tier;

            var selectedOption = this.menu.getOption(tier);
            for (var option : ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(tier, List.of())) {
                var sprite = HayoGuiSprites.RECIPE;
                if (option == selectedOption.orElse(null)) {
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
        if (mouseX > this.leftPos  + 34) {
            for (int tier = 0; tier < 4; tier++) {
                int y = this.topPos + 17 + 22 * tier;
                if (mouseY > y && mouseY < y + 18) {
                    int column = (mouseX - this.leftPos - 34) / 18;
                    var tierOptions = ElectricBeacon.OPTIONS_BY_TIER.getOrDefault(tier, List.of());
                    if (column >= 0 && column < tierOptions.size()) {
                        var option = tierOptions.get(column);
                        graphics.setTooltipForNextFrame(this.font, List.of(option.name()), Optional.empty(), mouseX, mouseY);
                        return;
                    }
                }
            }
        }
    }
}
