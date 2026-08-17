package hayo.energy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hayo.energy.impl.HayoEnergy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;

public record AttributesWhenCharged(ItemAttributeModifiers attributes, int requiredEnergy) {
    public static final Codec<AttributesWhenCharged> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    ItemAttributeModifiers.CODEC.fieldOf("attributes").forGetter(AttributesWhenCharged::attributes),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("required_energy").forGetter(AttributesWhenCharged::requiredEnergy)
            ).apply(instance, AttributesWhenCharged::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributesWhenCharged> STREAM_CODEC = StreamCodec.composite(
            ItemAttributeModifiers.STREAM_CODEC,
            AttributesWhenCharged::attributes,
            ByteBufCodecs.VAR_INT,
            AttributesWhenCharged::requiredEnergy,
            AttributesWhenCharged::new);

    public static AttributesWhenCharged tool(float attackDamage, float attackSpeed, int requiredEnergy) {
        var builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

        return new AttributesWhenCharged(builder.build(), requiredEnergy);
    }

    public static AttributesWhenCharged armor(ArmorType type, int armor, int toughness, int requiredEnergy) {
        var slot = EquipmentSlotGroup.bySlot(type.getSlot());

        var builder = ItemAttributeModifiers.builder();
        var id = HayoEnergy.modId("armor." + type.getName());
        builder.add(Attributes.ARMOR, new AttributeModifier(id, armor, AttributeModifier.Operation.ADD_VALUE), slot);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, toughness, AttributeModifier.Operation.ADD_VALUE), slot);

        return new AttributesWhenCharged(builder.build(), requiredEnergy);
    }
}
