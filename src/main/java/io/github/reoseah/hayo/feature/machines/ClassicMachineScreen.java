package io.github.reoseah.hayo.feature.machines;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyGuiSprites;
import io.github.reoseah.hayo.feature.electric_blocks.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ClassicMachineScreen extends AbstractContainerScreen<MachineMenu> {
    public static final Identifier BACKGROUND = Hayo.modId("textures/gui/container/machine.png");

    protected final HayoGuiSprites.RecipeArrow arrow;

    public ClassicMachineScreen(MachineMenu menu, Inventory inventory, Component title, HayoGuiSprites.RecipeArrow arrow) {
        super(menu, inventory, title);
        this.arrow = arrow;
    }

    public static MenuScreens.ScreenConstructor<MachineMenu, ClassicMachineScreen> withArrow(HayoGuiSprites.RecipeArrow arrowType) {
        return (menu, inventory, title) -> new ClassicMachineScreen(menu, inventory, title, arrowType);
    }

    protected static void drawClassicSlots(ClassicMachineScreen screen, GuiGraphicsExtractor graphics) {
        for (var slot : screen.menu.slots.subList(0, 2)) {
            HayoGuiSprites.drawSlot(graphics, screen.leftPos + slot.x - 1, screen.topPos + slot.y - 1);
        }

        HayoGuiSprites.drawOutputSlot(graphics, screen.leftPos + screen.menu.slots.get(2).x - 4, screen.topPos + screen.menu.slots.get(2).y - 4);

        for (var slot : screen.menu.slots.subList(3, 7)) {
            HayoGuiSprites.drawUpgradeSlot(graphics, screen.leftPos + slot.x - 1, screen.topPos + slot.y - 1);
        }
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, ClassicMachineScreen.BACKGROUND, this.leftPos, this.topPos, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);

        drawClassicSlots(this, graphics);
        EnergyGuiSprites.energySmall(graphics, this.leftPos + 48, this.topPos + 37, this.menu.getStoredEnergy(), this.menu.getEnergyCapacity());
        HayoGuiSprites.drawRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.arrow, this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.isHovering(48, 37, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.amountAndCapacity(this.menu.getStoredEnergy(), this.menu.getEnergyCapacity()), //
                    Component.translatable("hayo.machine.crafting_speed", this.menu.getEnergyUseRate()).withStyle(ChatFormatting.GRAY)
            ), Optional.empty(), mouseX, mouseY);
            return;
        }
        if (this.isHovering(70, 36, 24, 16, mouseX, mouseY) && this.menu.getRecipeTotalEnergy() > 0) {
            graphics.setTooltipForNextFrame(this.font, List.of( //
                    EnergyTexts.amountWithCapacityAndPercentage(this.menu.getRecipeUsedEnergy(), this.menu.getRecipeTotalEnergy()), //
                    EnergyTexts.durationAtAmountPerTick(this.menu.getRecipeDuration(), this.menu.getEnergyUseRate()).withStyle(ChatFormatting.GRAY) //
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var tooltip = super.getTooltipFromContainerItem(stack);

        // TODO: un-hardcode, use the item tags that BEs have to derive this
        if ((stack.is(Hayo.Items.BLASTING_UPGRADE) || stack.is(Hayo.Items.SMOKING_UPGRADE)) //
                && this.menu.getType() != Hayo.MenuTypes.ELECTRIC_FURNACE) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("hayo.machine.not_compatible_uprade").withStyle(ChatFormatting.RED));
            return tooltip;
        } else if (stack.is(Hayo.Items.BLASTING_UPGRADE) || stack.is(Hayo.Items.SMOKING_UPGRADE)) {
            boolean hoveringInstalledUpgrade = false;
            boolean similarUpgradeInstalled = false;

            for (var upgradeSlot = 3; upgradeSlot <= 7; upgradeSlot++) {
                var machineUpgrade = this.menu.getSlot(upgradeSlot).getItem();
                if (stack == machineUpgrade) {
                    hoveringInstalledUpgrade = true;
                    break;
                }

                if (machineUpgrade.is(Hayo.Items.BLASTING_UPGRADE) || machineUpgrade.is(Hayo.Items.SMOKING_UPGRADE)) {
                    similarUpgradeInstalled = true;
                }
            }

            if (!hoveringInstalledUpgrade && similarUpgradeInstalled) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("hayo.machine.conflicts_with_installed_upgrade").withStyle(ChatFormatting.RED));
            }
        }
        return tooltip;
    }

}
