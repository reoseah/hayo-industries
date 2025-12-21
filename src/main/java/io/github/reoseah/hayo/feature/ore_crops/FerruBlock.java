package io.github.reoseah.hayo.feature.ore_crops;

import com.mojang.serialization.MapCodec;

public class FerruBlock extends OreCropBlock {
    public static final MapCodec<FerruBlock> CODEC = simpleCodec(FerruBlock::new);

    @Override
    public MapCodec<FerruBlock> codec() {
        return CODEC;
    }

    public FerruBlock(Properties properties) {
        super(properties);
    }

//    @Override
//    protected ItemLike getBaseSeedId() {
//        return Items.CARROT;
//    }
}
