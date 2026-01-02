package io.github.reoseah.hayo.mod_support.roughlyenoughitems;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

import java.util.List;

public class MachineArrowWidget extends WidgetWithBounds {
    public final Point point;
    public final HayoGuiSprites.RecipeArrow arrowType;
    public final int animationDuration;

    public MachineArrowWidget(Point point, HayoGuiSprites.RecipeArrow arrowType, int animationDuration) {
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
        int width = Mth.ceil((System.currentTimeMillis() / (this.animationDuration / 24) % 24d));
        HayoGuiSprites.drawRecipeArrow(graphics, this.point.x, this.point.y, this.arrowType, width, 24);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
