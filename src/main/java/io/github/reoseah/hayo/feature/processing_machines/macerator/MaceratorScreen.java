package io.github.reoseah.hayo.feature.processing_machines.macerator;

import io.github.reoseah.hayo.base.client.HayoMachineTexture;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import io.github.reoseah.hayo.feature.processing_machines.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MaceratorScreen extends MachineScreen {
    public MaceratorScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected HayoMachineTexture.RecipeArrow getArrow() {
        return HayoMachineTexture.RecipeArrow.MACERATOR;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        drawClassicSlots(this, graphics);
    }
}
