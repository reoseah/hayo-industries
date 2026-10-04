package hayo.features.processing_machine.classic;

import hayo.Hayo;
import hayo.common.HayoGuiSprites;
import hayo.energy.client.EnergyGuiSprites;
import hayo.features.processing_machine.MachineTexts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.List;
import java.util.Optional;

public class ClassicMachineScreen extends AbstractContainerScreen<ClassicMachineMenu> {
    protected final int baseEnergyUse;
    protected final int defaultRecipeCost;
    protected final Identifier arrow;
    protected final Identifier arrowOverlay;

    public ClassicMachineScreen(ClassicMachineMenu menu, Inventory inventory, Component title, int baseEnergyUse, int defaultRecipeCost, Identifier arrow, Identifier arrowOverlay) {
        super(menu, inventory, title);
        this.baseEnergyUse = baseEnergyUse;
        this.defaultRecipeCost = defaultRecipeCost;
        this.arrow = arrow;
        this.arrowOverlay = arrowOverlay;
    }

    public static ClassicMachineScreen createElectricFurnace(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                ElectricFurnaceBlockEntity.ENERGY_USE_RATE,
                ElectricFurnaceBlockEntity.energyCostFromCookingTime(AbstractFurnaceBlockEntity.BURN_TIME_STANDARD),
                HayoGuiSprites.DEFAULT_ARROW,
                HayoGuiSprites.DEFAULT_ARROW_OVERLAY);
    }

    public static ClassicMachineScreen createMacerator(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                MaceratorBlockEntity.ENERGY_USE_RATE,
                MaceratingRecipe.DEFAULT_ENERGY,
                HayoGuiSprites.MACERATING_ARROW,
                HayoGuiSprites.MACERATING_ARROW_OVERLAY);
    }

    public static ClassicMachineScreen createCompressor(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                CompressorBlockEntity.ENERGY_USE_RATE,
                CompressingRecipe.DEFAULT_ENERGY,
                HayoGuiSprites.COMPRESSING_ARROW,
                HayoGuiSprites.COMPRESSING_ARROW_OVERLAY);
    }

    public static ClassicMachineScreen createExtractor(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                ExtractorBlockEntity.ENERGY_USE_RATE,
                ExtractingRecipe.DEFAULT_ENERGY,
                HayoGuiSprites.EXTRACTING_ARROW,
                HayoGuiSprites.EXTRACTING_ARROW_OVERLAY);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        HayoGuiSprites.blitBackground(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        HayoGuiSprites.blitSlot(graphics, this.leftPos + 46, this.topPos + 17);
        HayoGuiSprites.blitSlot(graphics, this.leftPos + 46, this.topPos + 53);
        HayoGuiSprites.blitOutputSlot(graphics, this.leftPos + 103, this.topPos + 32);
        HayoGuiSprites.blitUpgradeSlots(graphics, this.leftPos + 151, this.topPos + 7, 4);
        HayoGuiSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);

        var data = this.menu.data;
        EnergyGuiSprites.blitZap(graphics, this.leftPos + 48, this.topPos + 37, data.energy(), data.capacity());
        HayoGuiSprites.blitRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.arrow, this.arrowOverlay, data.recipeProgress(), data.recipeCost());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(48, 37, 14, 14, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(
                    this.font,
                    MachineTexts.createUpgradableMachineTooltip(this.menu.data, this.defaultRecipeCost, this.baseEnergyUse),
                    Optional.empty(),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var tooltip = super.getTooltipFromContainerItem(stack);

        if (stack.is(Hayo.ItemTags.UPGRADES)) {
            MachineTexts.addUpgradeTooltip(stack, tooltip, this.menu, this.menu.getUpgradeTag(), 3, 7);
        }

        return tooltip;
    }
}
