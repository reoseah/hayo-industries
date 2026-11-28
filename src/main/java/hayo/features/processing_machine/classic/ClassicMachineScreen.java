package hayo.features.processing_machine.classic;

import hayo.Hayo;
import hayo.common.HayoSprites;
import hayo.common.IntRange;
import hayo.energy.client.HayoEnergySprites;
import hayo.features.processing_machine.MachineTexts;
import hayo.features.processing_machine.SlotAccessComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static hayo.features.processing_machine.classic.ClassicMachineBlockEntity.UPGRADES;
import static hayo.features.processing_machine.classic.ClassicMachineBlockEntity.UPGRADE_1;

public class ClassicMachineScreen extends AbstractContainerScreen<ClassicMachineMenu> {
    protected final int baseEnergyUse;
    protected final int defaultRecipeCost;
    protected final Identifier arrow;
    protected final Identifier arrowOverlay;

    protected @Nullable SlotAccessComponent<?> slotAccess;

    public ClassicMachineScreen(ClassicMachineMenu menu, Inventory inventory, Component title, int baseEnergyUse, int defaultRecipeCost, Identifier arrow, Identifier arrowOverlay) {
        super(menu, inventory, title);
        this.baseEnergyUse = baseEnergyUse;
        this.defaultRecipeCost = defaultRecipeCost;
        this.arrow = arrow;
        this.arrowOverlay = arrowOverlay;

        // FIXME: only enable when configuration upgrade is installed
        this.slotAccess = new SlotAccessComponent<>(this, new IntRange(UPGRADE_1, UPGRADE_1 + UPGRADES)) {
        };
//        this.addWidget(this.slotAccess);
    }

    public static ClassicMachineScreen createElectricFurnace(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                ElectricFurnaceBlockEntity.ENERGY_USE_RATE,
                ElectricFurnaceBlockEntity.energyCostFromCookingTime(AbstractFurnaceBlockEntity.BURN_TIME_STANDARD),
                HayoSprites.DEFAULT_ARROW,
                HayoSprites.DEFAULT_ARROW_OVERLAY);
    }

    public static ClassicMachineScreen createMacerator(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                MaceratorBlockEntity.ENERGY_USE_RATE,
                MaceratingRecipe.DEFAULT_ENERGY,
                HayoSprites.MACERATING_ARROW,
                HayoSprites.MACERATING_ARROW_OVERLAY);
    }

    public static ClassicMachineScreen createCompressor(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                CompressorBlockEntity.ENERGY_USE_RATE,
                CompressingRecipe.DEFAULT_ENERGY,
                HayoSprites.COMPRESSING_ARROW,
                HayoSprites.COMPRESSING_ARROW_OVERLAY);
    }

    public static ClassicMachineScreen createExtractor(ClassicMachineMenu menu, Inventory inventory, Component title) {
        return new ClassicMachineScreen(menu, inventory, title,
                ExtractorBlockEntity.ENERGY_USE_RATE,
                ExtractingRecipe.DEFAULT_ENERGY,
                HayoSprites.EXTRACTING_ARROW,
                HayoSprites.EXTRACTING_ARROW_OVERLAY);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.slotAccess.init(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        this.addRenderableWidget(this.slotAccess);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.slotAccess != null) {
            this.slotAccess.tick();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        HayoSprites.blitBackground(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        HayoSprites.blitSlot(graphics, this.leftPos + 46, this.topPos + 17);
        HayoSprites.blitSlot(graphics, this.leftPos + 46, this.topPos + 53);
        HayoSprites.blitOutputSlot(graphics, this.leftPos + 103, this.topPos + 32);
        HayoSprites.blitUpgradeSlots(graphics, this.leftPos + 151, this.topPos + 7, 4);
        HayoSprites.blitStandardPlayerSlots(graphics, this.leftPos + 7, this.topPos + 83);

        var data = this.menu.data;
        HayoEnergySprites.blitZap(graphics, this.leftPos + 48, this.topPos + 37, data.energy(), data.capacity());
        HayoSprites.blitRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.arrow, this.arrowOverlay, data.recipeProgress(), data.recipeCost());
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

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }
}
