package hayo.mod_support.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import hayo.Hayo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MatterGeneratingRrvType implements ReliableClientRecipeType {
    protected static final MatterGeneratingRrvType INSTANCE = new MatterGeneratingRrvType();

    @Override
    public Component getDisplayName() {
        return Component.translatable("hayo.recipe_type.matter_generating");
    }

    @Override
    public int getDisplayWidth() {
        return 100;
    }

    @Override
    public int getDisplayHeight() {
        return 36;
    }

    @Override
    public @Nullable Identifier getGuiTexture() {
        return null;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 1, 5);
        slotDefinition.addItemSlot(1, 79, 5);
    }

    @Override
    public Identifier getId() {
        return Hayo.modId("matter_generating");
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Hayo.Items.MATTER_GENERATOR);
    }
}
