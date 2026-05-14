package com.auction.controller;

import com.auction.model.Item;
import com.auction.model.ItemFactory;
import com.auction.service.AuctionManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AddItemController {

    // Chỉ giữ lại nameField và priceField, đã bỏ idField
    @FXML private TextField nameField;
    @FXML private TextField priceField;

    @FXML
    public void handleSaveItem() { // Đã đổi tên hàm khớp với FXML
        try {
            // 1. Lấy dữ liệu từ các ô nhập liệu
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();

            // 2. Kiểm tra dữ liệu trống
            if (name.isEmpty() || priceText.isEmpty()) {
                showError("Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // 3. Chuyển đổi giá tiền và tạo đối tượng
            double price = Double.parseDouble(priceText);

            // ID của Item sẽ tự động được Entity sinh ra (UUID)
            // Tạm thời truyền "seller1" làm sellerId
            Item newItem = ItemFactory.createItem("ELECTRONICS", "seller1", name, price);
            newItem.setAuctionActive(true);

            // 4. Thêm vào danh sách quản lý
            AuctionManager.getInstance().addNewItem(newItem);

            // 5. Đóng cửa sổ sau khi thêm thành công
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError("Giá khởi điểm phải là một số hợp lệ!");
        } catch (Exception e) {
            showError("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Đóng cửa sổ khi nhấn Hủy
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}