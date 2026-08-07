package io.github.reoseah.hayo.feature.electric_items;

import com.mojang.serialization.Codec;
import io.github.reoseah.hayo.Hayo;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;

public enum EnergyComponents {
    ;

    public static final DataComponentType<EnergyStorage> ENERGY_STORAGE = DataComponentType.<EnergyStorage>builder() //
            .persistent(EnergyStorage.CODEC) //
            .networkSynchronized(EnergyStorage.STREAM_CODEC) //
            .build();

    public static final DataComponentType<Integer> ENERGY = DataComponentType.<Integer>builder() //
            .persistent(Codec.INT) //
            .networkSynchronized(ByteBufCodecs.VAR_INT) //
            .ignoreSwapAnimation() //
            .build();

    public static final DataComponentType<Unit> CAN_DISCHARGE = DataComponentType.<Unit>builder() //
            .persistent(Unit.CODEC) //
            .networkSynchronized(Unit.STREAM_CODEC) //
            .build();

    public static final DataComponentType<ChargedAttributes> CHARGED_ATTRIBUTES = DataComponentType.<ChargedAttributes>builder() //
            .persistent(ChargedAttributes.CODEC) //
            .networkSynchronized(ChargedAttributes.STREAM_CODEC) //
            .build();

    public static final DataComponentType<EnergyTool> ENERGY_TOOL = DataComponentType.<EnergyTool>builder() //
            .persistent(EnergyTool.CODEC) //
            .networkSynchronized(EnergyTool.STREAM_CODEC) //
            .build();

    public static final DataComponentType<EnergyArmor> ENERGY_ARMOR = DataComponentType.<EnergyArmor>builder() //
            .persistent(EnergyArmor.CODEC) //
            .networkSynchronized(EnergyArmor.STREAM_CODEC) //
            .build();

    public static final DataComponentType<Unit> CHARGES_INVENTORY = DataComponentType.<Unit>builder() //
            .persistent(Unit.CODEC) //
            .networkSynchronized(Unit.STREAM_CODEC) //
            .build();

    public static final DataComponentType<Unit> QUANTUM_ARMOR = DataComponentType.<Unit>builder() //
            .persistent(Unit.CODEC) //
            .networkSynchronized(Unit.STREAM_CODEC) //
            .build();

    public static boolean isStorage(ItemStack stack) {
        return stack.has(ENERGY_STORAGE);
    }

    public static boolean canChargeInMachine(ItemStack stack) {
        return stack.has(ENERGY_STORAGE);
    }

    public static boolean canDischargeInMachine(ItemStack stack) {
        return stack.has(ENERGY_STORAGE) && stack.has(CAN_DISCHARGE);
    }

    public static int getEnergy(ItemStack stack) {
        return stack.getOrDefault(ENERGY, 0);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        stack.set(ENERGY, energy);
        updateEnergyComponents(stack, energy);
    }

