package io.github.reoseah.hayo.feature.rei_support;

import io.github.reoseah.hayo.base.client.HayoMachineTexture;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

public class MachineEnergyWidget extends WidgetWithBounds {
    private final Point point;

    public MachineEnergyWidget(Point point) {
        this.point = point;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.point.x, this.point.y, HayoMachineTexture.ZAP_SIZE, HayoMachineTexture.ZAP_SIZE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HayoMachineTexture.blit(graphics, this.point.x, this.point.y, HayoMachineTexture.ZAP_X, HayoMachineTexture.ZAP_Y, HayoMachineTexture.ZAP_SIZE, HayoMachineTexture.ZAP_SIZE);
        int height = 4;
        HayoMachineTexture.blit(graphics, this.point.x, this.point.y + height, HayoMachineTexture.ZAP_OVERLAY_X, HayoMachineTexture.ZAP_Y + height, HayoMachineTexture.ZAP_SIZE, HayoMachineTexture.ZAP_SIZE - height);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
