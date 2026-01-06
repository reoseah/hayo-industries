package io.github.reoseah.hayo.base;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

public abstract class HayoContainerMenu extends AbstractContainerMenu {
    protected final Container container;

    protected HayoContainerMenu(@Nullable MenuType<?> menuType, int containerId, Container container) {
        super(menuType, containerId);
        this.container = container;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
}
