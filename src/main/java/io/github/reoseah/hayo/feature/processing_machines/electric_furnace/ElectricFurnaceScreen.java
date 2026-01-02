package io.github.reoseah.hayo.feature.processing_machines.electric_furnace;

import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import io.github.reoseah.hayo.feature.processing_machines.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends MachineScreen {
    public ElectricFurnaceScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected HayoGuiSprites.RecipeArrow getArrow() {
        return HayoGuiSprites.RecipeArrow.DEFAULT;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        drawClassicSlots(this, graphics);
    }
}
