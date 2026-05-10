package com.auction.controller;

import com.auction.dao.ItemDAO;
import com.auction.model.ElectronicItem;
import com.auction.model.Item;
import com.auction.service.AuctionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddItemController {

    // Khớp với fx:id trong file AddItem.fxml
    @FXML private TextField nameField;
    @FXML private TextField priceField;

    // Khai báo DAO để làm việc với Database
    private final ItemDAO itemDAO = new ItemDAO();

    @FXML
    private void handleSaveItem() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();

        // 1. Kiểm tra dữ liệu trống
        if (name.isEmpty() || priceText.isEmpty()) {
            showNotification("Lỗi", "Vui lòng nhập đầy đủ thông tin tên và giá!");
            return;
        }

        try {
            // 2. Kiểm tra định dạng số cho giá tiền
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                showNotification("Lỗi", "Giá khởi điểm không được âm!");
                return;
            }

            // 3. Tạo đối tượng mới
            // ID truyền null để Database tự sinh AUTOINCREMENT
            Item newItem = new ElectronicItem(null, name, "Sản phẩm mới thêm", price, price);
            newItem.setAuctionActive(true);

            // 4. Lưu trực tiếp vào Database thông qua DAO
            if (itemDAO.insertItem(newItem)) {

                // === PHẦN QUAN TRỌNG: KÍCH HOẠT REAL-TIME ===

                // Bước A: Ép AuctionManager nạp lại danh sách từ DB vào RAM ngay lập tức
                AuctionManager.getInstance().refreshItemsFromDB();

                // Bước B: Phát tín hiệu cho TẤT CẢ các cửa sổ đang mở (Observer)
                // "NEW_ITEM" là mã thông báo để các cửa sổ khác biết cần load lại bảng
                AuctionManager.getInstance().notifyPriceChanged("NEW_ITEM", price);

                // ===========================================

                showNotification("Thành công", "Đã thêm sản phẩm: " + name);

                // 5. Đóng cửa sổ sau khi hoàn tất
                handleCancel();
            } else {
                showNotification("Lỗi", "Không thể ghi dữ liệu vào Database!");
            }

        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Giá tiền phải là một con số hợp lệ!");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Có lỗi hệ thống: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Đóng cửa sổ hiện tại một cách an toàn
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Không thể đóng cửa sổ: " + e.getMessage());
        }
    }

    // Hàm thông báo dùng chung
    private void showNotification(String title, String message) {
        Alert alert = new Alert(title.equals("Lỗi") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Dùng show() để không chặn luồng xử lý nếu cần
    }
}