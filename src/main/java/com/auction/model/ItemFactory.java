package com.auction.model;

import com.example.auction.util.MoneyUtil;
import java.math.BigDecimal;

public final class ItemFactory {
    private ItemFactory() {
    }

    public static Item create(ItemCategory category, String sellerId, String title, String description,
            BigDecimal startingPrice, String brand, Integer warrantyMonths,
            String artist, String material, String manufacturer, Integer year) {
        BigDecimal normalizedPrice = MoneyUtil.normalize(startingPrice);
        return switch (category) {
            case ELECTRONICS -> new Electronics(
                    sellerId,
                    title,
                    description,
                    normalizedPrice,
                    emptyToDefault(brand, "Generic"),
                    warrantyMonths == null ? 0 : warrantyMonths);
            case ART -> new Art(
                    sellerId,
                    title,
                    description,
                    normalizedPrice,
                    emptyToDefault(artist, "Unknown artist"),
                    emptyToDefault(material, "Unknown material"));
            case VEHICLE -> new Vehicle(
                    sellerId,
                    title,
                    description,
                    normalizedPrice,
                    emptyToDefault(manufacturer, "Unknown manufacturer"),
                    year == null ? 0 : year);
        };
    }

    private static String emptyToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
