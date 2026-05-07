package factory;

import model.*;

public class ItemFactory {
    public static Item createItem(String type, String name) {
        if (type.equalsIgnoreCase("Electronics")) return new Electronics(name);
        if (type.equalsIgnoreCase("Vehicle")) return new Vehicle(name);
        return null;
    }
}