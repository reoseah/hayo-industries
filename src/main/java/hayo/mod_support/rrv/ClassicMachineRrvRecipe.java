package hayo.mod_support.rrv;

import cc.cassian.rrv.api.client.RecipeScreenContext;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeSlot;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import hayo.Hayo;
import hayo.common.HayoSprites;
import hayo.common.StringUtils;
import hayo.energy.EnergyTexts;
import hayo.energy.client.HayoEnergySprites;
import hayo.features.processing_machine.classic.ClassicMachineRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static hayo.common.HayoSprites.*;
import static hayo.features.processing_machine.classic.ElectricFurnaceBlockEntity.ENERGY_USE_RATE;

public class ClassicMachineRrvRecipe implements ReliableClientRecipe {
    public static final ReliableClientRecipeType MACERATING = new Type() {
        @Override
        public Component getDisplayName() {
            return Component.translatable("hayo.recipe_type.macerating");
        }

        @Override
        public Identifier getId() {
            return Hayo.modId("macerating");
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Hayo.Items.MACERATOR);
        }

        @Override
        public List<ItemStack> getCraftReferences() {
            return List.of(new ItemStack(Hayo.Items.MACERATOR));
        }
    };
    public static final ReliableClientRecipeType COMPRESSING = new Type() {
        @Override
        public Component getDisplayName() {
            return Component.translatable("hayo.recipe_type.compressing");
        }

        @Override
        public Identifier getId() {
            return Hayo.modId("compressing");
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Hayo.Items.COMPRESSOR);
        }

        @Override
        public List<ItemStack> getCraftReferences() {
            return List.of(new ItemStack(Hayo.Items.COMPRESSOR));
        }
    };
    public static final ReliableClientRecipeType EXTRACTING = new Type() {
        @Override
        public Component getDisplayName() {
            return Component.translatable("hayo.recipe_type.extracting");
        }

        @Override
        public Identifier getId() {
            return Hayo.modId("extracting");
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Hayo.Items.EXTRACTOR);
        }

        @Override
        public List<ItemStack> getCraftReferences() {
            return List.of(new ItemStack(Hayo.Items.EXTRACTOR));
        }
    };

    private static abstract class Type implements ReliableClientRecipeType {
        @Override
        public int getDisplayWidth() {
            return 82 + 6 + 1 + 20;
        }

        @Override
        public int getDisplayHeight() {
            return 35 + 6 + 1;
        }

        @Override
        public @Nullable Identifier getGuiTexture() {
            return null;
        }

        @Override
        public void placeSlots(RecipeViewMenu.SlotDefinition definition) {
            definition.addItemSlot(0, 5, 5);
            definition.addItemSlot(1, 65, 8);
        }
    }

    private final RecipeHolder<? extends ClassicMachineRecipe> holder;

    public ClassicMachineRrvRecipe(RecipeHolder<? extends ClassicMachineRecipe> holder) {
        this.holder = holder;
    }

    @Override
    public ReliableClientRecipeType getType() {
        var recipeType = this.holder.value().getType();
        if (recipeType == Hayo.RecipeTypes.MACERATING) {
            return MACERATING;
        }
        if (recipeType == Hayo.RecipeTypes.COMPRESSING) {
            return COMPRESSING;
        }
        if (recipeType == Hayo.RecipeTypes.EXTRACTING) {
            return EXTRACTING;
        }
        throw new IllegalStateException("Unknown recipe type: " + recipeType);
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        var recipe = this.holder.value();
        context.bindSlot(0, SlotContent.of(recipe.input()));
        context.bindSlot(1, SlotContent.of(recipe.result()));
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(SlotContent.of(this.holder.value().input()));
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(SlotContent.of(this.holder.value().result()));
    }

    @Override
    public Identifier getId() {
        return this.holder.id().identifier();
    }

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        var graphics = context.guiGraphics();
        var position = context.recipePosition();
        var font = context.font();

        var recipe = this.holder.value();

        int energyCost = recipe.energyCost;
        long durationMillis = Mth.positiveCeilDiv(energyCost, ENERGY_USE_RATE) * 50L;

        boolean hasExtraResult = recipe.extraResultChance > 0;
        if (!(context.screen() instanceof RecipeViewScreen)) {
            // can't edit slots, so always render with an extra slot
            hasExtraResult = true;
        }

        if (!hasExtraResult) {
            var screen = (RecipeViewScreen) context.screen();
            var slots = screen.getMenu().slots;

            for (int i = 0; i < slots.size(); i++) {
                if (!(slots.get(i) instanceof RecipeSlot slot)) {
                    continue;
                }

                if (slot.x == position.left() - screen.getLeftPos() + 5
                        && slot.y == position.top() - screen.getTopPos() + 5) {
                    slots.set(i, new RecipeSlot(slot.container, slot.index, slot.x + 10, slot.y, true));
                } else if (slot.x == position.left() - screen.getLeftPos() + 65
                        && slot.y == position.top() - screen.getTopPos() + 8) {
                    slots.set(i, new RecipeSlot(slot.container, slot.index, slot.x + 10, slot.y, true));
                }
            }
        }

        HayoSprites.blitRecipeViewBackground(graphics, hasExtraResult ? 0 : 10, 0, position.width() + (hasExtraResult ? 0 : -20), position.height());
        HayoSprites.blitSlot(graphics, 4 + (hasExtraResult ? 0 : 10), 4);
        HayoSprites.blitOutputSlot(graphics, 61 + (hasExtraResult ? 0 : 10), 4);

        if (hasExtraResult) {
            HayoSprites.blitSlot(graphics, 87, 4);
            graphics.fakeItem(recipe.result().create().copyWithCount(1), 88, 5);

            var subscriptChance = Component.translatable("hayo.subscript_chance", StringUtils.toSubscriptDigits(recipe.extraResultChance));
            int chanceWidth = font.width(subscriptChance);
            graphics.text(font, subscriptChance, 87 + 18 - chanceWidth, 4 + 18 - font.lineHeight, 0xFFFFFFFF, true);
        }

        HayoEnergySprites.blitAnimatedZap(graphics, 6 + (hasExtraResult ? 0 : 10), 23, durationMillis);

        if (recipe.getType() == Hayo.RecipeTypes.MACERATING) {
            HayoSprites.blitAnimatedRecipeArrow(graphics, 28 + (hasExtraResult ? 0 : 10), 8, MACERATING_ARROW, MACERATING_ARROW_OVERLAY, durationMillis);
        } else if (recipe.getType() == Hayo.RecipeTypes.COMPRESSING) {
            HayoSprites.blitAnimatedRecipeArrow(graphics, 28 + (hasExtraResult ? 0 : 10), 8, COMPRESSING_ARROW, COMPRESSING_ARROW_OVERLAY, durationMillis);
        } else if (recipe.getType() == Hayo.RecipeTypes.EXTRACTING) {
            HayoSprites.blitAnimatedRecipeArrow(graphics, 28 + (hasExtraResult ? 0 : 10), 8, EXTRACTING_ARROW, EXTRACTING_ARROW_OVERLAY, durationMillis);
        }

        graphics.text(font, EnergyTexts.amount(energyCost), 23 + (hasExtraResult ? 0 : 10), 27, 0xFF404040, false);
    }
}
