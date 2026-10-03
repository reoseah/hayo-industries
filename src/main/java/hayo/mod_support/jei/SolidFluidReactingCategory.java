package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.fluid_stack.FluidGuiRendering;
import hayo.processing_machine.solid_fluid_reactor.BaseSolidFluidReactingRecipe;
import hayo.processing_machine.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;

import static hayo.mod_support.jei.SingleItemJeiCategory.UPGRADE_SLOT_DRAWABLE;

public class SolidFluidReactingCategory extends AbstractRecipeCategory<RecipeHolder<BaseSolidFluidReactingRecipe>> {
    public final ItemStack requiredUpgrade;

    public SolidFluidReactingCategory(IRecipeType<RecipeHolder<BaseSolidFluidReactingRecipe>> type, Component name, IDrawable icon, ItemStack requiredUpgrade) {
        super(type, name, icon, 140 + (requiredUpgrade.isEmpty() ? 0 : 20), 54);
        this.requiredUpgrade = requiredUpgrade;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BaseSolidFluidReactingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        if (!this.requiredUpgrade.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 18)
                    .setBackground(UPGRADE_SLOT_DRAWABLE, -1, -1)
                    .addRichTooltipCallback((_, tooltip) -> tooltip.add(Component.translatable("hayo.required_upgrade").withStyle(ChatFormatting.YELLOW)))
                    .add(this.requiredUpgrade);
        }

        int left = !this.requiredUpgrade.isEmpty() ? 20 : 0;

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(
                recipe.inputFluid.values().stream().findAny().map(Holder::value).orElse(Fluids.EMPTY),
                recipe.inputFluid.amount()
        );

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, left + 23, 10).setStandardSlotBackground();
        if (recipe.inputCount != 1) {
            inputSlot.addItemStacks(recipe.inputItem.items().map(item -> new ItemStack(item, recipe.inputCount)).toList());
        } else {
            inputSlot.add(recipe.inputItem);
        }

        boolean hasExtraChances = !recipe.extraResultChances.isEmpty();
        int resultsLeft = left + 84 + (hasExtraChances ? 0 : 9);

        builder.addSlot(RecipeIngredientRole.OUTPUT, resultsLeft, 1)
                .add(getOptional(recipe.resultItems, 0)
                        .map(ItemStackTemplate::create)
                        .orElse(ItemStack.EMPTY)
                ).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.OUTPUT, resultsLeft, 19)
                .add(getOptional(recipe.resultItems, 1)
                        .map(ItemStackTemplate::create)
                        .orElse(ItemStack.EMPTY)
                ).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.OUTPUT, resultsLeft, 37)
                .add(getOptional(recipe.resultItems, 2)
                        .map(ItemStackTemplate::create)
                        .orElse(ItemStack.EMPTY)
                ).setStandardSlotBackground();
    }

    public static <T> Optional<T> getOptional(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index));
    }

    @Override
    public void draw(RecipeHolder<BaseSolidFluidReactingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        var recipe = holder.value();

        int left = !this.requiredUpgrade.isEmpty() ? 20 : 0;

        FluidGuiRendering.extractFluidTank(
                graphics,
                recipe.inputFluid,
                SolidFluidReactorBlockEntity.FLUID_CAPACITY,
                left,
                -1
        );

        boolean hasExtraChances = !recipe.extraResultChances.isEmpty();
        int resultsLeft = left + 84 + (hasExtraChances ? 0 : 9);

        FluidGuiRendering.extractFluidTank(
                graphics,
                recipe.resultFluid,
                SolidFluidReactorBlockEntity.FLUID_CAPACITY,
                resultsLeft + 20 + (hasExtraChances ? 18 : 0),
                -1
        );

        EnergyGuiSprites.blitZap(graphics, left + 24, 29, 10, 14);
        graphics.text(Minecraft.getInstance().font, EnergyTexts.amount(recipe.energyCost), left + 40, 33, 0xFF404040, false);

        int energyUseRate = SolidFluidReactorBlockEntity.REACTING_ENERGY_RATE;
        int fill = (int) Math.abs((System.currentTimeMillis() / 50 * energyUseRate) % recipe.energyCost);
        HayoGuiSprites.blitRecipeArrow(graphics, left + 49 + (hasExtraChances ? 0 : 5), 12, HayoGuiSprites.REACTING_ARROW, HayoGuiSprites.REACTING_ARROW_OVERLAY, fill, recipe.energyCost);

        if (hasExtraChances) {
            int i = 0;
            for (; i < recipe.extraResultChances.size(); i++) {
                SingleItemJeiCategory.drawExtraChanceItemSlot(graphics, recipe.resultItems.get(i).create(), recipe.extraResultChances.get(i), resultsLeft + 17, i * 18);
            }
            for (; i < 3; i++) {
                HayoGuiSprites.blitSlot(graphics, resultsLeft + 17, i * 18);
            }
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<BaseSolidFluidReactingRecipe> holder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var recipe = holder.value();

        int left = !this.requiredUpgrade.isEmpty() ? 20 : 0;

        if (FluidGuiRendering.isHoveringTank(left, -1, mouseX, mouseY)) {
            tooltip.addAll(FluidGuiRendering.createTooltip(recipe.inputFluid));
        }

        boolean hasExtraChances = !recipe.extraResultChances.isEmpty();
        int resultsLeft = left + 84 + (hasExtraChances ? 0 : 9);

        if (FluidGuiRendering.isHoveringTank(resultsLeft + 20 + (hasExtraChances ? 18 : 0), -1, mouseX, mouseY)) {
            tooltip.addAll(FluidGuiRendering.createTooltip(recipe.resultFluid));
        }

        if (hasExtraChances) {
            if (mouseX >= resultsLeft + 18 && mouseX < resultsLeft + 18 + 18 && mouseY >= 0 && mouseY < 18 * 3) {
                int i = (int) (mouseY / 18);
                if (i < recipe.extraResultChances.size()) {
                    tooltip.add(Component.translatable("hayo.extra_chance_tooltip", String.format("%.0f", 100 * recipe.extraResultChances.get(i))).withStyle(ChatFormatting.YELLOW));
                }
            }
        }
    }
}
