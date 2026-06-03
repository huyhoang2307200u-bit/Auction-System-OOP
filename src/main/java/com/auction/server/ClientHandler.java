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
import java.nio.charset.StandardCharsets;
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

        PrintWriter output = null;

        try (
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8)
                )
        ) {
            output = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);
            RealtimeClientRegistry.register(output);
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

                if (request != null && request.getType() == RequestType.PLACE_BID && response.isSuccess()) {
                    RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_BID_UPDATE", response.getData()));
                }

                if (request != null
                        && (request.getType() == RequestType.CREATE_AUCTION
                        || request.getType() == RequestType.APPROVE_AUCTION
                        || request.getType() == RequestType.REJECT_AUCTION
                        || request.getType() == RequestType.FINISH_AUCTION)
                        && response.isSuccess()) {
                    RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_AUCTION_UPDATE", response.getMessage()));
                }

                if (request != null && request.getType() == RequestType.EXIT) {
                    break;
                }
            }

        } catch (IOException e) {
            log(clientInfo, "Lỗi kết nối: " + e.getMessage());
        } finally {
            if (output != null) {
                RealtimeClientRegistry.unregister(output);
            }
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