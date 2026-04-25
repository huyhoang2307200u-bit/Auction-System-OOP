package com.auction.service;

import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.Item;
import com.auction.model.User;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    // BƯỚC 1: Tạo biến static để giữ đối tượng duy nhất (Singleton)
    private static AuctionManager instance;
    private List<Item> items = new ArrayList<>();

    // BƯỚC 2: Để private constructor để không ai bên ngoài tự tạo mới được
    private AuctionManager() {
        // Thêm dòng này để bảng có dữ liệu hiển thị
        // Sửa dòng 16 và 17 trong AuctionManager.java thành:
        items.add(new Item("SP01", "Đồng hồ cổ", "Mô tả sản phẩm", 500.0, 500.0));
        items.add(new Item("SP02", "Tranh sơn dầu", "Mô tả sản phẩm", 1200.0, 1200.0));
    }

    // BƯỚC 3: Hàm lấy đối tượng duy nhất (Nhóm yêu cầu)
    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public List<Item> getAvailableItems() {
        return items;
    }

    // Thêm hàm này để kiểm tra tài khoản
    public boolean checkLogin(String username, String password) {
        // Tạm thời để admin/123, sau này nhóm sẽ thay bằng database
        return "admin".equals(username) && "123".equals(password);
    }

    // BƯỚC 4: Hàm đặt giá - Logic cực kỳ quan trọng
    public synchronized void placeBid(String itemId, double amount, User bidder) throws Exception {
        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                {
                    // KIỂM TRA LOGIC: Nếu giá đặt thấp hơn hoặc bằng giá hiện tại -> Báo lỗi
                    if (amount <= item.getCurrentPrice()) {
                        throw new Exception("Giá đặt phải lớn hơn " + item.getCurrentPrice());
                    }

                    // Nếu hợp lệ thì cập nhật giá mới
                    item.setCurrentPrice(amount);

                    // THÔNG BÁO CHO GUI (Observer)
                    // Chỗ này bạn sẽ gọi hàm notify để màn hình của các bạn khác tự nhảy số
                    System.out.println("Sản phẩm " + item.getName() + " vừa có giá mới: " + amount);
                    return;
                }
            }
        }
    }
    public User authenticate(String username, String password) {
        // 1. Tạo danh sách user giả lập (Sau này dùng Database thì xóa đoạn này đi)
        List<User> userList = new ArrayList<>();
        userList.add(new Admin(1, "Quản trị viên", "admin@gmail.com", "123", "ADMIN"));
        userList.add(new Bidder(2, "Người đấu giá", "user@gmail.com", "123", "USER"));

        // 2. Duyệt qua danh sách để tìm người dùng khớp với username và password
        for (User u : userList) {
            // Lưu ý: Tôi dùng email để làm username đăng nhập, bạn có thể thay bằng u.getUsername()
            if (u.getEmail().equals(username) && u.getPassword().equals(password)) {
                return u; // Tìm thấy thì trả về đối tượng user đầy đủ
            }
        }
        return null; // Không tìm thấy
    }
}