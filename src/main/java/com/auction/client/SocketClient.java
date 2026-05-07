package com.auction.client;

import com.auction.common.Request;
import com.auction.common.RequestType;
import com.auction.common.Response;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) {
        Gson gson = new Gson();

        System.out.println("========================================");
        System.out.println("          CLIENT ĐẤU GIÁ ĐANG CHẠY      ");
        System.out.println("========================================");

        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)
        ) {
            log("Đã kết nối tới server " + HOST + ":" + PORT);

            while (true) {
                printMenu();

                System.out.print("Chọn chức năng: ");
                String choice = scanner.nextLine();

                Request request = new Request();

                try {
                    switch (choice) {
                        case "1":
                            request.setType(RequestType.PING);
                            request.setMessage("Xin chào server");
                            break;

                        case "2":
                            System.out.print("Nhập tin nhắn: ");
                            request.setType(RequestType.MESSAGE);
                            request.setMessage(scanner.nextLine());
                            break;

                        case "3":
                            System.out.print("Nhập tên đăng nhập: ");
                            request.setUsername(scanner.nextLine());

                            System.out.print("Nhập mật khẩu: ");
                            request.setPassword(scanner.nextLine());

                            request.setType(RequestType.LOGIN);
                            break;

                        case "4":
                            System.out.print("Nhập tên đăng nhập mới: ");
                            request.setUsername(scanner.nextLine());

                            System.out.print("Nhập mật khẩu mới: ");
                            request.setPassword(scanner.nextLine());

                            System.out.print("Nhập vai trò (BIDDER/SELLER/ADMIN): ");
                            request.setRole(scanner.nextLine());

                            request.setType(RequestType.REGISTER);
                            break;

                        case "5":
                            request.setType(RequestType.GET_AUCTIONS);
                            request.setMessage("Lấy danh sách phiên đấu giá");
                            break;

                        case "6":
                            System.out.print("Nhập tên đăng nhập: ");
                            request.setUsername(scanner.nextLine());

                            System.out.print("Nhập mã phiên đấu giá: ");
                            request.setAuctionId(Integer.parseInt(scanner.nextLine()));

                            System.out.print("Nhập số tiền muốn đặt giá: ");
                            request.setAmount(Double.parseDouble(scanner.nextLine()));

                            request.setType(RequestType.PLACE_BID);
                            break;

                        case "0":
                            request.setType(RequestType.EXIT);
                            request.setMessage("Ngắt kết nối");
                            break;

                        default:
                            System.out.println("Lựa chọn không hợp lệ. Vui lòng thử lại.");
                            continue;
                    }

                    String requestJson = gson.toJson(request);
                    output.println(requestJson);

                    log("Đã gửi JSON: " + requestJson);

                    String responseJson = input.readLine();

                    if (responseJson == null) {
                        System.out.println("Server đã ngắt kết nối.");
                        break;
                    }

                    Response response = gson.fromJson(responseJson, Response.class);
                    printResponse(response);

                    if (request.getType() == RequestType.EXIT) {
                        log("Client đã dừng.");
                        break;
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Bạn phải nhập số hợp lệ.");
                } catch (Exception e) {
                    System.out.println("Đã xảy ra lỗi: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            log("Lỗi client: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("========== MENU CLIENT ĐẤU GIÁ ==========");
        System.out.println("1. Kiểm tra kết nối tới server");
        System.out.println("2. Gửi tin nhắn");
        System.out.println("3. Đăng nhập");
        System.out.println("4. Đăng ký");
        System.out.println("5. Xem danh sách phiên đấu giá");
        System.out.println("6. Đặt giá");
        System.out.println("0. Thoát");
        System.out.println("=========================================");
    }

    private static void printResponse(Response response) {
        if (response == null) {
            System.out.println("Không nhận được phản hồi từ server.");
            return;
        }

        System.out.println();
        System.out.println("---------- PHẢN HỒI TỪ SERVER ----------");
        System.out.println("Thành công : " + response.isSuccess());
        System.out.println("Thông báo  : " + response.getMessage());

        if (response.getData() != null) {
            System.out.println("Dữ liệu    : " + response.getData());
        }

        System.out.println("----------------------------------------");
    }

    private static void log(String message) {
        System.out.println("[" + getCurrentTime() + "] [CLIENT] " + message);
    }

    private static String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}