package hayo.common;

public class StringUtils {
    public static String toSubscriptDigits(float chance) {
        return String.format("%.0f", 100 * chance)
                .chars()
                .map(ch -> {
                    char[] subscriptDigits = {'₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉'};
                    if (ch >= '0' && ch <= '9') {
                        return subscriptDigits[ch - '0'];
                    }
                    return ch;
                })
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
