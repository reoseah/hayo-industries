package io.github.reoseah.hayo.feature.universal_screen;

import io.github.reoseah.hayo.Hayo;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Accessors(chain = true)
public class UniversalContainerMenu extends AbstractContainerMenu {
    protected final Container container;
    protected SlotRange playerInventory;
    protected SlotRange playerMainInventory;
    protected SlotRange playerHotbar;
    protected final List<QuickMoveRule> quickMoveRules = new ArrayList<>();

    protected int width = 176, height = 166;
    @Setter
    protected boolean centerTitle = true;
    protected int titleX = 8, titleY = 6;
    protected int inventoryLabelX = 8, inventoryLabelY = 72;

    protected final List<GuiElement> guiElements = new ArrayList<>();

    public UniversalContainerMenu(int containerId, Container container) {
        super(Hayo.MenuTypes.UNIVERSAL, containerId);
        this.container = container;
    }

    public UniversalContainerMenu setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public UniversalContainerMenu setTitlePos(int x, int y) {
        this.titleX = x;
        this.titleY = y;
        return this;
    }

    public UniversalContainerMenu setInventoryLabelPos(int x, int y) {
        this.inventoryLabelX = x;
        this.inventoryLabelY = y;
        return this;
    }

    public UniversalContainerMenu addDataSlotsChainable(ContainerData data) {
        this.addDataSlots(data);
        return this;
    }

    public UniversalContainerMenu addSlotChainable(Slot slot) {
        this.addSlot(slot);
        return this;
    }

    public record SlotRange(int start, int end) {
        public static SlotRange of(int start, int count) {
            return new SlotRange(start, start + count);
        }

        public boolean contains(int index) {
            return index >= this.start && index < this.end;
        }
    }

    public UniversalContainerMenu addStandardInventorySlotsChainable(Container inventory) {
        return this.addStandardInventorySlotsChainable(inventory, 8, 84);
    }

    public UniversalContainerMenu addStandardInventorySlotsChainable(Container inventory, int x, int y) {
        int start = this.slots.size();

        this.addStandardInventorySlots(inventory, x, y);

        this.playerInventory = new SlotRange(start, start + 36);
        this.playerMainInventory = new SlotRange(start, start + 27);
        this.playerHotbar = new SlotRange(start + 27, start + 36);

        return this;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    protected record QuickMoveRule(int start, int end, Predicate<ItemStack> predicate, boolean reverse) {
    }

    public UniversalContainerMenu addQuickMoveRule(int start, int end, Predicate<ItemStack> predicate) {
        this.quickMoveRules.add(new QuickMoveRule(start, end, predicate, false));
        return this;
    }

    public UniversalContainerMenu addQuickMoveRule(int start, int end, Predicate<ItemStack> predicate, boolean reverse) {
        this.quickMoveRules.add(new QuickMoveRule(start, end, predicate, reverse));
        return this;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var original = stack.copy();

        if (!this.playerInventory.contains(index)) {
            if (!this.moveItemStackTo(stack, this.playerInventory.start(), this.playerInventory.end(), true)) {
                return ItemStack.EMPTY;
            }

            slot.onQuickCraft(stack, original);
        } else {
            boolean moved = false;

            for (var rule : this.quickMoveRules) {
                if (rule.predicate().test(stack) && this.moveItemStackTo(stack, rule.start(), rule.end(), rule.reverse())) {
                    moved = true;
                    break;
                }
            }

            if (!moved) {
                if (this.playerMainInventory.contains(index)) {
                    moved = this.moveItemStackTo(stack, this.playerHotbar.start(), this.playerHotbar.end(), false);
                } else if (this.playerHotbar.contains(index)) {
                    moved = this.moveItemStackTo(stack, this.playerMainInventory.start(), this.playerMainInventory.end(), false);
                }
            }

            if (!moved) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    public interface GuiElement {
        @Environment(EnvType.CLIENT)
        void extract( //
                GuiGraphicsExtractor graphics, //
                Font font, //
                int left, //
                int top, //
                int mouseX, //
                int mouseY, //
                float partialTick //
        );

        @Environment(EnvType.CLIENT)
        default void extractTooltip( //
                GuiGraphicsExtractor graphics, //
                Font font, //
                int left, //
                int top, //
                int mouseX, //
                int mouseY //
        ) {
        }

        static boolean isHovering(int left, int top, int x, int y, int width, int height, double mouseX, double mouseY) {
            mouseX -= left;
            mouseY -= top;
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    public UniversalContainerMenu addElement(GuiElement element) {
        this.guiElements.add(element);
        return this;
    }
}
