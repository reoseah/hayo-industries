package io.github.reoseah.hayoind;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class EnergyFormatting {
    private static final String AMOUNT_AND_CAPACITY_KEY = "hayoind.energy.amount_and_capacity";

    private static final DecimalFormat LARGE_AMOUNTS_FORMAT;

    static {
        LARGE_AMOUNTS_FORMAT = (DecimalFormat) DecimalFormat.getInstance(Locale.ROOT);
        LARGE_AMOUNTS_FORMAT.setGroupingUsed(true);
        LARGE_AMOUNTS_FORMAT.setGroupingSize(3);
        DecimalFormatSymbols symbols = LARGE_AMOUNTS_FORMAT.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        LARGE_AMOUNTS_FORMAT.setDecimalFormatSymbols(symbols);
    }

    /**
     * E.g.: "1000 ε", "1,000,000 ε".
     */
    public static String formatEnergy(long amount) {
        return amount < 10000 ? String.valueOf(amount) : LARGE_AMOUNTS_FORMAT.format(amount);
    }

    /**
     * E.g.: "500,000 / 1,000,000 ε"
     */
    public static MutableComponent energyAndCapacity(long amount, long capacity) {
        return Component.translatable(AMOUNT_AND_CAPACITY_KEY, formatEnergy(amount), formatEnergy(capacity));
    }
}
