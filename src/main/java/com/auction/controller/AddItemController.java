package com.auction.controller;

import com.auction.dao.ItemDAO;
import com.auction.model.ElectronicItem; // Cần thiết để khởi tạo đối tượng cụ thể
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

            // 3. Tạo đối tượng mới (ID truyền null để Database tự sinh AUTOINCREMENT)
            Item newItem = new ElectronicItem(null, name, "Sản phẩm mới thêm", price, price);
            newItem.setAuctionActive(true);

            // 4. Lưu trực tiếp vào Database thông qua DAO
            if (itemDAO.insertItem(newItem)) {

                // 5. Kích hoạt Observer để màn hình chính (AuctionList) tự động cập nhật bảng
                // Chúng ta truyền null cho ID vì đây là hàng mới, Observer sẽ hiểu để load lại toàn bộ
                AuctionManager.getInstance().notifyPriceChanged(null, price);

                showNotification("Thành công", "Đã thêm sản phẩm: " + name);

                // 6. Đóng cửa sổ sau khi hoàn tất
                handleCancel();
            } else {
                showNotification("Lỗi", "Không thể ghi dữ liệu vào Database!");
            }

        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Giá tiền phải là một con số hợp lệ!");
        } catch (Exception e) {
            showNotification("Lỗi", "Có lỗi hệ thống: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    // Hàm thông báo dùng chung cho cả Lỗi và Thành công
    private void showNotification(String title, String message) {
        Alert alert = new Alert(title.equals("Lỗi") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}