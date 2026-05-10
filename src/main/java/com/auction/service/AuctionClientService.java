package com.auction.service;

import java.io.*;
import java.net.*;

public class AuctionClientService {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;

    public String sendRequest(String message) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 1. Gửi tin nhắn sang Server
            out.println(message);

            // 2. Đợi và nhận phản hồi từ Server
            return in.readLine();

        } catch (IOException e) {
            return "LỖI: Không thể kết nối tới Server! " + e.getMessage();
        }
    }
}