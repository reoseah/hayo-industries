package hayo.mod_support.jei;

import hayo.common.HayoGuiSprites;
import hayo.energy.EnergyTexts;
import hayo.energy.client.EnergyGuiSprites;
import hayo.processing_machine.classic.ClassicMachineRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.elements.DrawableSprite;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.jspecify.annotations.Nullable;

public abstract class SingleItemJeiCategory<T extends RecipeHolder<? extends SingleItemRecipe>> implements IRecipeCategory<T> {
    public static final IDrawable UPGRADE_SLOT_DRAWABLE = new DrawableSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI), HayoGuiSprites.UPGRADE_SLOT, 18, 18);

    private final IDrawable icon;
    public final @Nullable ItemStack requiredUpgrade;

    protected SingleItemJeiCategory(IDrawable icon, @Nullable ItemStack requiredUpgrade) {
        this.icon = icon;
        this.requiredUpgrade = requiredUpgrade;
    }

    protected final boolean canHaveCatalyst() {
        return this.requiredUpgrade != null;
    }

    protected final ItemStack getCatalyst(T holder) {
        return this.requiredUpgrade;
    }

    protected abstract boolean canHaveSecondaryResult();

    protected abstract int getEnergyCost(T holder);

    protected abstract int getEnergyUseRate(T holder);

    protected abstract Identifier getArrowSprite();

    protected abstract Identifier getArrowOverlaySprite();

    @Override
    public final IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        int width = 82;
        if (this.canHaveCatalyst()) {
            width += 20;
        }
        if (this.canHaveSecondaryResult()) {
            width += 20;
        }
        return width;
    }

    @Override
    public int getHeight() {
        return 35;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T holder, IFocusGroup focuses) {
        var recipe = holder.value();
        if (this.canHaveCatalyst() && !this.getCatalyst(holder).isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .setBackground(UPGRADE_SLOT_DRAWABLE, -1, -1)
                    .addRichTooltipCallback((_, tooltip) -> tooltip.add(Component.translatable("hayo.required_upgrade").withStyle(ChatFormatting.YELLOW)))
                    .add(this.getCatalyst(holder));
        }

        int left = this.canHaveCatalyst() ? 20 : 0;

        var inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, left + 1, 1).setStandardSlotBackground();
        if (recipe instanceof ClassicMachineRecipe machineRecipe && machineRecipe.inputCount != 1) {
            inputSlot.addItemStacks(recipe.input().items().map(item -> new ItemStack(item, machineRecipe.inputCount)).toList());
        } else {
            inputSlot.add(recipe.input());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, left + 61, 5)
                .setOutputSlotBackground()
                .add(recipe.result().create());
    }

    @Override
    public void draw(T holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int x = this.canHaveCatalyst() ? 20 : 0;

        EnergyGuiSprites.blitZap(graphics, x + 1, 20, 10, 14);

        int energyCost = this.getEnergyCost(holder);
        int energyUseRate = this.getEnergyUseRate(holder);
        int fill = (int) Math.abs((System.currentTimeMillis() / 50 * energyUseRate) % energyCost);
        HayoGuiSprites.blitRecipeArrow(graphics, x + 24, 4, this.getArrowSprite(), this.getArrowOverlaySprite(), fill, energyCost);

        var font = Minecraft.getInstance().font;
        graphics.text(font, EnergyTexts.amount(energyCost), x + 19, 24, 0xFF404040, false);

        if (holder.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            drawExtraChanceItemSlot(graphics, machineRecipe.result().create(), machineRecipe.extraResultChance, x + 84, 0);
        }
    }

    public static void drawExtraChanceItemSlot(GuiGraphicsExtractor graphics, ItemStack stack, float chance, int x, int y) {
        var font = Minecraft.getInstance().font;

        HayoGuiSprites.blitSlot(graphics, x, 0);
        graphics.item(stack, x + 1, 1);

        var subscriptChance = Component.translatable("hayo.subscript_chance", toSubscriptDigits(chance));
        int chanceWidth = font.width(subscriptChance);
        graphics.text(font, subscriptChance, x + 18 - chanceWidth, y + 18 - font.lineHeight, 0xFFFFFFFF, true);
    }

    public static String toSubscriptDigits(float chance) {
        return String.format("%.0f", 100 * chance)
                .chars()
                .map(ch -> {
                    char[] subscriptDigits = {'₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉'};
                    if (ch >= '0' && ch <= '9') {
                        return subscriptDigits[ch - '0'];
                    }
                    return ch;
                })
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T holder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        IRecipeCategory.super.getTooltip(tooltip, holder, recipeSlotsView, mouseX, mouseY);

        if (holder.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            if (mouseX >= 84 && mouseX < 84 + 18 && mouseY >= 0 && mouseY < 18) {
//                tooltip.addAll(holder.value().result().create().getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL));
                tooltip.add(Component.translatable("hayo.extra_chance_tooltip", String.format("%.0f", 100 * machineRecipe.extraResultChance)).withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
