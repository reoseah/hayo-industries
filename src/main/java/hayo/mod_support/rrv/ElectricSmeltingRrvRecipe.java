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
import hayo.energy.EnergyTexts;
import hayo.energy.client.HayoEnergySprites;
import hayo.features.processing_machine.classic.ElectricFurnaceBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static hayo.common.HayoSprites.DEFAULT_ARROW;
import static hayo.common.HayoSprites.DEFAULT_ARROW_OVERLAY;
import static hayo.features.processing_machine.classic.ElectricFurnaceBlockEntity.ENERGY_USE_RATE;

public class ElectricSmeltingRrvRecipe implements ReliableClientRecipe {
    public static final ReliableClientRecipeType TYPE = new ReliableClientRecipeType() {
        @Override
        public Component getDisplayName() {
            return Component.translatable("hayo.recipe_type.electric_smelting");
        }

        @Override
        public Identifier getId() {
            return Hayo.modId("electric_smelting");
        }

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
            definition.addItemSlot(2, 5, 5);
            definition.addItemSlot(0, 5 + 20, 5);
            definition.addItemSlot(1, 65 + 20, 8);
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Hayo.Items.ELECTRIC_FURNACE);
        }

        @Override
        public List<ItemStack> getCraftReferences() {
            return List.of(new ItemStack(Hayo.Items.ELECTRIC_FURNACE));
        }
    };

    private final RecipeHolder<? extends AbstractCookingRecipe> holder;

    public ElectricSmeltingRrvRecipe(RecipeHolder<? extends AbstractCookingRecipe> holder) {
        this.holder = holder;
    }

    public static boolean needsUpgrade(RecipeType<? extends AbstractCookingRecipe> type) {
        return type == RecipeType.BLASTING || type == RecipeType.SMOKING;
    }

    public static @Nullable Item getUpgrade(RecipeType<? extends AbstractCookingRecipe> type) {
        if (type == RecipeType.BLASTING) {
            return Hayo.Items.BLASTING_UPGRADE;
        } else if (type == RecipeType.SMOKING) {
            return Hayo.Items.SMOKING_UPGRADE;
        }
        return null;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return TYPE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        var recipe = this.holder.value();
        context.bindSlot(0, SlotContent.of(recipe.input()));
        context.bindSlot(1, SlotContent.of(recipe.result()));
        if (needsUpgrade(recipe.getType())) {
            context.bindSlot(2, SlotContent.of(getUpgrade(recipe.getType())));
        }
    }

    @Override
    public List<SlotContent> getIngredients() {
        var recipe = this.holder.value();

        var input = SlotContent.of(recipe.input());
        var upgrade = getUpgrade(recipe.getType());

        if (upgrade != null) {
            return List.of(input, SlotContent.of(upgrade));
        }
        return List.of(input);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(SlotContent.of(this.holder.value().result()));
    }

    @Override
    public Identifier getId() {
        var recipeId = this.holder.id().identifier();
        return Hayo.modId("/electric_smelting/" + recipeId.getNamespace() + '/' + recipeId.getPath());
    }

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        var graphics = context.guiGraphics();
        var position = context.recipePosition();
        var font = context.font();

        var recipe = this.holder.value();

        int energyCost = ElectricFurnaceBlockEntity.energyCostFromCookingTime(recipe);
        long durationMillis = Mth.positiveCeilDiv(energyCost, ENERGY_USE_RATE) * 50L;

        boolean needsUpgrade = needsUpgrade(recipe.getType());
        if (!(context.screen() instanceof RecipeViewScreen)) {
            // can't edit slots, so always render with an upgrade slot
            needsUpgrade = true;
        }

        if (!needsUpgrade) {
            var screen = (RecipeViewScreen) context.screen();
            var slots = screen.getMenu().slots;

            for (int i = 0; i < slots.size(); i++) {
                if (!(slots.get(i) instanceof RecipeSlot slot)) {
                    continue;
                }

                if (slot.x == position.left() - screen.getLeftPos() + 5
                        && slot.y == position.top() - screen.getTopPos() + 5) {
                    slots.set(i, new RecipeSlot(slot.container, slot.index, Integer.MIN_VALUE, Integer.MIN_VALUE, true));
                } else if (slot.x == position.left() - screen.getLeftPos() + 25
                        && slot.y == position.top() - screen.getTopPos() + 5) {
                    slots.set(i, new RecipeSlot(slot.container, slot.index, slot.x - 10, slot.y, true));
                } else if (slot.x == position.left() - screen.getLeftPos() + 85
                        && slot.y == position.top() - screen.getTopPos() + 8) {
                    slots.set(i, new RecipeSlot(slot.container, slot.index, slot.x - 10, slot.y, true));
                }
            }
        }

        HayoSprites.blitRecipeViewBackground(graphics, needsUpgrade ? 0 : 10, 0, position.width() + (needsUpgrade ? 0 : -20), position.height());
        if (needsUpgrade) {
            HayoSprites.blitUpgradeSlots(graphics, 4, 4, 1);
        }
        HayoSprites.blitSlot(graphics, 4 + (needsUpgrade ? 20 : 10), 4);
        HayoSprites.blitOutputSlot(graphics, 61 + (needsUpgrade ? 20 : 10), 4);
        HayoEnergySprites.blitAnimatedZap(graphics, 6 + (needsUpgrade ? 20 : 10), 23, durationMillis);
        HayoSprites.blitAnimatedRecipeArrow(graphics, 28 + (needsUpgrade ? 20 : 10), 8, DEFAULT_ARROW, DEFAULT_ARROW_OVERLAY, durationMillis);

        graphics.text(font, EnergyTexts.amount(energyCost), 23 + (needsUpgrade ? 20 : 10), 27, 0xFF404040, false);
    }
}
