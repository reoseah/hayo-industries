package io.github.reoseah.hayoind.menu;

import io.github.reoseah.hayoind.Hayo;
import io.github.reoseah.hayoind.block.entity.*;
import io.github.reoseah.hayoind.item.ElectricItems;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;

public abstract class ProcessingMachineMenu extends AbstractContainerMenu {
    protected final ContainerData data;
    protected final TagKey<Item> validUpgrades;

    protected ProcessingMachineMenu(MenuType<?> type, TagKey<Item> validUpgrades, int menuId, Container container, ContainerData data, Inventory inventory) {
        super(type, menuId);

        this.validUpgrades = validUpgrades;

        this.data = data;
        this.addDataSlots(this.data);

        this.addSlot(new Slot(container, 0, 47, 18));
        this.addSlot(new Slot(container, 1, 47, 54));
        this.addSlot(new SimpleResultSlot(container, 2, 107, 36));

        this.addSlot(new UpgradeSlot(container, 3, 152, 8, validUpgrades));
        this.addSlot(new UpgradeSlot(container, 4, 152, 26, validUpgrades));
        this.addSlot(new UpgradeSlot(container, 5, 152, 44, validUpgrades));
        this.addSlot(new UpgradeSlot(container, 6, 152, 62, validUpgrades));

        this.addStandardInventorySlots(inventory, 8, 84);
    }

    public static ContainerData createData(ProcessingMachineBlockEntity<?, ?> entity) {
        return new ContainerData() {
            @Override
            public int getCount() {
                return 7;
            }

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> entity.getStoredEnergy() & 0xFFFF;
                    case 1 -> entity.getStoredEnergy() >>> 16;
                    case 2 -> entity.getRecipeUsedEnergy();
                    case 3 -> entity.getRecipeTotalEnergy();
                    case 4 -> entity.getEnergyCapacity() & 0xFFFF;
                    case 5 -> entity.getEnergyCapacity() >>> 16;
                    case 6 -> entity.getOverclockCount();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }
        };
    }

    public float getRecipeDuration() {
        return Mth.ceil(this.getRecipeTotalEnergy() / (float) this.getEnergyUseRate()) / 20F;
    }

    public static class UpgradeSlot extends Slot {
        protected final TagKey<Item> validItems;

        public UpgradeSlot(Container container, int slot, int x, int y, TagKey<Item> validItems) {
            super(container, slot, x, y);
            this.validItems = validItems;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(this.validItems);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public static final int FIRST_PLAYER_SLOT = 3 + 4;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var previous = stack.copy();
        if (index < FIRST_PLAYER_SLOT) {
            if (!this.moveItemStackTo(stack, FIRST_PLAYER_SLOT, FIRST_PLAYER_SLOT + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, previous);
        } else {
            if (stack.is(this.validUpgrades)) {
                if (!this.moveItemStackTo(stack, 3, 7, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (ElectricItems.isElectric(stack)) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.isRecipeInput(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < FIRST_PLAYER_SLOT + 27) {
                if (!this.moveItemStackTo(stack, FIRST_PLAYER_SLOT + 27, FIRST_PLAYER_SLOT + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, FIRST_PLAYER_SLOT, FIRST_PLAYER_SLOT + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == previous.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return previous;
    }

    protected abstract boolean isRecipeInput(ItemStack stack);

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getStoredEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public int getRecipeUsedEnergy() {
        return this.data.get(2);
    }

    public int getRecipeTotalEnergy() {
        return this.data.get(3);
    }

    public int getEnergyCapacity() {
        return (this.data.get(5) << 16) | (this.data.get(4) & 0xFFFF);
    }

    public boolean hasOverclockUpgrades() {
        return this.data.get(6) > 0;
    }

    public int getUseRatePercentage() {
        return 100 + 100 * this.data.get(6);
    }

    public int getRecipeEnergyPercentage() {
        return 100 + 25 * this.data.get(6);
    }

    public int getRecipeDurationPercentage() {
        return 100 * this.getRecipeEnergyPercentage() / this.getUseRatePercentage();
    }

    public abstract int getEnergyUseRate();

    public static class ElectricFurnaceMenu extends ProcessingMachineMenu {
        private final RecipePropertySet acceptedInputs;

        public ElectricFurnaceMenu(int menuId, Inventory inventory) {
            super(Hayo.MenuTypes.ELECTRIC_FURNACE, Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES, menuId, new SimpleContainer(7), new SimpleContainerData(7), inventory);

            var level = inventory.player.level();
            this.acceptedInputs = level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT);
        }

        public ElectricFurnaceMenu(int menuId, ElectricFurnaceBlockEntity entity, Inventory inventory) {
            super(Hayo.MenuTypes.ELECTRIC_FURNACE, Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES, menuId, entity, createData(entity), inventory);

            var level = inventory.player.level();
            this.acceptedInputs = level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT);
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            return this.acceptedInputs.test(stack);
        }

        @Override
        public int getEnergyUseRate() {
            return ElectricFurnaceBlockEntity.ENERGY_USE_RATE * (1 + this.data.get(6));
        }
    }

    public static class MaceratorMenu extends ProcessingMachineMenu {
        public MaceratorMenu(int menuId, Inventory inventory) {
            super(Hayo.MenuTypes.MACERATOR, Hayo.ItemTags.MACERATOR_UPGRADES, menuId, new SimpleContainer(7), new SimpleContainerData(7), inventory);
        }

        public MaceratorMenu(int menuId, MaceratorBlockEntity entity, Inventory inventory) {
            super(Hayo.MenuTypes.MACERATOR, Hayo.ItemTags.MACERATOR_UPGRADES, menuId, entity, createData(entity), inventory);
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            return true;
        }

        @Override
        public int getEnergyUseRate() {
            return MaceratorBlockEntity.ENERGY_USE_RATE * (1 + this.data.get(6));
        }
    }

    public static class CompressorMenu extends ProcessingMachineMenu {
        public CompressorMenu(int menuId, Inventory inventory) {
            super(Hayo.MenuTypes.COMPRESSOR, Hayo.ItemTags.COMPRESSOR_UPGRADES, menuId, new SimpleContainer(7), new SimpleContainerData(7), inventory);
        }

        public CompressorMenu(int menuId, CompressorBlockEntity entity, Inventory inventory) {
            super(Hayo.MenuTypes.COMPRESSOR, Hayo.ItemTags.COMPRESSOR_UPGRADES, menuId, entity, createData(entity), inventory);
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            return true;
        }

        @Override
        public int getEnergyUseRate() {
            return CompressorBlockEntity.ENERGY_USE_RATE * (1 + this.data.get(6));
        }
    }

    public static class ExtractorMenu extends ProcessingMachineMenu {
        public ExtractorMenu(int menuId, Inventory inventory) {
            super(Hayo.MenuTypes.EXTRACTOR, Hayo.ItemTags.EXTRACTOR_UPGRADES, menuId, new SimpleContainer(7), new SimpleContainerData(7), inventory);
        }

        public ExtractorMenu(int menuId, ExtractorBlockEntity entity, Inventory inventory) {
            super(Hayo.MenuTypes.EXTRACTOR, Hayo.ItemTags.EXTRACTOR_UPGRADES, menuId, entity, createData(entity), inventory);
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            return true;
        }

        @Override
        public int getEnergyUseRate() {
            return ExtractorBlockEntity.ENERGY_USE_RATE * (1 + this.data.get(6));
        }
    }
}
