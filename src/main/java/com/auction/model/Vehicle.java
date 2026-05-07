package com.auction.model;

public class Vehicle extends Item {
    private String brand;
    private int year;

    // 1. Constructor Đầy Đủ: Truyền 5 tham số cho Item và 2 tham số riêng của Vehicle
    public Vehicle(int id, String name, String description, double startingPrice, double currentPrice, String brand, int year) {
        // Gọi constructor 5 tham số của Item
        super(id, name, description, startingPrice, currentPrice);
        this.brand = brand;
        this.year = year;
    }

    // 2. Constructor Rút Gọn: Dùng khi bạn muốn khởi tạo nhanh
    public Vehicle(String name, double startingPrice, String brand, int year) {
        super(); // Gọi constructor mặc định Item()
        this.setName(name);
        this.setStartingPrice(startingPrice);
        this.setCurrentPrice(startingPrice);
        this.brand = brand;
        this.year = year;
    }

    // Getter và Setter
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    // 3. Ghi đè toString() để đồng bộ với cấu trúc dự án của nhóm
    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", brand='" + brand + '\'' +
                ", year=" + year +
                ", currentPrice=" + getCurrentPrice() +
                '}';
    }
}