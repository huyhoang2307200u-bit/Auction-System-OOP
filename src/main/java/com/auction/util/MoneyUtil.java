package com.auction.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyUtil {
    private static final DecimalFormat VND_FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        VND_FORMATTER = new DecimalFormat("#,##0", symbols);
        VND_FORMATTER.setRoundingMode(RoundingMode.HALF_UP);
    }

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

    public static String formatVnd(BigDecimal value) {
        BigDecimal normalized = normalize(value).setScale(0, RoundingMode.HALF_UP);
        synchronized (VND_FORMATTER) {
            return VND_FORMATTER.format(normalized) + "VND";
        }
    }

    public static String formatVnd(double value) {
        return formatVnd(fromDouble(value));
    }

    public static BigDecimal parseUserAmount(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new NumberFormatException("Số tiền không được để trống.");
        }

        String cleaned = rawValue.trim()
                .toUpperCase(Locale.ROOT)
                .replace("VND", "")
                .replace("VNĐ", "")
                .replace("Đ", "")
                .replace(" ", "")
                .replace("_", "");

        if (cleaned.isBlank()) {
            throw new NumberFormatException("Số tiền không hợp lệ.");
        }

        // Người dùng thường nhập 1.000.000 hoặc 1,000,000 để phân tách hàng nghìn.
        // Nếu chỉ có dấu phẩy ở 1-2 chữ số cuối, coi đó là dấu thập phân; ngược lại coi là phân tách hàng nghìn.
        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');
        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                cleaned = cleaned.replace(".", "").replace(',', '.');
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (lastComma >= 0) {
            int digitsAfterComma = cleaned.length() - lastComma - 1;
            if (digitsAfterComma > 0 && digitsAfterComma <= 2) {
                cleaned = cleaned.replace(',', '.');
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (lastDot >= 0) {
            int digitsAfterDot = cleaned.length() - lastDot - 1;
            if (digitsAfterDot == 3) {
                cleaned = cleaned.replace(".", "");
            }
        }

        return normalize(new BigDecimal(cleaned));
    }
}
