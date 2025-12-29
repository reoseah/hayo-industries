package io.github.reoseah.hayo.feature.ore_crops;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class FerruBlock extends OreCropBlock {
    public static final MapCodec<FerruBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance //
            .group(TagKey.codec(Registries.ITEM).fieldOf("ore_fertilizers").forGetter(trapDoorBlock -> trapDoorBlock.oreFertilizers), //
                    propertiesCodec()) //
            .apply(instance, FerruBlock::new));

    @Override
    public MapCodec<FerruBlock> codec() {
        return CODEC;
    }

    public FerruBlock(TagKey<Item> oreFertilizers, Properties properties) {
        super(oreFertilizers, properties);
    }
}
