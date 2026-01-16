package io.github.reoseah.hayo.feature.electric_beacon;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ElectricBeaconOption(Identifier id, Identifier sprite, List<Component> tooltip) {
    public ElectricBeaconOption(Identifier id, Identifier sprite, Component... tooltip) {
        this(id, sprite, List.of(tooltip));
    }
}
