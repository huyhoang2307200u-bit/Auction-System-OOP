package com.auction.model;

public class Art extends Item {
    private String artist;
    private int year;

    // 1. Constructor Đầy Đủ: Dùng khi bạn có mọi thông tin từ ID đến nghệ sĩ
    public Art(int id, String name, String description, double startingPrice, double currentPrice, String artist, int year) {
        // Gọi chính xác constructor 5 tham số của Item
        super(id, name, description, startingPrice, currentPrice);
        this.artist = artist;
        this.year = year;
    }

    // 2. Constructor Rút Gọn: Dùng khi tạo nhanh một tác phẩm mới
    public Art(String name, double startingPrice, String artist, int year) {
        // Gọi constructor rỗng của Item()
        super();
        this.setName(name);
        this.setStartingPrice(startingPrice);
        this.setCurrentPrice(startingPrice); // Mặc định giá hiện tại bằng giá khởi điểm
        this.artist = artist;
        this.year = year;
    }

    // Getter và Setter
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    // 3. Ghi đè toString() thay vì printInfo() để khớp với lớp Item
    @Override
    public String toString() {
        return "Art{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", artist='" + artist + '\'' +
                ", year=" + year +
                ", currentPrice=" + getCurrentPrice() +
                '}';
    }
}