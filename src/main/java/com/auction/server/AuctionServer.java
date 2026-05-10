package com.auction.server;

import com.auction.dao.ItemDAO;
import java.io.*;
import java.net.*;

public class AuctionServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        ItemDAO itemDAO = new ItemDAO(); // Server là người giữ DAO

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("SERVER: Đang chạy tại cổng " + PORT + "...");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    String request = in.readLine(); // Nhận tin dạng: "BID|id_sp|gia_tien"
                    System.out.println("SERVER nhận yêu cầu: " + request);

                    if (request != null && request.startsWith("BID")) {
                        String[] parts = request.split("\\|");
                        String itemId = parts[1];
                        double newPrice = Double.parseDouble(parts[2]);

                        // --- KHU VỰC KIỂM DUYỆT (Ý THẦY BẠN Ở ĐÂY) ---
                        if (newPrice > 100000000) {
                            out.println("REJECT|Giá quá lớn (trên 100tr). Cần xác minh tài chính!");
                        } else if (newPrice <= 0) {
                            out.println("REJECT|Giá đặt không hợp lệ!");
                        } else {
                            // Nếu vượt qua kiểm duyệt mới được lưu vào DB
                            boolean success = itemDAO.updatePrice(itemId, newPrice);
                            if (success) {
                                out.println("SUCCESS|Đã phê duyệt và cập nhật giá: " + newPrice);
                            } else {
                                out.println("ERROR|Lỗi cập nhật Database!");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}