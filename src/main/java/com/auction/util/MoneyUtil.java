package com.auction.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {
    private MoneyUtil() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return safeValue.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal fromDouble(double value) {
        return normalize(BigDecimal.valueOf(value));
    }

    public static double toDouble(BigDecimal value) {
        return normalize(value).doubleValue();
    }
}
