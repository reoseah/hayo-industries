package hayo.processing_machine.classic;

import com.google.common.collect.Lists;
import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.processing_machine.UpgradableMachineBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
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

        EnergyGuiSprites.blitZap(graphics, this.leftPos + 48, this.topPos + 37, this.menu.machineData.energy(), this.menu.machineData.capacity());
        HayoGuiSprites.blitRecipeArrow(graphics, this.leftPos + 70, this.topPos + 36, this.arrow, this.arrowOverlay, this.menu.machineData.recipeProgress(), this.menu.machineData.recipeCost());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(48, 37, 14, 14, mouseX, mouseY)) {
            List<Component> components = Lists.newArrayList(
                    EnergyTexts.amountAndPercentage(this.menu.machineData.energy(), this.menu.machineData.capacity()),
                    EnergyTexts.maxAmount(this.menu.machineData.capacity()).withStyle(ChatFormatting.GRAY),
                    Component.translatable("hayo.machine.energy_use_with_base_and_bonus", this.menu.machineData.energyUseRate(), this.baseEnergyUse, String.format("%+.0f", this.menu.upgradeData.extraCraftingSpeed())).withStyle(ChatFormatting.GRAY)
            );
            if (this.menu.upgradeData.hasInductionUpgrade()) {
                components.add(Component.translatable("hayo.machine.heat", String.format("%.1f", this.menu.upgradeData.inductionHeat() * 100F / UpgradableMachineBlockEntity.MAX_INDUCTION_HEAT), this.menu.getRecipeProgressPerTick()).withStyle(ChatFormatting.GRAY));
            }

            components.add(Component.empty());
            if (this.menu.machineData.matchesRecipe()) {
                components.add(Component.translatable("hayo.machine.current_recipe").withStyle(ChatFormatting.GRAY));
            } else {
                components.add(Component.translatable("hayo.machine.default_recipe").withStyle(ChatFormatting.GRAY));
            }
            components.add(Component.translatable("hayo.machine.recipe_cost", this.menu.machineData.recipeCost(), this.defaultRecipeCost, String.format("%+.0f", this.menu.upgradeData.extraRecipeCost())).withStyle(ChatFormatting.GRAY));

            float duration = Mth.positiveCeilDiv(this.menu.machineData.recipeCost(), this.menu.getRecipeProgressPerTick()) / 20F;
            float defaultDuration = Mth.positiveCeilDiv(this.defaultRecipeCost, this.baseEnergyUse) / 20F;
            float relativeDuration = duration / defaultDuration * 100;
            components.add(Component.translatable("hayo.machine.recipe_duration", String.format("%.0f", duration), this.menu.getRecipeProgressPerTick(), String.format("%.0f", defaultDuration), String.format("%.0f",relativeDuration)).withStyle(ChatFormatting.GRAY));

            graphics.setTooltipForNextFrame(this.font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
