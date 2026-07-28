package io.github.reoseah.hayo.base.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class HayoContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public HayoContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    public HayoContainerScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        super(menu, inventory, title, imageWidth, imageHeight);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }
}
