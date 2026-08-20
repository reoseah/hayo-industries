package hayo.processing_machine.classic;

import hayo.common.IntRange;
import hayo.common.menuslot.ResultSlot;
import hayo.common.menuslot.TagFilteredSlot;
import hayo.energy.item.EnergyComponents;
import hayo.old_menus.MachineEnergyBar;
import hayo.old_menus.SpriteElement;
import hayo.old_menus.UniversalContainerMenu;
import hayo.processing_machine.UpgradableMachineBlockEntity;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class ClassicMachineBlockEntity<R extends Recipe<SingleRecipeInput>> extends UpgradableMachineBlockEntity<R, SingleRecipeInput> implements WorldlyContainer, ExtendedMenuProvider<BlockPos> {
    public static final int SLOTS = 7;

    public static final int INPUT = 0;
    public static final int BATTERY = 1;
    public static final int OUTPUT = 2;
    public static final int FIRST_UPGRADE = 3;
    public static final int UPGRADES = 4;

    protected ClassicMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    @Override
    protected IntRange getUpgradeSlots() {
        return new IntRange(FIRST_UPGRADE, FIRST_UPGRADE + UPGRADES);
    }

    @Override
    public boolean isInputSlot(int slot) {
        return slot == INPUT;
    }

    @Override
    public SingleRecipeInput createRecipeInput() {
        return new SingleRecipeInput(this.stacks.get(INPUT));
    }

    @Override
    public boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<R> recipe, SingleRecipeInput input) {
        if (recipe == null || input.isEmpty()) {
            return false;
        }
        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.inputCount > input.item().getCount()) {
            return false;
        }

        var recipeOutput = recipe.value().assemble(input);
        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            recipeOutput.setCount(recipeOutput.getCount() + 1);
        }
        return this.canInsertToSlot(recipeOutput, OUTPUT);
    }

    @Override
    public void craft(RegistryAccess registryAccess, RecipeHolder<R> recipe, SingleRecipeInput input) {
        var inputStack = this.stacks.get(INPUT);
        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe) {
            inputStack.shrink(machineRecipe.inputCount);
        } else {
            inputStack.shrink(1);
        }
        var recipeOutput = recipe.value().assemble(input);
        if (recipe.value() instanceof ClassicMachineRecipe machineRecipe && machineRecipe.extraResultChance > 0) {
            if (this.level.getRandom().nextFloat() < machineRecipe.extraResultChance) {
                recipeOutput.setCount(recipeOutput.getCount() + 1);
            }
        }

        var outputStack = this.stacks.get(OUTPUT);
        if (outputStack.isEmpty()) {
            this.stacks.set(OUTPUT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> new int[]{INPUT};
            case DOWN -> new int[]{OUTPUT};
            default -> new int[]{BATTERY};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction side) {
        return switch (side) {
            case null -> index != OUTPUT;
            case UP -> true;
            case DOWN -> false;
            default -> EnergyComponents.isStorage(stack);
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
        return side != Direction.UP;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public UniversalContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new UniversalContainerMenu(containerId, this) //
                .addSlotChainable(new Slot(this, INPUT, 47, 18)) //
                .addSlotChainable(new Slot(this, BATTERY, 47, 54)) //
                .addSlotChainable(new ResultSlot(this, OUTPUT, 107, 36)) //
                .addSlotChainable(new TagFilteredSlot(this, 3, 152, 8, this.getUpgradeTag())) //
                .addSlotChainable(new TagFilteredSlot(this, 4, 152, 26, this.getUpgradeTag())) //
                .addSlotChainable(new TagFilteredSlot(this, 5, 152, 44, this.getUpgradeTag())) //
                .addSlotChainable(new TagFilteredSlot(this, 6, 152, 62, this.getUpgradeTag())) //
                .addStandardInventorySlotsChainable(inventory) //
                .addQuickMoveRule(3, 7, stack -> stack.is(this.getUpgradeTag())) //
                .addQuickMoveRule(INPUT, INPUT + 1, this::isRecipeInput) //
                .addQuickMoveRule(BATTERY, BATTERY + 1, EnergyComponents::canChargeMachine) //
                .setRecipeTransferData(this::getRecipeType, INPUT, INPUT + 1) //
                .addDataSlotsChainable(new ClassicMachineData(this)) //
                .addElement(new MachineEnergyBar(48, 37, this::getStoredEnergy, this::getEnergyCapacity)) //
                .addElement(SpriteElement.outputSlot(103, 32)) //
                .addElement(SpriteElement.upgradeSlot(151, 7)) //
                .addElement(SpriteElement.upgradeSlot(151, 25)) //
                .addElement(SpriteElement.upgradeSlot(151, 43)) //
                .addElement(SpriteElement.upgradeSlot(151, 61));
    }

    protected abstract TagKey<Item> getUpgradeTag();

    protected boolean isRecipeInput(ItemStack stack) {
        return this.level //
                .recipeAccess() //
                .getSynchronizedRecipes() //
                .getFirstMatch(this.getRecipeType(), new SingleRecipeInput(stack), this.level) //
                .isPresent();
    }

    protected int lastOrDefaultRecipeEnergy;

    @Override
    public int getLastOrDefaultRecipeCost() {
        return this.level.isClientSide() ? this.lastOrDefaultRecipeEnergy : super.getLastOrDefaultRecipeCost();
    }

    public record ClassicMachineData(ClassicMachineBlockEntity<?> entity) implements ContainerData {
        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> this.entity.storedEnergy & 0xFFFF;
                case 1 -> this.entity.storedEnergy >>> 16;
                case 2 -> this.entity.recipeProgress & 0xFFFF;
                case 3 -> this.entity.recipeProgress >>> 16;
                case 4 -> this.entity.getLastOrDefaultRecipeCost() & 0xFFFF;
                case 5 -> this.entity.getLastOrDefaultRecipeCost() >>> 16;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            value &= 0xFFFF;
            switch (index) {
                case 0 -> this.entity.storedEnergy = this.entity.storedEnergy & 0xFFFF_0000 | value;
                case 1 -> this.entity.storedEnergy = this.entity.storedEnergy & 0xFFFF | value << 16;
                case 2 -> this.entity.recipeProgress = this.entity.recipeProgress & 0xFFFF_0000 | value;
                case 3 -> this.entity.recipeProgress = this.entity.recipeProgress & 0xFFFF | value << 16;
                case 4 ->
                        this.entity.lastOrDefaultRecipeEnergy = this.entity.lastOrDefaultRecipeEnergy & 0xFFFF_0000 | value;
                case 5 ->
                        this.entity.lastOrDefaultRecipeEnergy = this.entity.lastOrDefaultRecipeEnergy & 0xFFFF | value << 16;
            }
        }
    }
}
