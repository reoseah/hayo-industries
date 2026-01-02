package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
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
        return new Rectangle(this.point.x, this.point.y, 14, 14);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HayoGuiSprites.drawMachineEnergy(graphics, this.point.x, this.point.y, 10, 14);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
