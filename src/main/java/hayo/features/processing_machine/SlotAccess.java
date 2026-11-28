package hayo.features.processing_machine;

import net.minecraft.util.StringRepresentable;

public enum SlotAccess implements StringRepresentable {
    NONE("none"),
    INPUT("input"),
    OUTPUT("output"),
    BOTH("both"),
    OTHER("other");

    public static final StringRepresentable.EnumCodec<SlotAccess> CODEC = StringRepresentable.fromEnum(SlotAccess::values);

    public final String name;

    SlotAccess(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static SlotAccess ofOrdinal(int index) {
        return values()[index % values().length];
    }
}