    public static void updateEnergyComponents(ItemStack stack, int energy) {
        var chargedAttributes = stack.get(CHARGED_ATTRIBUTES);
        if (chargedAttributes != null) {
            boolean hasCharge = energy >= chargedAttributes.requiredEnergy();

            var attributes = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            boolean hasChargedAttributes = attributes == chargedAttributes.attributes();

            if (hasCharge && !hasChargedAttributes) {
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, chargedAttributes.attributes());
            } else if (!hasCharge && hasChargedAttributes) {
                stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            }
        }
    }

    public static int getCapacity(ItemStack stack) {
        var storage = stack.get(ENERGY_STORAGE);
        return storage != null ? storage.capacity() : 0;
    }

    public static int getTransferLimit(ItemStack stack) {
        var storage = stack.get(ENERGY_STORAGE);
        return storage != null ? storage.transferLimit() : 0;
    }

    /// Charge the item or do nothing if the item is not electric.
    ///
    /// @return energy that was added to the item, you probably want to remove it from your energy source
    public static int charge(int energy, ItemStack stack) {
        var storage = stack.get(ENERGY_STORAGE);
        if (storage == null) {
            return 0;
        }
        int stackEnergy = getEnergy(stack);
        int stackCapacity = storage.capacity();
        if (stackEnergy >= stackCapacity || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(stackCapacity - stackEnergy, energy), storage.transferLimit());

        setEnergy(stack, stackEnergy + change);
        return change;
    }

    /// Discharge the item or do nothing if the item is not electric.
    ///
    /// @return energy that was removed from the item, you probably want to add it to your energy storage
    public static int discharge(int amount, ItemStack stack) {
        if (!stack.has(CAN_DISCHARGE)) {
            return 0;
        }
        var storage = stack.get(ENERGY_STORAGE);
        if (storage == null) {
            return 0;
        }
        int stackEnergy = getEnergy(stack);
        if (stackEnergy <= 0 || stack.getCount() > 1) {
            return 0;
        }
        int change = Math.min(Math.min(stackEnergy, amount), storage.transferLimit());
        setEnergy(stack, stackEnergy - change);
        return change;
    }

    /// Removes the specified amount of energy from an item and returns true,
    /// otherwise returns false.
    public static boolean tryRemoveEnergy(int amount, ItemStack stack) {
        if (stack.getCount() > 1) {
            return false;
        }
        int storedEnergy = getEnergy(stack);
        if (storedEnergy < amount) {
            return false;
        }
        setEnergy(stack, storedEnergy - amount);
        return true;
    }

    public static ItemStack withEnergy(Item item, int amount) {
        var stack = new ItemStack(item);
        setEnergy(stack, amount);
        return stack;
    }

    public static ItemStack withFullEnergy(Item item) {
        var stack = new ItemStack(item);
        setEnergy(stack, getCapacity(stack));
        return stack;
    }

    public static boolean defaultIsBarVisible(ItemStack stack) {
        if (stack.getCount() != 1) {
            return false;
        }
        int energy = getEnergy(stack);
        int capacity = getCapacity(stack);
        if (energy >= capacity) {
            return false;
        }
        var isEquipment = stack.get(CHARGED_ATTRIBUTES);
        if (isEquipment != null) {
            return true;
        }
        return energy > 0;
    }

    public static int defaultBarWidth(ItemStack stack) {
        return Math.round(13F * ((float) getEnergy(stack)) / getCapacity(stack));
    }

    public static int defaultBarColor(ItemStack stack) {
        float ratio = 1F - ((float) getEnergy(stack)) / (float) getCapacity(stack);

        // from blue to red
        float hue = Mth.lerp(ratio, 240F, 360F) / 360F;
        // from 50% to 100% saturation, otherwise pure blue appears too dark
        float saturation = Mth.lerp(ratio, 0.5F, 1F);
        return Mth.hsvToRgb(hue, saturation, 1.0F);
    }

    public static int moveEnergy(ItemStack source, ItemStack target) {
        var sourceStorage = source.get(ENERGY_STORAGE);
        var targetStorage = target.get(ENERGY_STORAGE);

        if (sourceStorage == null || targetStorage == null) {
            return 0;
        }

        var transferLimit = Math.min(sourceStorage.transferLimit(), targetStorage.transferLimit());
        var sourceEnergy = getEnergy(source);
        int targetEnergy = getEnergy(target);

        var transfer = Math.min(transferLimit, Math.min(sourceEnergy, targetStorage.capacity() - targetEnergy));
        setEnergy(target, targetEnergy + transfer);
        setEnergy(source, sourceEnergy - transfer);

        return transfer;
    }

    public static int spreadEnergy(LivingEntity player, int amount, @Nullable EquipmentSlot exclude) {
        var targets = new EnumMap<EquipmentSlot, Integer>(EquipmentSlot.class);
        int targetsTotal = 0;

        for (var slot : EquipmentSlot.values()) {
            if (slot == exclude) {
                continue;
            }
            var item = player.getItemBySlot(slot);
            var storage = item.get(ENERGY_STORAGE);
            if (storage == null) {
                continue;
            }

            int limit = Math.min(storage.transferLimit(), storage.capacity() - getEnergy(item));
            if (limit > 0) {
                targets.put(slot, limit);
                targetsTotal += limit;
            }
        }

        if (targetsTotal == 0) {
            return 0;
        }

        if (amount >= targetsTotal) {
            for (var entry : targets.entrySet()) {
                var slot = entry.getKey();
                var targetStack = player.getItemBySlot(slot);
                var limit = targets.getOrDefault(slot, 0);
                setEnergy(targetStack, getEnergy(targetStack) + limit);
            }
            return targetsTotal;
        }

        int totalDistributed = 0;

        var fraction = Mth.ceil(amount / (float) targets.size());
        for (var entry : targets.entrySet()) {
            var slot = entry.getKey();
            int limit = entry.getValue();

            var targetStack = player.getItemBySlot(slot);
            int targetEnergy = getEnergy(targetStack);

            int slice = Math.min(amount - totalDistributed, Math.min(limit, fraction));
            if (slice > 0) {
                setEnergy(targetStack, targetEnergy + slice);
                targets.put(slot, limit - slice);
                totalDistributed += slice;
            }
        }

        if (amount - totalDistributed > 0) {
            for (var entry : targets.entrySet()) {
                var slot = entry.getKey();
                int limit = entry.getValue();

                var targetStack = player.getItemBySlot(slot);
                int targetEnergy = getEnergy(targetStack);

                int min = Math.min(amount - totalDistributed, limit);
                if (min > 0) {
                    setEnergy(targetStack, targetEnergy + min);
                    targets.put(slot, limit - min);
                    totalDistributed += min;
                }

                if (amount - totalDistributed == 0) {
                    break;
                }
            }
        }

        return totalDistributed;
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("energy_storage"), EnergyComponents.ENERGY_STORAGE);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("energy"), EnergyComponents.ENERGY);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("can_discharge"), EnergyComponents.CAN_DISCHARGE);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("charged_attributes"), EnergyComponents.CHARGED_ATTRIBUTES);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("energy_tool"), EnergyComponents.ENERGY_TOOL);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("energy_armor"), EnergyComponents.ENERGY_ARMOR);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("charges_inventory"), EnergyComponents.CHARGES_INVENTORY);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Hayo.modId("quantum_armor"), EnergyComponents.QUANTUM_ARMOR);

        ServerTickEvents.END_LEVEL_TICK.register(EnergyComponents::tickPlayerInventories);
    }

    private static void tickPlayerInventories(ServerLevel level) {
        for (var player : level.players()) {
            var chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.has(EnergyComponents.CHARGES_INVENTORY)) {
                var stats = chest.get(EnergyComponents.ENERGY_STORAGE);
                if (stats == null) continue;

                var limit = stats.transferLimit();
                if (limit == 0) continue;

                var energy = EnergyComponents.getEnergy(chest);
                if (energy == 0) continue;

                int moved = EnergyComponents.spreadEnergy(player, Math.min(energy, limit), EquipmentSlot.CHEST);
                EnergyComponents.setEnergy(chest, energy - moved);

                player.getInventory().setChanged();
                if (player.isCreative()) {
                    player.inventoryMenu.broadcastChanges();
                }
            }
        }
    }
}
