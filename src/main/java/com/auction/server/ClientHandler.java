package com.auction.server;

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

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Gson gson;
    private final RequestProcessor requestProcessor;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gson = new Gson();
        this.requestProcessor = new RequestProcessor();
    }

    @Override
    public void run() {
        String clientInfo = clientSocket.getInetAddress().getHostAddress()
                + ":" + clientSocket.getPort();

        log(clientInfo, "Bắt đầu xử lý client.");

        try (
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String rawJson;

            while ((rawJson = input.readLine()) != null) {
                log(clientInfo, "JSON nhận được: " + rawJson);

                Request request = null;
                Response response;

                try {
                    request = gson.fromJson(rawJson, Request.class);
                    response = requestProcessor.process(request);
                } catch (Exception e) {
                    response = new Response(false, "Lỗi xử lý yêu cầu: " + e.getMessage(), null);
                }

                String responseJson = gson.toJson(response);
                output.println(responseJson);

                log(clientInfo, "Phản hồi đã gửi: " + responseJson);

                if (request != null && request.getType() == RequestType.EXIT) {
                    break;
                }
            }

        } catch (IOException e) {
            log(clientInfo, "Lỗi kết nối: " + e.getMessage());
        } finally {
            closeSocket(clientInfo);
        }
    }

    private void closeSocket(String clientInfo) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }

            log(clientInfo, "Đã đóng kết nối.");
        } catch (IOException e) {
            log(clientInfo, "Không thể đóng socket: " + e.getMessage());
        }
    }

    private void log(String clientInfo, String message) {
        System.out.println("[" + getCurrentTime() + "] [CLIENT " + clientInfo + "] " + message);
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}