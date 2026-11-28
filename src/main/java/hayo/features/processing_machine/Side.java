package hayo.features.processing_machine;

import net.minecraft.util.StringRepresentable;

public enum Side implements StringRepresentable {
    FRONT("front"),
    LEFT("left"),
    RIGHT("right"),
    BACK("back"),
    TOP("top"),
    BOTTOM("bottom");

    public final String name;

    Side(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
