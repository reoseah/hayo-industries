package io.github.reoseah.hayo.base;

import io.github.reoseah.hayo.Hayo;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class HayoContainerMenu extends AbstractContainerMenu {
    protected final Container container;
    protected final int width, height;

    protected HayoContainerMenu(@Nullable MenuType<?> menuType, int containerId, Container container, int width, int height) {
        super(menuType, containerId);
        this.container = container;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Accessors(fluent = true, chain = true)
    public static class Builder {
        private final int containerId;
        private final Container container;

        // Default assumes both server and client open this menu from block entity method,
        // and that block entity is synchronized as needed for both sides to construct
        // equivalent menu.
        @Setter
        protected @Nullable MenuType<?> menuType = Hayo.MenuTypes.BLOCK_POS_MENU;

        @Setter
        protected int width = 176, height = 166;

        public Builder(int containerId, Container container) {
            this.containerId = containerId;
            this.container = container;
        }

        public HayoContainerMenu build() {
            return new HayoContainerMenu(this.menuType, this.containerId, this.container, this.width, this.height);
        }
    }
}
