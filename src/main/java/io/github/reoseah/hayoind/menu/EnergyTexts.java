package io.github.reoseah.hayoind.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.util.Locale;

/// Default translation keys and formatting for HAYO energy,
/// generally identified with lowercase epsilon "ε".
public class EnergyTexts {
    private static final DecimalFormat LARGE_AMOUNTS_FORMAT;

    static {
        LARGE_AMOUNTS_FORMAT = (DecimalFormat) DecimalFormat.getInstance(Locale.ROOT);
        LARGE_AMOUNTS_FORMAT.setGroupingUsed(true);
        LARGE_AMOUNTS_FORMAT.setGroupingSize(3);
        var symbols = LARGE_AMOUNTS_FORMAT.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        LARGE_AMOUNTS_FORMAT.setDecimalFormatSymbols(symbols);
    }

    private static final String AMOUNT_KEY = "hayoind.energy.amount";
    private static final String AMOUNT_AND_CAPACITY_KEY = "hayoind.energy.amount_and_capacity";
    private static final String AMOUNT_PER_TICK_KEY = "hayoind.energy.amount_per_tick";
    private static final String AVERAGE_AMOUNT_PER_TICK_KEY = "hayoind.energy.average_amount_per_tick";
    private static final String AMOUNT_PER_USE_KEY = "hayoind.energy.amount_per_use";
    private static final String FUEL_VALUE_KEY = "hayoind.energy.fuel_value";
    private static final String APPROXIMATE_AMOUNT_KEY = "hayoind.energy.approximate_amount";

    /// "Energy"
    public static final Component ENERGY = Component.translatable("hayoind.energy");
    /// "Stored Energy", used for tooltip when hovering over energy bar in storage blocks.
    public static final Component STORED_ENERGY = Component.translatable("hayoind.energy.stored");

    /// Format energy amount, grouping digits with commas for large numbers.
    /// <aside>
    /// Not using suffixes like "M", like TechReborn and so many tech mods,
    /// it has wrong "vibe". If you have to show truly large numbers, use
    /// scientific notation.
    /// </aside>
    public static String formatAmount(long amount) {
        return amount < 10000 ? String.valueOf(amount) : LARGE_AMOUNTS_FORMAT.format(amount);
    }

    /// E.g.: "1000 ε", after 10000 group with commas - "1,000,000 ε".
    public static MutableComponent amount(long amount) {
        return Component.translatable(AMOUNT_KEY, formatAmount(amount));
    }

    /// E.g.: "500,000 / 1,000,000 ε"
    public static MutableComponent amountAndCapacity(long amount, long capacity) {
        return Component.translatable(AMOUNT_AND_CAPACITY_KEY, formatAmount(amount), formatAmount(capacity));
    }

    /// E.g.: "100 ε/t"
    public static MutableComponent amountPerTick(long amount) {
        return Component.translatable(AMOUNT_PER_TICK_KEY, formatAmount(amount));
    }

    /// E.g.: "+42.5 avg. ε/t". Doesn't round the value, make sure to round it yourself to one or two digits.
    public static MutableComponent averageAmountPerTick(float amount) {
        return Component.translatable(AVERAGE_AMOUNT_PER_TICK_KEY, (amount > 0 ? "+" : "") + amount);
    }

    /// E.g.: "100 ε per use"
    public static MutableComponent amountPerUse(long amount) {
        return Component.translatable(AMOUNT_PER_USE_KEY, formatAmount(amount));
    }

    /// E.g.: "1000 ε fuel value", used by basic generator screen in item tooltips
    public static MutableComponent fuelValue(long amount) {
        return Component.translatable(FUEL_VALUE_KEY, formatAmount(amount));
    }

    /// E.g.: "≈ 1000 ε", used by basic generator screen in tooltip over the fuel gauge
    public static MutableComponent approximateAmount(long amount) {
        return Component.translatable(APPROXIMATE_AMOUNT_KEY, formatAmount(amount));
    }
}
