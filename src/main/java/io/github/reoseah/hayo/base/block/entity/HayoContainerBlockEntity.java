package io.github.reoseah.hayo.base.block.entity;

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
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class HayoContainerBlockEntity extends BlockEntity implements Container, Nameable, MenuProvider {
    protected final NonNullList<ItemStack> stacks;
    protected @Nullable Component customName;

    public HayoContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.stacks = createInventory();
    }

    protected abstract NonNullList<ItemStack> createInventory();

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
        var stack = ContainerHelper.removeItem(this.stacks, slot, amount);
        this.setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.stacks, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        this.setChanged();
    }

    @Override
    protected void applyImplicitComponents(final DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.customName = components.get(DataComponents.CUSTOM_NAME);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.stacks);
    }

    @Override
    public boolean stillValid(final Player player) {
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
}
