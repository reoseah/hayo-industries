package io.github.reoseah.hayo.feature.machines.matter_generator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class MatterGeneratorScreen extends AbstractContainerScreen<MatterGeneratorMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("textures/gui/container/matter_generator.png");

    public static final int RECIPE_COLUMNS = 8;
    public static final int RECIPE_ROWS = 2;
    public static final Identifier RECIPE = Hayo.modId("recipe_button/default");
    public static final Identifier RECIPE_SELECTED = Hayo.modId("recipe_button/selected");
    public static final Identifier RECIPE_HIGHLIGHTED = Hayo.modId("recipe_button/highlighted");
    public static final Identifier RECIPE_DISABLED = Hayo.modId("recipe_button/disabled");
    public static final Identifier SCROLLER = Hayo.modId("scroller/default");
    public static final Identifier SCROLLER_DISABLED = Hayo.modId("scroller/disabled");

    private static final int SCROLLER_HEIGHT = 15;
    private static final int SCROLLER_FULL_HEIGHT = 36;

    private int startIndex = 0;
    private float scrollOffset = 0;
    private boolean scrolling = false;

    public MatterGeneratorScreen(MatterGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 192);
    }

    public static void drawScroller(GuiGraphicsExtractor graphics, int x, int y, boolean disabled) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, disabled ? SCROLLER_DISABLED : SCROLLER, x, y, 12, 15);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        HayoGuiSprites.drawSlot(graphics, x + this.menu.slots.get(0).x - 1, y + this.menu.slots.get(0).y - 1);
        HayoGuiSprites.drawOutputSlot(graphics, x + this.menu.slots.get(1).x - 4, y + this.menu.slots.get(1).y - 4);

        EnergyGuiSprites.energySmall(graphics, x + 57, y + 17, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
        HayoGuiSprites.drawRecipeArrow(graphics, x + 80, y + 25, HayoGuiSprites.RecipeArrow.DEFAULT, this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy());

        this.drawRecipeButtons(graphics, mouseX, mouseY);

        this.drawScrollbar(graphics, mouseX, mouseY);
    }

    protected void drawRecipeButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (int i = this.startIndex; i < this.startIndex + 16 && i < this.menu.recipes.size(); i++) {
            int pos = i - this.startIndex;

            int x = this.leftPos + 9 + (pos % RECIPE_COLUMNS) * 18;
            int y = this.topPos + 59 + (pos / RECIPE_COLUMNS) * 18;

            var sprite = RECIPE;
            if (i == this.menu.getSelectedRecipeIdx()) {
                sprite = RECIPE_SELECTED;
            } else if (mouseX >= x && mouseY >= y && mouseX < x + 18 && mouseY < y + 18) {
                sprite = RECIPE_HIGHLIGHTED;
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18);

            var item = this.menu.recipes.get(i).value().result().create();
            graphics.fakeItem(item, x + 1, y + 1);
        }
    }

    protected void drawScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean disabled = this.menu.recipes.size() <= RECIPE_ROWS * RECIPE_COLUMNS;

        if (disabled) {
            drawScroller(graphics, this.leftPos + 156, this.topPos + 59, true);
            return;
        }

        int scrollerY = 59 + (int) (this.scrollOffset * (SCROLLER_FULL_HEIGHT - SCROLLER_HEIGHT));
        drawScroller(graphics, this.leftPos + 156, this.topPos + scrollerY, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.isHovering(57, 17, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of(EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), this.menu.getEnergyCapacity())), Optional.empty(), mouseX, mouseY);
            return;
        }
        if (this.isHovering(80, 25, 24, 16, mouseX, mouseY) && this.menu.getRecipeTotalEnergy() > 0) {
            var tooltip = List.<Component>of( //
                    EnergyTexts.amountWithCapacityAndPercentage(this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy()), //
                    EnergyTexts.durationAtAmountPerTick(this.menu.getRecipeDuration(), this.menu.getEnergyUseRate()).withStyle(ChatFormatting.GRAY) //
            );
            graphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            return;
        }

        if (this.isHovering(9, 59, 18 * RECIPE_COLUMNS, 18 * RECIPE_ROWS, mouseX, mouseY)) {
            int column = (mouseX - this.leftPos - 9) / 18;
            int row = (mouseY - this.topPos - 59) / 18;
            int idx = this.startIndex + RECIPE_COLUMNS * row + column;

            if (idx >= 0 && idx < this.menu.recipes.size()) {
                var recipe = this.menu.recipes.get(idx).value();
                graphics.setTooltipForNextFrame(this.font, List.of( //
                        recipe.result().create().getStyledHoverName(), //
                        EnergyTexts.amount(recipe.energyCost()).withStyle(ChatFormatting.GRAY) //
                ), Optional.empty(), mouseX, mouseY);
                return;
            }
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.isHovering(9, 59, 18 * RECIPE_COLUMNS, 18 * RECIPE_ROWS, event.x(), event.y())) {
            int column = Mth.floor(event.x() - this.leftPos - 9) / 18;
            int row = Mth.floor(event.y() - this.topPos - 59) / 18;
            int idx = this.startIndex + RECIPE_COLUMNS * row + column;

            if (idx >= 0 && idx < this.menu.recipes.size()) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, idx);
                return true;
            }
        }

        if (this.isScrollBarActive()) {
            if (event.x() >= this.leftPos + 156 && event.x() < this.leftPos + 156 + 12 //
                    && event.y() >= this.topPos + 59 && event.y() < this.topPos + 59 + SCROLLER_FULL_HEIGHT) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.scrolling && this.isScrollBarActive()) {
            int yscr = this.topPos + 59;
            int yscr2 = yscr + SCROLLER_FULL_HEIGHT;
            this.scrollOffset = ((float) event.y() - yscr - 7.5F) / (yscr2 - yscr - 15.0F);
            this.scrollOffset = Mth.clamp(this.scrollOffset, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffset * this.getOffscreenRows() + 0.5) * RECIPE_COLUMNS;
            return true;
        } else {
            return super.mouseDragged(event, dx, dy);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (super.mouseScrolled(x, y, scrollX, scrollY)) {
            return true;
        }
        if (this.isScrollBarActive()) {
            int offscreenRows = this.getOffscreenRows();
            float scrolledDelta = (float) scrollY / offscreenRows;
            this.scrollOffset = Mth.clamp(this.scrollOffset - scrolledDelta, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffset * offscreenRows + 0.5) * RECIPE_COLUMNS;
        }

        return true;
    }


    private boolean isScrollBarActive() {
        return this.menu.recipes.size() > RECIPE_COLUMNS * RECIPE_ROWS;
    }

    protected int getOffscreenRows() {
        return (this.menu.recipes.size() + RECIPE_COLUMNS - 1) / RECIPE_COLUMNS - RECIPE_ROWS;
    }
}
