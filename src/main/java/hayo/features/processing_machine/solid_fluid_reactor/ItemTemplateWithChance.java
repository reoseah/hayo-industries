package hayo.features.processing_machine.solid_fluid_reactor;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

public record ItemTemplateWithChance(ItemStackTemplate template, float chance) {
    public static final MapCodec<ItemTemplateWithChance> MAP_CODEC = RecordCodecBuilder.<ItemTemplateWithChance>mapCodec(i -> i
                    .group(
                            Item.CODEC.fieldOf("id").forGetter(o -> o.template().item()),
                            ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(o -> o.template().count()),
                            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(o -> o.template().components()),
                            ExtraCodecs.floatRange(0F, 1F).fieldOf("chance").orElse(1F).forGetter(ItemTemplateWithChance::chance)
                    )
                    .apply(i, (item, count, components, chance) -> new ItemTemplateWithChance(new ItemStackTemplate(item, count, components), chance)))
            .validate(o -> {
                if (o.chance < 1F && o.template().count() > 1)
                    return DataResult.error(() -> "Recipe outputs can have either chance or count, not both", o);
                return DataResult.success(o);
            });

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTemplateWithChance> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC,
            ItemTemplateWithChance::template,
            ByteBufCodecs.FLOAT,
            ItemTemplateWithChance::chance,
            ItemTemplateWithChance::new
    );
}
