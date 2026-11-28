package hayo.features.processing_machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SlotAccessState(SlotAccess left, SlotAccess right, SlotAccess back, SlotAccess top, SlotAccess bottom) {
    public static final Codec<SlotAccessState> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SlotAccess.CODEC.fieldOf("left").forGetter(SlotAccessState::left),
                    SlotAccess.CODEC.fieldOf("right").forGetter(SlotAccessState::right),
                    SlotAccess.CODEC.fieldOf("back").forGetter(SlotAccessState::back),
                    SlotAccess.CODEC.fieldOf("top").forGetter(SlotAccessState::top),
                    SlotAccess.CODEC.fieldOf("bottom").forGetter(SlotAccessState::bottom)
            )
            .apply(instance, SlotAccessState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotAccessState> STREAM_CODEC = StreamCodec.of(
            (output, value) -> {
                output.writeByte(value.left().ordinal());
                output.writeByte(value.right().ordinal());
                output.writeByte(value.back().ordinal());
                output.writeByte(value.top().ordinal());
                output.writeByte(value.bottom().ordinal());
            },
            input -> new SlotAccessState(
                    SlotAccess.ofOrdinal(input.readByte()),
                    SlotAccess.ofOrdinal(input.readByte()),
                    SlotAccess.ofOrdinal(input.readByte()),
                    SlotAccess.ofOrdinal(input.readByte()),
                    SlotAccess.ofOrdinal(input.readByte())
            ));

    public static final SlotAccessState DEFAULT = new SlotAccessState(
            SlotAccess.OTHER,
            SlotAccess.OTHER,
            SlotAccess.OTHER,
            SlotAccess.INPUT,
            SlotAccess.OUTPUT
    );

    public SlotAccess getSide(Direction facing, Direction side) {
        if (side == Direction.UP) {
            return this.top;
        } else if (side == Direction.DOWN) {
            return this.bottom;
        } else if (side == facing.getClockWise()) {
            return this.left;
        } else if (side == facing.getCounterClockWise()) {
            return this.right;
        } else if (side == facing.getOpposite()) {
            return this.back;
        }
        return SlotAccess.NONE;
    }
}
