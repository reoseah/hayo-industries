package io.github.reoseah.hayo.feature.electric_items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;

/// @see EnergyComponents#CHARGED_ATTRIBUTES
public record ChargedAttributes(ItemAttributeModifiers attributes, int requiredEnergy) {
    public static final Codec<ChargedAttributes> CODEC = RecordCodecBuilder.create(instance -> instance //
            .group( //
                    ItemAttributeModifiers.CODEC.fieldOf("attributes").forGetter(ChargedAttributes::attributes), //
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("required_energy").forGetter(ChargedAttributes::requiredEnergy) //
            ).apply(instance, ChargedAttributes::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChargedAttributes> STREAM_CODEC = StreamCodec.composite( //
            ItemAttributeModifiers.STREAM_CODEC, //
            ChargedAttributes::attributes, //
            ByteBufCodecs.VAR_INT, //
            ChargedAttributes::requiredEnergy, //
            ChargedAttributes::new);

    public static ChargedAttributes tool(float attackDamage, float attackSpeed, int requiredEnergy) {
        var builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        var attributes = builder.build();

        return new ChargedAttributes(attributes, requiredEnergy);
    }

    public static ChargedAttributes armor(ArmorType type, int armor, int toughness, int requiredEnergy) {
        var builder = ItemAttributeModifiers.builder();
        var slot = EquipmentSlotGroup.bySlot(type.getSlot());
        var modifierId = Identifier.withDefaultNamespace("armor." + type.getName());
        builder.add(Attributes.ARMOR, new AttributeModifier(modifierId, armor, AttributeModifier.Operation.ADD_VALUE), slot);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, toughness, AttributeModifier.Operation.ADD_VALUE), slot);
        var attributes = builder.build();

        return new ChargedAttributes(attributes, requiredEnergy);
    }
}
