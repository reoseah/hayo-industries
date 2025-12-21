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

import java.util.Map;

public abstract class ProcessingMachineMenu extends AbstractContainerMenu {
    protected final ContainerData data;
    protected final TagKey<Item> validUpgrades;

    protected ProcessingMachineMenu(MenuType<?> type, TagKey<Item> validUpgrades, int menuId, Container container, ContainerData data, Inventory inventory) {
        super(type, menuId);

        this.validUpgrades = validUpgrades;

        this.data = data;
        this.addDataSlots(this.data);
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

    public static void addClassicSlots(ProcessingMachineMenu menu, Container container, Inventory inventory) {
        menu.addSlot(new Slot(container, 0, 47, 18));
        menu.addSlot(new Slot(container, 1, 47, 54));
        menu.addSlot(new SimpleResultSlot(container, 2, 107, 36));

        menu.addSlot(new UpgradeSlot(container, 3, 152, 8, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 4, 152, 26, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 5, 152, 44, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 6, 152, 62, menu.validUpgrades));

        menu.addStandardInventorySlots(inventory, 8, 84);
    }

    public static void addClassicWithSecondaryOutputSlots(ProcessingMachineMenu menu, Container container, Inventory inventory) {
        menu.addSlot(new Slot(container, 0, 47, 18));
        menu.addSlot(new Slot(container, 1, 47, 54));
        menu.addSlot(new SimpleResultSlot(container, 2, 107, 24));
        menu.addSlot(new SimpleResultSlot(container, 3, 107, 50));

        menu.addSlot(new UpgradeSlot(container, 4, 152, 8, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 5, 152, 26, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 6, 152, 44, menu.validUpgrades));
        menu.addSlot(new UpgradeSlot(container, 7, 152, 62, menu.validUpgrades));

        menu.addStandardInventorySlots(inventory, 8, 84);
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var previous = stack.copy();
        int firstPlayerSlot = getFirstPlayerSlot();
        if (index < firstPlayerSlot) {
            if (!this.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 36, true)) {
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
            } else if (index < firstPlayerSlot + 27) {
                if (!this.moveItemStackTo(stack, firstPlayerSlot + 27, firstPlayerSlot + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, firstPlayerSlot, firstPlayerSlot + 27, false)) {
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

    protected abstract int getFirstPlayerSlot();

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

    public int getOverclockCount() {
        return this.data.get(6);
    }

    public boolean hasOverclockUpgrades() {
        return this.getOverclockCount() > 0;
    }

    public int getUseRatePercentage() {
        return 100 + 100 * this.getOverclockCount();
    }

    public int getRecipeEnergyPercentage() {
        return 100 + 25 * this.getOverclockCount();
    }

    public int getRecipeDurationPercentage() {
        return 100 * this.getRecipeEnergyPercentage() / this.getUseRatePercentage();
    }

    public abstract int getEnergyUseRate();

    public static class ElectricFurnaceMenu extends ProcessingMachineMenu {
        public static final int SLOTS = ElectricFurnaceBlockEntity.SLOTS;

        private final DataSlot recipeMode;
        private final Map<ElectricFurnaceBlockEntity.ElectricFurnaceMode, RecipePropertySet> recipeInputs;

        public ElectricFurnaceMenu(int menuId, Inventory inventory) {
            this(menuId, new SimpleContainer(SLOTS), new SimpleContainerData(7), inventory, DataSlot.standalone());
        }

        public ElectricFurnaceMenu(int menuId, ElectricFurnaceBlockEntity entity, Inventory inventory) {
            this(menuId, entity, createData(entity), inventory, createRecipeModeData(entity));
        }


        protected ElectricFurnaceMenu(int menuId, Container container, ContainerData data, Inventory inventory, DataSlot recipeMode) {
            super(Hayo.MenuTypes.ELECTRIC_FURNACE, Hayo.ItemTags.ELECTRIC_FURNACE_UPGRADES, menuId, container, data, inventory);

            addClassicSlots(this, container, inventory);

            this.recipeMode = this.addDataSlot(recipeMode);

            var level = inventory.player.level();
            this.recipeInputs = Map.of( //
                    ElectricFurnaceBlockEntity.ElectricFurnaceMode.NORMAL, level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT), //
                    ElectricFurnaceBlockEntity.ElectricFurnaceMode.BLASTING, level.recipeAccess().propertySet(RecipePropertySet.BLAST_FURNACE_INPUT), //
                    ElectricFurnaceBlockEntity.ElectricFurnaceMode.SMOKING, level.recipeAccess().propertySet(RecipePropertySet.SMOKER_INPUT));
        }

        public static DataSlot createRecipeModeData(ElectricFurnaceBlockEntity entity) {
            return new DataSlot() {
                @Override
                public int get() {
                    return entity.getMode().ordinal();
                }

                @Override
                public void set(int value) {
                }
            };
        }

        public ElectricFurnaceBlockEntity.ElectricFurnaceMode getRecipeMode() {
            return ElectricFurnaceBlockEntity.ElectricFurnaceMode.values()[Mth.clamp(this.recipeMode.get(), 0, 2)];
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            return this.recipeInputs.get(this.getRecipeMode()).test(stack);
        }

        @Override
        protected int getFirstPlayerSlot() {
            return SLOTS;
        }

        @Override
        public int getEnergyUseRate() {
            return ElectricFurnaceBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
        }
    }

    public static class MaceratorMenu extends ProcessingMachineMenu {
        public static final int SLOTS = MaceratorBlockEntity.SLOTS;

        public MaceratorMenu(int menuId, Inventory inventory) {
            this(menuId, new SimpleContainer(SLOTS), new SimpleContainerData(7), inventory);
        }

        public MaceratorMenu(int menuId, MaceratorBlockEntity entity, Inventory inventory) {
            this(menuId, entity, createData(entity), inventory);
        }

        protected MaceratorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
            super(Hayo.MenuTypes.MACERATOR, Hayo.ItemTags.MACERATOR_UPGRADES, menuId, container, data, inventory);

            addClassicSlots(this, container, inventory);
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            // TODO: synchronize recipe inputs, quick move only valid inputs
            return true;
        }

        @Override
        protected int getFirstPlayerSlot() {
            return SLOTS;
        }

        @Override
        public int getEnergyUseRate() {
            return MaceratorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
        }
    }

    public static class CompressorMenu extends ProcessingMachineMenu {
        public static final int SLOTS = CompressorBlockEntity.SLOTS;

        public CompressorMenu(int menuId, Inventory inventory) {
            this(menuId, new SimpleContainer(SLOTS), new SimpleContainerData(7), inventory);
        }

        public CompressorMenu(int menuId, CompressorBlockEntity entity, Inventory inventory) {
            this(menuId, entity, createData(entity), inventory);
        }

        protected CompressorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
            super(Hayo.MenuTypes.COMPRESSOR, Hayo.ItemTags.COMPRESSOR_UPGRADES, menuId, container, data, inventory);

            addClassicSlots(this, container, inventory);
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            // TODO: synchronize recipe inputs, quick move only valid inputs
            return true;
        }

        @Override
        protected int getFirstPlayerSlot() {
            return SLOTS;
        }

        @Override
        public int getEnergyUseRate() {
            return CompressorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
        }
    }

    public static class ExtractorMenu extends ProcessingMachineMenu {
        public static final int SLOTS = ExtractorBlockEntity.SLOTS;

        public ExtractorMenu(int menuId, Inventory inventory) {
            this(menuId, new SimpleContainer(SLOTS), new SimpleContainerData(7), inventory);
        }

        public ExtractorMenu(int menuId, ExtractorBlockEntity entity, Inventory inventory) {
            this(menuId, entity, createData(entity), inventory);
        }

        protected ExtractorMenu(int menuId, Container container, ContainerData data, Inventory inventory) {
            super(Hayo.MenuTypes.EXTRACTOR, Hayo.ItemTags.EXTRACTOR_UPGRADES, menuId, container, data, inventory);

            addClassicWithSecondaryOutputSlots(this, container, inventory);
        }

        @Override
        protected int getFirstPlayerSlot() {
            return SLOTS;
        }

        @Override
        protected boolean isRecipeInput(ItemStack stack) {
            // TODO: synchronize recipe inputs, quick move only valid inputs
            return true;
        }

        @Override
        public int getEnergyUseRate() {
            return ExtractorBlockEntity.ENERGY_USE_RATE * (1 + this.getOverclockCount());
        }
    }
}
