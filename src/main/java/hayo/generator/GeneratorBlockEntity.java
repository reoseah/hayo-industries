package hayo.generator;

import hayo.Hayo;
import hayo.common.block.HorizontalDirectionalElectricalBlock;
import hayo.energy.block.EnergyGrid;
import hayo.old_menus.FuelBar;
import hayo.old_menus.SpriteElement;
import hayo.old_menus.StorageEnergyBar;
import hayo.old_menus.UniversalContainerMenu;
import hayo.common.blockentity.SimpleContainerBlockEntity;
import lombok.Getter;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class GeneratorBlockEntity extends SimpleContainerBlockEntity implements WorldlyContainer, ExtendedMenuProvider<BlockPos> {
    public static final int FUEL_CONSUMPTION_RATE = 2;
    public static final int ENERGY_PER_FUEL_TICK = 5;
    public static final int GENERATION_RATE = FUEL_CONSUMPTION_RATE * ENERGY_PER_FUEL_TICK;
    public static final int CAPACITY = 10000;
    public static final int TRANSFER_LIMIT = 32;

    @Getter
    protected int storedEnergy;
    @Getter
    protected int fuelEnergyLeft;
    @Getter
    protected int fuelEnergyTotal;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.GENERATOR, pos, state, NonNullList.withSize(1, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity entity) {
        boolean wasBurning = entity.fuelEnergyLeft > 0;

        if (!wasBurning && entity.canConsumeFuel() && entity.storedEnergy < CAPACITY) {
            entity.tryConsumeFuel();
        }

        if (entity.fuelEnergyLeft > 0) {
            var generation = Math.max(1, Math.min(Math.min(entity.fuelEnergyLeft, GENERATION_RATE), CAPACITY - entity.storedEnergy));
            entity.fuelEnergyLeft -= generation;
            entity.storedEnergy += generation;
            if (entity.storedEnergy > CAPACITY) {
                entity.storedEnergy = CAPACITY;
            }
            entity.setChanged();
        }

        if (entity.storedEnergy > 0) {
            int sendable = Math.min(entity.storedEnergy, TRANSFER_LIMIT);
            int sent = EnergyGrid.trySendToAllSides(sendable, (ServerLevel) level, pos);
            if (sent > 0) {
                entity.storedEnergy -= sent;
                entity.setChanged();
            }
        }

        boolean isBurning = entity.fuelEnergyLeft > 0;
        if (isBurning != wasBurning) {
            level.setBlockAndUpdate(pos, state.setValue(HorizontalDirectionalElectricalBlock.LIT, isBurning));
        }
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.hayo.generator");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return new UniversalContainerMenu(menuId, this) //
                .addSlotChainable(new Slot(this, 0, 62, 54)) // TODO: only accept fuels?
                .addStandardInventorySlotsChainable(playerInventory) //
                .addQuickMoveRule(0, 1, stack -> this.level.fuelValues().isFuel(stack) && !stack.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) //
                .addDataSlotsChainable(new GeneratorData(this)) //
                .addElement(new FuelBar(62, 37, this::getFuelEnergyLeft, this::getFuelEnergyTotal)) //
                .addElement(new StorageEnergyBar(88, 16, this::getStoredEnergy, () -> CAPACITY)) //
                .addElement(SpriteElement.smallArrowRight(79, 44));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("stored_energy", this.storedEnergy);
        output.putInt("fuel_energy_left", this.fuelEnergyLeft);
        output.putInt("fuel_energy_total", this.fuelEnergyTotal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedEnergy = input.getIntOr("stored_energy", 0);
        this.fuelEnergyLeft = input.getIntOr("fuel_energy_left", 0);
        this.fuelEnergyTotal = input.getIntOr("fuel_energy_total", 0);
    }

    protected boolean canConsumeFuel() {
        return this.level != null //
                && !this.getItem(0).is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS) //
                && FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).isFuel(this.getItem(0));
    }

    protected void tryConsumeFuel() {
        if (this.level == null) {
            return;
        }
        var fuel = this.getItem(0);
        if (fuel.is(Hayo.ItemTags.DISABLED_GENERATOR_FUELS)) {
            return;
        }
        int fuelValue = FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).burnDuration(fuel);
        if (fuelValue <= 0) {
            return;
        }

        var fuelRemainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty() && fuelRemainder != null) {
            this.setItem(0, fuelRemainder.create());
        }

        this.fuelEnergyTotal = this.fuelEnergyLeft = fuelValue * ENERGY_PER_FUEL_TICK;
        this.setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).isFuel(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return !FuelValues.vanillaBurnTimes(this.level.registryAccess(), FeatureFlags.DEFAULT_FLAGS).isFuel(stack);
    }

    protected record GeneratorData(GeneratorBlockEntity entity) implements ContainerData {
        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> this.entity.storedEnergy & 0xFFFF;
                case 1 -> this.entity.storedEnergy >>> 16;
                case 2 -> this.entity.fuelEnergyLeft & 0xFFFF;
                case 3 -> this.entity.fuelEnergyLeft >>> 16;
                case 4 -> this.entity.fuelEnergyTotal & 0xFFFF;
                case 5 -> this.entity.fuelEnergyTotal >>> 16;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            value &= 0xFFFF;
            switch (index) {
                case 0 -> this.entity.storedEnergy = this.entity.storedEnergy & 0xFFFF_0000 | value;
                case 1 -> this.entity.storedEnergy = this.entity.storedEnergy & 0xFFFF | value << 16;
                case 2 -> this.entity.fuelEnergyLeft = this.entity.fuelEnergyLeft & 0xFFFF_0000 | value;
                case 3 -> this.entity.fuelEnergyLeft = this.entity.fuelEnergyLeft & 0xFFFF | value << 16;
                case 4 -> this.entity.fuelEnergyTotal = this.entity.fuelEnergyTotal & 0xFFFF_0000 | value;
                case 5 -> this.entity.fuelEnergyTotal = this.entity.fuelEnergyTotal & 0xFFFF | value << 16;
            }
        }
    }
}
