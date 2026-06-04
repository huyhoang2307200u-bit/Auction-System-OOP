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
    private static final String SERVER_HOST = "26.201.190.189";
    private static final int SERVER_PORT = 9999;

    private final Gson gson;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;

    public SocketClient() {
        this.gson = new Gson();
    }

    public void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);

        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        writer = new PrintWriter(socket.getOutputStream(), true);

        scanner = new Scanner(System.in);

        log("Đã kết nối tới server: " + SERVER_HOST + ":" + SERVER_PORT);
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();

            System.out.print("Chọn chức năng: ");
            String choice = scanner.nextLine();

            try {
                Request request = buildRequest(choice);

                if (request == null) {
                    System.out.println("Lựa chọn không hợp lệ. Vui lòng thử lại.");
                    continue;
                }

                Response response = sendRequest(request);
                printResponse(response);

                if (request.getType() == RequestType.EXIT) {
                    running = false;
                }

            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Bạn phải nhập số hợp lệ.");
            } catch (IOException e) {
                System.out.println("Lỗi kết nối tới server: " + e.getMessage());
                running = false;
            } catch (Exception e) {
                System.out.println("Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            }
        }

        close();
    }

    private Request buildRequest(String choice) {
        Request request = new Request();

        switch (choice) {
            case "1":
                request.setType(RequestType.PING);
                request.setMessage("Kiểm tra kết nối tới server");
                return request;

            case "2":
                request.setType(RequestType.MESSAGE);

                System.out.print("Nhập tin nhắn: ");
                request.setMessage(scanner.nextLine());

                return request;

            case "3":
                request.setType(RequestType.LOGIN);

                System.out.print("Nhập tên đăng nhập: ");
                request.setUsername(scanner.nextLine());

                System.out.print("Nhập mật khẩu: ");
                request.setPassword(scanner.nextLine());

                return request;

            case "4":
                request.setType(RequestType.REGISTER);

                System.out.print("Nhập tên đăng nhập mới: ");
                request.setUsername(scanner.nextLine());

                System.out.print("Nhập mật khẩu mới: ");
                request.setPassword(scanner.nextLine());

                System.out.print("Nhập vai trò BIDDER / SELLER / ADMIN: ");
                request.setRole(scanner.nextLine());

                return request;

            case "5":
                request.setType(RequestType.GET_AUCTIONS);
                request.setMessage("Lấy danh sách phiên đấu giá");
                return request;

            case "6":
                request.setType(RequestType.PLACE_BID);

                System.out.print("Nhập tên đăng nhập của bidder: ");
                request.setUsername(scanner.nextLine());

                System.out.print("Nhập mã phiên đấu giá: ");
                request.setAuctionId(Integer.parseInt(scanner.nextLine()));

                System.out.print("Nhập số tiền muốn đặt giá: ");
                request.setAmount(Double.parseDouble(scanner.nextLine()));

                return request;

            case "0":
                request.setType(RequestType.EXIT);
                request.setMessage("Client yêu cầu ngắt kết nối");
                return request;

            default:
                return null;
        }
    }

    private Response sendRequest(Request request) throws IOException {
        if (writer == null || reader == null) {
            return new Response(false, "Client chưa kết nối tới server.", null);
        }

        String requestJson = gson.toJson(request);
        writer.println(requestJson);

        log("Đã gửi request: " + requestJson);

        String responseJson = reader.readLine();

        if (responseJson == null) {
            return new Response(false, "Server đã ngắt kết nối.", null);
        }

        log("Đã nhận response: " + responseJson);

        return gson.fromJson(responseJson, Response.class);
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== CLIENT ĐẤU GIÁ ==========");
        System.out.println("1. Kiểm tra kết nối tới server");
        System.out.println("2. Gửi tin nhắn");
        System.out.println("3. Đăng nhập");
        System.out.println("4. Đăng ký");
        System.out.println("5. Xem danh sách phiên đấu giá");
        System.out.println("6. Đặt giá");
        System.out.println("0. Thoát");
        System.out.println("====================================");
    }

    private void printResponse(Response response) {
        System.out.println();
        System.out.println("---------- PHẢN HỒI TỪ SERVER ----------");

        if (response == null) {
            System.out.println("Không nhận được phản hồi từ server.");
            System.out.println("----------------------------------------");
            return;
        }

        System.out.println("Thành công : " + response.isSuccess());
        System.out.println("Thông báo  : " + response.getMessage());

        if (response.getData() != null) {
            System.out.println("Dữ liệu    : " + response.getData());
        }

        System.out.println("----------------------------------------");
    }

    private void close() {
        try {
            if (scanner != null) {
                scanner.close();
            }

            if (reader != null) {
                reader.close();
            }

            if (writer != null) {
                writer.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            log("Client đã ngắt kết nối.");

        } catch (IOException e) {
            System.out.println("Lỗi khi đóng client: " + e.getMessage());
        }
    }

    private static void log(String message) {
        System.out.println("[" + getCurrentTime() + "] [CLIENT] " + message);
    }

    private static String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public static void main(String[] args) {
        SocketClient client = new SocketClient();

        try {
            client.connect();
            client.start();
        } catch (IOException e) {
            System.out.println("Không thể kết nối tới server: " + e.getMessage());
        }
    }
}