package io.github.reoseah.hayo.feature.processing_machines.matter_generator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoContainerScreen;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class MatterGeneratorScreen extends HayoContainerScreen<MatterGeneratorMenu> {
    public static final Identifier TEXTURE = Hayo.modId("textures/gui/container/matter_generator.png");

    private int scrollOffset = 0;

    public MatterGeneratorScreen(MatterGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 192);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        HayoGuiSprites.drawSlot(graphics, this.leftPos + this.menu.slots.get(0).x - 1, this.topPos + this.menu.slots.get(0).y - 1);
        HayoGuiSprites.drawOutputSlot(graphics, this.leftPos + this.menu.slots.get(1).x - 4, this.topPos + this.menu.slots.get(1).y - 4);

        HayoGuiSprites.drawMachineEnergy(graphics, this.leftPos + 57, this.topPos + 17, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
        HayoGuiSprites.drawRecipeArrow(graphics, this.leftPos + 80, this.topPos + 25, HayoGuiSprites.RecipeArrow.DEFAULT, this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy());

        this.drawRecipeButtons(graphics, mouseX, mouseY);
    }

    protected void drawRecipeButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = this.scrollOffset; i < this.scrollOffset + 12 && i < this.menu.recipes.size(); i++) {
            int pos = i - this.scrollOffset;

            int x = this.leftPos + 9 + (pos % 8) * 18;
            int y = this.topPos + 59 + (pos / 8) * 18;

            var sprite = HayoGuiSprites.RECIPE;
            if (i == this.menu.getSelectedRecipeIdx()) {
                sprite = HayoGuiSprites.RECIPE_SELECTED;
            } else if (mouseX >= x && mouseY >= y && mouseX < x + 18 && mouseY < y + 18) {
                sprite = HayoGuiSprites.RECIPE_HIGHLIGHTED;
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18);

            var item = this.menu.recipes.get(i).value().result();
            graphics.renderItem(item, x + 1, y + 1);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int column = Mth.floor(event.x() - this.leftPos - 9) / 18;
        int row = Mth.floor(event.y() - this.topPos - 59) / 18;
        int idx = 12 * (this.scrollOffset + row) + column;

        if (idx < this.menu.recipes.size()) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, idx);

            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
