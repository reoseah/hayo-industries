package io.github.reoseah.hayo.feature.processing_machines.extractor;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtractorScreen extends ClassicMachineScreen {
    public ExtractorScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected HayoGuiSprites.RecipeArrow getArrow() {
        return HayoGuiSprites.RecipeArrow.EXTRACTOR;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        drawClassicSlots(this, graphics); // FIXME: draw slots with secondary result
    }
}
