package com.auction.model;

public class Electronics extends Item {
    private String brand;
    private int warranty; // tháng bảo hành

    // 1. Constructor Đầy Đủ: Truyền đủ 5 tham số lên cho lớp cha, và 2 tham số cho con
    public Electronics(int id, String name, String description, double startingPrice, double currentPrice, String brand, int warranty) {
        // Lệnh super NÀY MỚI ĐÚNG vì nó gọi chính xác constructor 5 tham số của Item
        super(id, name, description, startingPrice, currentPrice);
        this.brand = brand;
        this.warranty = warranty;
    }

    // 2. Constructor Rút Gọn: Nếu bạn vẫn muốn tạo nhanh đồ điện tử chỉ cần Tên, Giá, Hãng
    public Electronics(String name, double startingPrice, String brand, int warranty) {
        // Tự động gọi super() (không tham số) ngầm định của Item
        // Sau đó dùng Setter để gán các giá trị cần thiết
        this.setName(name);
        this.setStartingPrice(startingPrice);
        this.setCurrentPrice(startingPrice); // Mới tạo thì giá hiện tại = giá khởi điểm
        this.brand = brand;
        this.warranty = warranty;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getWarranty() { return warranty; }
    public void setWarranty(int warranty) { this.warranty = warranty; }
    @Override
    public String toString() {
        return "Electronics{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", startingPrice=" + getStartingPrice() +
                ", currentPrice=" + getCurrentPrice() +
                ", brand='" + brand + '\'' +
                ", warranty=" + warranty + " months" +
                '}';
    }
}