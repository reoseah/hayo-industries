package io.github.reoseah.hayo.feature.processing_machines.compressor;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompressorScreen extends ClassicMachineScreen {
    public CompressorScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected HayoGuiSprites.RecipeArrow getArrow() {
        return HayoGuiSprites.RecipeArrow.COMPRESSOR;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        drawClassicSlots(this, graphics);
    }
}
