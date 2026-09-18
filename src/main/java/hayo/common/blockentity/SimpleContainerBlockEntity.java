package hayo.common.blockentity;

import hayo.solid_fluid_reactor.SolidFluidReactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class SimpleContainerBlockEntity extends BlockEntity implements Container, Nameable, MenuProvider {
    protected final NonNullList<ItemStack> stacks;
    protected @Nullable Component customName;

    public SimpleContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, NonNullList<ItemStack> initialStacks) {
        super(type, pos, state);
        this.stacks = initialStacks;
    }

    @MustBeInvokedByOverriders
    protected void inventoryChanged(int slot, ItemStack previous, ItemStack stack) {
        this.setChanged();
    }

    @Override
    @MustBeInvokedByOverriders
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.stacks);
        output.storeNullable("custom_name", ComponentSerialization.CODEC, this.customName);
    }

    @Override
    @MustBeInvokedByOverriders
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.stacks.clear();
        ContainerHelper.loadAllItems(input, this.stacks);
        this.customName = parseCustomNameSafe(input, "custom_name");
    }

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (var stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var previous = this.stacks.get(slot).copy();
        var stack = ContainerHelper.removeItem(this.stacks, slot, amount);
        this.inventoryChanged(slot, previous, stack);
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var previous = this.stacks.get(slot).copy();
        var stack = ContainerHelper.takeItem(this.stacks, slot);
        this.inventoryChanged(slot, previous, stack);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        var previous = this.stacks.get(slot);
        this.stacks.set(slot, stack);
        this.inventoryChanged(slot, previous, stack);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.customName = components.get(DataComponents.CUSTOM_NAME);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.stacks);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
    }

    @Override
    public @Nullable Component getCustomName() {
        return this.customName;
    }

    protected abstract Component getDefaultName();

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public Component getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    protected boolean canInsertToSlot(ItemStack stack, int slot) {
        var currentStack = this.stacks.get(slot);
        return currentStack.isEmpty()
                || ItemStack.isSameItemSameComponents(currentStack, stack)
                && currentStack.getCount() + stack.getCount() <= Math.min(stack.getMaxStackSize(), this.getMaxStackSize(stack));
    }

    public static boolean canInsertShapelessly(NonNullList<ItemStack> inventory, List<ItemStackTemplate> templates, int from, int to, int maxStackSize) {
        if (templates.isEmpty()) {
            return true;
        }

        var simulated = NonNullList.withSize(SolidFluidReactorBlockEntity.SLOTS, ItemStack.EMPTY);
        for (int slot = from; slot <= to; slot++) {
            simulated.set(slot, inventory.get(slot).copy());
        }

        for (var template : templates) {
            var resultStack = template.create();
            if (!insertShapeless(simulated, resultStack, from, to, maxStackSize)) {
                return false;
            }
        }

        return true;
    }

    public static boolean insertShapeless(NonNullList<ItemStack> inventory, ItemStack stack, int from, int to, int maxStackSize) {
        if (stack.isEmpty()) {
            return true;
        }

        for (int slot = from; slot <= to; slot++) {
            var current = inventory.get(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack)) {
                int maxCount = Math.min(stack.getMaxStackSize(), Math.min(maxStackSize, stack.getMaxStackSize()));
                int available = maxCount - current.getCount();
                if (available > 0) {
                    int toAdd = Math.min(available, stack.getCount());
                    current.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        for (int slot = from; slot <= to; slot++) {
            var current = inventory.get(slot);
            if (current.isEmpty()) {
                int maxCount = Math.min(stack.getMaxStackSize(), Math.min(maxStackSize, stack.getMaxStackSize()));
                int toAdd = Math.min(maxCount, stack.getCount());
                inventory.set(slot, stack.copyWithCount(toAdd));
                stack.shrink(toAdd);
                if (stack.isEmpty()) {
                    return true;
                }
            }
        }

        return stack.isEmpty();
    }
}
