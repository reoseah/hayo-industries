package io.github.reoseah.hayoind.compat.rei;

import io.github.reoseah.hayoind.client.screen.HayoMachineTexture;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class MachineArrowWidget extends WidgetWithBounds {
    public final Point point;
    public final HayoMachineTexture.RecipeArrow arrowType;
    public final int animationDuration;

    public MachineArrowWidget(Point point, HayoMachineTexture.RecipeArrow arrowType, int animationDuration) {
        this.point = point;
        this.arrowType = arrowType;
        this.animationDuration = animationDuration;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.point.x, this.point.y, 24, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HayoMachineTexture.blit(graphics, this.point.x, this.point.y, HayoMachineTexture.RecipeArrow.X, this.arrowType.y, 24, 16);

        int width = Mth.ceil((System.currentTimeMillis() / (this.animationDuration / 24) % 24d));
        HayoMachineTexture.blit(graphics, this.point.x, this.point.y, HayoMachineTexture.RecipeArrow.OVERLAY_X, this.arrowType.y, width, 16);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
