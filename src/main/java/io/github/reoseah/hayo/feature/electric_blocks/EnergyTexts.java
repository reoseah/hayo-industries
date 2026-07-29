package io.github.reoseah.hayo.feature.electric_blocks;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/// Translation keys and formatting for HAYO energy,
/// generally identified with lowercase epsilon "ε".
public enum EnergyTexts {
    ;
    public static final String AMOUNT = "hayo.energy.amount";
    public static final String AMOUNT_AND_CAPACITY = "hayo.energy.amount_and_capacity";
    public static final String AMOUNT_PER_TICK = "hayo.energy.amount_per_tick";
    public static final String AVERAGE_AMOUNT_PER_TICK = "hayo.energy.average_amount_per_tick";
    public static final String AVERAGE_INPUT_PER_TICK = "hayo.energy.average_input_per_tick";
    public static final String AVERAGE_OUTPUT_PER_TICK = "hayo.energy.average_output_per_tick";
    public static final String MAX_AMOUNT = "hayo.energy.max_amount";
    public static final String MAX_AMOUNT_PER_TICK = "hayo.energy.max_amount_per_tick";
    public static final String AMOUNT_AND_PERCENTAGE = "hayo.energy.amount_and_percentage";
    public static final String AMOUNT_WITH_CAPACITY_AND_PERCENTAGE = "hayo.energy.amount_with_capacity_and_percentage";
    public static final String DURATION_AT_AMOUNT_PER_TICK = "hayo.energy.duration_at_amount_per_tick";
    public static final String CONVERSION = "hayo.energy.conversion_per_tick";
    public static final String APPROXIMATE_AMOUNT = "hayo.energy.approximate_amount";

    private static final DecimalFormat LARGE_AMOUNTS_FORMAT;

    static {
        LARGE_AMOUNTS_FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.ROOT);
        LARGE_AMOUNTS_FORMAT.setGroupingUsed(true);
        LARGE_AMOUNTS_FORMAT.setGroupingSize(3);
        var symbols = LARGE_AMOUNTS_FORMAT.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        LARGE_AMOUNTS_FORMAT.setDecimalFormatSymbols(symbols);
    }

    /// Format energy amount, grouping digits with commas for large numbers.
    /// <aside>
    /// Not using suffixes like "M", like TechReborn and so many tech mods,
    /// it has wrong "vibe". (If you have to show truly large numbers, use
    /// scientific notation.)
    /// </aside>
    public static String formatAmount(long amount) {
        return amount < 10000 ? String.valueOf(amount) : LARGE_AMOUNTS_FORMAT.format(amount);
    }

    /// E.g.: `1000 ε`, after 10,000 group with commas - `1,000,000 ε`.
    public static MutableComponent amount(long amount) {
        return Component.translatable(AMOUNT, formatAmount(amount));
    }

    /// E.g.: `500,000 / 1,000,000 ε`.
    public static MutableComponent amountAndCapacity(long amount, long capacity) {
        return Component.translatable(AMOUNT_AND_CAPACITY, formatAmount(amount), formatAmount(capacity));
    }

    /// E.g.: `100 ε/t`.
    public static MutableComponent amountPerTick(long amount) {
        return Component.translatable(AMOUNT_PER_TICK, formatAmount(amount));
    }

    /// E.g.: `+42.5 avg. ε/t`. Make sure to round amount to one or two digits.
    public static MutableComponent averageAmountPerTick(float amount) {
        return Component.translatable(AVERAGE_AMOUNT_PER_TICK, (amount > 0 ? "+" : "") + amount);
    }

    public static MutableComponent averageInputPerTick(float amount) {
        return Component.translatable(AVERAGE_INPUT_PER_TICK, (amount > 0 ? "+" : "") + amount);
    }

    public static MutableComponent averageOutputPerTick(float amount) {
        if (amount != 0) amount = -amount;
        return Component.translatable(AVERAGE_OUTPUT_PER_TICK, (amount > 0 ? "+" : "") + amount);
    }

    public static MutableComponent maxAmount(long amount) {
        return Component.translatable(MAX_AMOUNT, formatAmount(amount));
    }

    public static MutableComponent maxAmountPerTick(long amount) {
        return Component.translatable(MAX_AMOUNT_PER_TICK, amount);
    }

    public static MutableComponent amountAndPercentage(long amount, long capacity) {
        if (capacity == 0) {
            return Component.translatable(AMOUNT_AND_PERCENTAGE, formatAmount(amount), 0);
        }
        long percentage = 100 * amount / capacity;
        return Component.translatable(AMOUNT_AND_PERCENTAGE, formatAmount(amount), percentage);
    }

    public static MutableComponent amountWithCapacityAndPercentage(long amount, long capacity) {
        if (capacity == 0) {
            return Component.translatable(AMOUNT_WITH_CAPACITY_AND_PERCENTAGE, formatAmount(amount), formatAmount(capacity), 0);
        }
        long percentage = 100 * amount / capacity;
        return Component.translatable(AMOUNT_WITH_CAPACITY_AND_PERCENTAGE, formatAmount(amount), formatAmount(capacity), percentage);
    }

    public static MutableComponent durationAtAmountPerTick(float duration, int amountPerTick) {
        return Component.translatable(DURATION_AT_AMOUNT_PER_TICK, duration, amountPerTick);
    }

    public static MutableComponent conversionRate(long amount) {
        return Component.translatable(CONVERSION, formatAmount(amount));
    }

    /// E.g.: "≈ 1000 ε", used by Generator in tooltips
    public static MutableComponent approximateAmount(long amount) {
        return Component.translatable(APPROXIMATE_AMOUNT, formatAmount(amount));
    }
}
