package io.github.reoseah.hayo.feature.electric_beacon;

import com.mojang.serialization.Codec;
import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.energy.EnergyTexts;
import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ElectricBeacon {
    public static final ElectricBeaconOption NONE_1 = new ElectricBeaconOption( //
            Hayo.modId("none_1"), //
            Hayo.modId("electric_beacon_options/cross"), //
            Component.translatable("hayo.electric_beacon.none"));
    public static final ElectricBeaconOption WIRELESS_CHARGE = new ElectricBeaconOption( //
            Hayo.modId("wireless_charge"), //
            Hayo.modId("electric_beacon_options/wireless_charge"), //
            Component.translatable("hayo.electric_beacon.wireless_charge"), //
            EnergyTexts.maxAmountPerTick(32).withStyle(ChatFormatting.GRAY), //
            Component.translatable("hayo.distance.blocks_horizontally", 10).withStyle(ChatFormatting.GRAY), //
            Component.translatable("hayo.distance.blocks_vertically", 10).withStyle(ChatFormatting.GRAY));

    public static final ElectricBeaconOption NONE_2 = new ElectricBeaconOption( //
            Hayo.modId("none_2"), //
            Hayo.modId("electric_beacon_options/cross"), //
            Component.translatable("hayo.electric_beacon.none"));
    public static final ElectricBeaconOption POWER_1 = new ElectricBeaconOption( //
            Hayo.modId("power_1"), //
            Hayo.modId("electric_beacon_options/power"), //
            Component.translatable("hayo.electric_beacon.power_1"), //
            Component.translatable("hayo.energy.max_amount_per_tick", "+32").withStyle(ChatFormatting.GRAY) //
    );
    public static final ElectricBeaconOption HORIZONTAL_RANGE_1 = new ElectricBeaconOption( //
            Hayo.modId("horizontal_range_1"), //
            Hayo.modId("electric_beacon_options/horizontal_range"), //
            Component.translatable("hayo.electric_beacon.horizontal_range_1"), //
            Component.translatable("hayo.distance.blocks", "+10").withStyle(ChatFormatting.GRAY) //
    );
    public static final ElectricBeaconOption VERTICAL_RANGE_1 = new ElectricBeaconOption( //
            Hayo.modId("vertical_range_1"), //
            Hayo.modId("electric_beacon_options/vertical_range"), //
            Component.translatable("hayo.electric_beacon.vertical_range_1"), //
            Component.translatable("hayo.distance.blocks", "+10").withStyle(ChatFormatting.GRAY) //
    );

    public static final ElectricBeaconOption NONE_3 = new ElectricBeaconOption( //
            Hayo.modId("none_3"), //
            Hayo.modId("electric_beacon_options/cross"), //
            Component.translatable("hayo.electric_beacon.none"));
    public static final ElectricBeaconOption POWER_2 = new ElectricBeaconOption( //
            Hayo.modId("power_2"), //
            Hayo.modId("electric_beacon_options/power"), //
            Component.translatable("hayo.electric_beacon.power_2"), //
            Component.translatable("hayo.energy.max_amount_per_tick", "+32").withStyle(ChatFormatting.GRAY) //
    );
    public static final ElectricBeaconOption HORIZONTAL_RANGE_2 = new ElectricBeaconOption( //
            Hayo.modId("horizontal_range_2"), //
            Hayo.modId("electric_beacon_options/horizontal_range"), //
            Component.translatable("hayo.electric_beacon.horizontal_range_2"), //
            Component.translatable("hayo.distance.blocks", "+10").withStyle(ChatFormatting.GRAY) //
    );
    public static final ElectricBeaconOption VERTICAL_RANGE_2 = new ElectricBeaconOption( //
            Hayo.modId("vertical_range_2"), //
            Hayo.modId("electric_beacon_options/vertical_range"), //
            Component.translatable("hayo.electric_beacon.vertical_range_2"), //
            Component.translatable("hayo.distance.blocks", "+10").withStyle(ChatFormatting.GRAY) //
    );

    public static final ElectricBeaconOption NONE_4 = new ElectricBeaconOption( //
            Hayo.modId("none_4"), //
            Hayo.modId("electric_beacon_options/cross"), //
            Component.translatable("hayo.electric_beacon.none") //
    );
    public static final ElectricBeaconOption POWER_3 = new ElectricBeaconOption( //
            Hayo.modId("power_3"), //
            Hayo.modId("electric_beacon_options/power"), //
            Component.translatable("hayo.electric_beacon.power_3"), //
            Component.translatable("hayo.energy.max_amount_per_tick", "+64").withStyle(ChatFormatting.GRAY) //
    );
    public static final ElectricBeaconOption HORIZONTAL_RANGE_3 = new ElectricBeaconOption( //
            Hayo.modId("horizontal_range_3"), //
            Hayo.modId("electric_beacon_options/horizontal_range"), //
            Component.translatable("hayo.electric_beacon.horizontal_range_3"), //
            Component.translatable("hayo.distance.blocks", "+20").withStyle(ChatFormatting.GRAY) //
    );
    public static final ElectricBeaconOption VERTICAL_RANGE_3 = new ElectricBeaconOption( //
            Hayo.modId("vertical_range_3"), //
            Hayo.modId("electric_beacon_options/vertical_range"), //
            Component.translatable("hayo.electric_beacon.vertical_range_3"), //
            Component.translatable("hayo.distance.blocks", "+20").withStyle(ChatFormatting.GRAY) //
    );

    public static final Map<Integer, List<ElectricBeaconOption>> OPTIONS_BY_TIER = Map.of(0, List.of(NONE_1, WIRELESS_CHARGE), //
            1, List.of(NONE_2, POWER_1, HORIZONTAL_RANGE_1, VERTICAL_RANGE_1), //
            2, List.of(NONE_3, POWER_2, HORIZONTAL_RANGE_2, VERTICAL_RANGE_2), //
            3, List.of(NONE_4, POWER_3, HORIZONTAL_RANGE_3, VERTICAL_RANGE_3));
    public static final List<ElectricBeaconOption> OPTIONS = OPTIONS_BY_TIER.values().stream().flatMap(List::stream).toList();
    public static final Map<Identifier, ElectricBeaconOption> OPTIONS_BY_ID = OPTIONS.stream().collect(Collectors.toMap(ElectricBeaconOption::id, o -> o));
    public static final Codec<ElectricBeaconOption> CODEC = Identifier.CODEC.xmap(OPTIONS_BY_ID::get, ElectricBeaconOption::id);

    public static int encodeOption(@Nullable ElectricBeaconOption option) {
        return option == null ? -1 : OPTIONS.indexOf(option);
    }

    public static @Nullable ElectricBeaconOption decodeOption(int data) {
        if (data < 0 || data >= OPTIONS.size()) {
            return null;
        }
        return OPTIONS.get(data);
    }
}
