package hayo.energy;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/// Translation keys and formatting for HAYO energy, using lowercase epsilon "ε".
public class EnergyTexts {
    public static final String AMOUNT = "hayo.energy.amount";
    public static final String AMOUNT_AND_CAPACITY = "hayo.energy.amount_and_capacity";
    public static final String AMOUNT_PER_TICK = "hayo.energy.amount_per_tick";
    public static final String AMOUNT_PER_USE = "hayo.energy.amount_per_use";
    public static final String AMOUNT_AND_AMOUNT_PER_TICK = "hayo.energy.amount_and_amount_per_tick";
    public static final String AVERAGE_AMOUNT_PER_TICK = "hayo.energy.average_amount_per_tick";
    public static final String AVERAGE_INPUT_PER_TICK = "hayo.energy.average_input_per_tick";
    public static final String AVERAGE_OUTPUT_PER_TICK = "hayo.energy.average_output_per_tick";
    public static final String MAX_AMOUNT = "hayo.energy.max_amount";
    public static final String MAX_AMOUNT_PER_TICK = "hayo.energy.max_amount_per_tick";
    public static final String AMOUNT_AND_PERCENTAGE = "hayo.energy.amount_and_percentage";
    public static final String AMOUNT_WITH_CAPACITY_AND_PERCENTAGE = "hayo.energy.amount_with_capacity_and_percentage";
    public static final String DURATION_AT_AMOUNT_PER_TICK = "hayo.energy.duration_at_amount_per_tick";

    public static final int LARGE_AMOUNT_THRESHOLD = 10000;
    private static final DecimalFormat LARGE_AMOUNT_FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.ROOT);

    static {
        LARGE_AMOUNT_FORMAT.setGroupingUsed(true);
        LARGE_AMOUNT_FORMAT.setGroupingSize(3);
        var symbols = LARGE_AMOUNT_FORMAT.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        LARGE_AMOUNT_FORMAT.setDecimalFormatSymbols(symbols);
    }

    /// Format energy amount, grouping digits with commas for large numbers.
    /// <aside>
    /// HAYO doesn't use suffixes like "K" or "M", like, e.g., TechReborn,
    /// it gives off wrong vibe. If you have to show truly large numbers, switch
    /// to scientific notation, like "1.5e15" or "1.5*10¹⁵".
    /// </aside>
    public static String formatAmount(long amount) {
        return amount < LARGE_AMOUNT_THRESHOLD ? String.valueOf(amount) : LARGE_AMOUNT_FORMAT.format(amount);
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

    /// E.g.: `100 ε per use`.
    public static MutableComponent amountPerUse(long amount) {
        return Component.translatable(AMOUNT_PER_USE, formatAmount(amount));
    }

    /// E.g.: `1000 ε at 10 ε/t`.
    public static MutableComponent amountAndAmountPerTick(long amount, long amountPerTick) {
        return Component.translatable(AMOUNT_AND_AMOUNT_PER_TICK, formatAmount(amount), formatAmount(amountPerTick));
    }

    /// E.g.: `+42.5 avg. ε/t`. Round the value to one or two digits before passing it as a parameter.
    public static MutableComponent averageAmountPerTick(float amount) {
        return Component.translatable(AVERAGE_AMOUNT_PER_TICK, (amount > 0 ? "+" : "") + amount);
    }

    /// E.g.: `+42.5 avg. ε/t in`.
    public static MutableComponent averageInputPerTick(float amount) {
        return Component.translatable(AVERAGE_INPUT_PER_TICK, (amount > 0 ? "+" : "") + amount);
    }

    /// E.g.: `-42.5 avg. ε/t out`.
    public static MutableComponent averageOutputPerTick(float amount) {
        if (amount != 0) amount = -amount;
        return Component.translatable(AVERAGE_OUTPUT_PER_TICK, (amount > 0 ? "+" : "") + amount);
    }

    /// E.g.: `1,000,000 max. ε`.
    public static MutableComponent maxAmount(long amount) {
        return Component.translatable(MAX_AMOUNT, formatAmount(amount));
    }

    /// E.g.: `512 max. ε/t`.
    public static MutableComponent maxAmountPerTick(long amount) {
        return Component.translatable(MAX_AMOUNT_PER_TICK, amount);
    }

    /// E.g.: `5000 ε (50%)`.
    public static MutableComponent amountAndPercentage(long amount, long capacity) {
        if (capacity == 0) {
            return Component.translatable(AMOUNT, formatAmount(amount));
        }
        long percentage = 100 * amount / capacity;
        return Component.translatable(AMOUNT_AND_PERCENTAGE, formatAmount(amount), percentage);
    }

    /// E.g.: `5000 / 10,000 ε (50%)`.
    public static MutableComponent amountWithCapacityAndPercentage(long amount, long capacity) {
        if (capacity == 0) {
            return Component.translatable(AMOUNT_WITH_CAPACITY_AND_PERCENTAGE, formatAmount(amount), formatAmount(capacity), 0);
        }
        long percentage = 100 * amount / capacity;
        return Component.translatable(AMOUNT_WITH_CAPACITY_AND_PERCENTAGE, formatAmount(amount), formatAmount(capacity), percentage);
    }

    /// E.g.: `10 s using 10 ε/t`.
    public static MutableComponent durationAtAmountPerTick(float duration, int amountPerTick) {
        return Component.translatable(DURATION_AT_AMOUNT_PER_TICK, duration, amountPerTick);
    }
}
