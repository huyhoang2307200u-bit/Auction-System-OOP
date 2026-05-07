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
import java.util.Scanner;

public class SocketClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private final Gson gson;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public SocketClient() {
        this.gson = new Gson();
    }

    public void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);

        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        writer = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Đã kết nối tới server: " + SERVER_HOST + ":" + SERVER_PORT);
    }

    public Response sendRequest(Request request) throws IOException {
        String jsonRequest = gson.toJson(request);
        writer.println(jsonRequest);

        String jsonResponse = reader.readLine();

        if (jsonResponse == null) {
            return new Response(false, "Server đã ngắt kết nối.", null);
        }

        return gson.fromJson(jsonResponse, Response.class);
    }

    public void startConsole() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();

            System.out.print("Chọn chức năng: ");
            String choice = scanner.nextLine();

            Request request = new Request();

            try {
                switch (choice) {
                    case "1":
                        request.setType(RequestType.PING);
                        break;

                    case "2":
                        System.out.print("Nhập tin nhắn của bạn: ");
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
                        request.setType(RequestType.GET_AUCTIONS);
                        break;

                    case "5":
                        System.out.print("Nhập mã phiên đấu giá: ");
                        request.setAuctionId(Integer.parseInt(scanner.nextLine()));

                        System.out.print("Nhập số tiền muốn đặt giá: ");
                        request.setAmount(Double.parseDouble(scanner.nextLine()));

                        request.setType(RequestType.PLACE_BID);
                        break;

                    case "0":
                        request.setType(RequestType.EXIT);
                        break;

                    default:
                        System.out.println("Lựa chọn không hợp lệ. Vui lòng thử lại.");
                        continue;
                }

                Response response = sendRequest(request);
                printResponse(response);

                if (request.getType() == RequestType.EXIT) {
                    break;
                }

            } catch (NumberFormatException e) {
                System.out.println("Định dạng số không hợp lệ. Vui lòng nhập một số hợp lệ.");
            } catch (IOException e) {
                System.out.println("Lỗi kết nối: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.out.println("Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            }
        }

        close();
        scanner.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== CLIENT ĐẤU GIÁ ==========");
        System.out.println("1. Kiểm tra kết nối tới server");
        System.out.println("2. Gửi tin nhắn");
        System.out.println("3. Đăng nhập");
        System.out.println("4. Xem danh sách phiên đấu giá");
        System.out.println("5. Đặt giá");
        System.out.println("0. Thoát");
        System.out.println("====================================");
    }

    private void printResponse(Response response) {
        if (response == null) {
            System.out.println("Không nhận được phản hồi từ server.");
            return;
        }

        System.out.println();
        System.out.println("---------- PHẢN HỒI TỪ SERVER ----------");
        System.out.println("Thành công: " + response.isSuccess());
        System.out.println("Thông báo: " + response.getMessage());

        if (response.getData() != null) {
            System.out.println("Dữ liệu: " + response.getData());
        }

        System.out.println("----------------------------------------");
    }

    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }

            if (writer != null) {
                writer.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Client đã ngắt kết nối.");

        } catch (IOException e) {
            System.out.println("Lỗi khi đóng client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SocketClient client = new SocketClient();

        try {
            client.connect();
            client.startConsole();
        } catch (IOException e) {
            System.out.println("Không thể kết nối tới server: " + e.getMessage());
        }
    }
}