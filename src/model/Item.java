package model;

public abstract class Item {
    // Thuộc tính để protected để lớp con (Electronics, Vehicle...) dùng được
    protected String itemId;
    protected String itemName;
    protected double startingPrice;

    // Constructor (Hàm khởi tạo) mặc định
    public Item() {}

    // Getter và Setter để đảm bảo tính Encapsulation (Đóng gói)
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }
}