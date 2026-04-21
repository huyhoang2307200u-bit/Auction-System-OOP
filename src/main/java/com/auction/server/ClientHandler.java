package com.auction.server;

import com.auction.common.Request;
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

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        String clientInfo = clientSocket.getInetAddress().getHostAddress()
                + ":" + clientSocket.getPort();

        log(clientInfo, "Client handler started.");

        try (
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String rawJson;

            while ((rawJson = input.readLine()) != null) {
                log(clientInfo, "Raw JSON received: " + rawJson);

                Response response;

                try {
                    Request request = gson.fromJson(rawJson, Request.class);

                    if (request == null || request.getAction() == null) {
                        response = new Response(false, "Invalid request.", null);
                    } else {
                        response = handleRequest(request, clientInfo);
                    }

                } catch (Exception e) {
                    response = new Response(false, "JSON parse error: " + e.getMessage(), null);
                }

                String responseJson = gson.toJson(response);
                output.println(responseJson);

                log(clientInfo, "Response sent: " + responseJson);

                if (response.getMessage().equals("Goodbye! Disconnecting from server...")) {
                    break;
                }
            }

        } catch (IOException e) {
            log(clientInfo, "Connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                log(clientInfo, "Connection closed.");
            } catch (IOException e) {
                log(clientInfo, "Failed to close socket: " + e.getMessage());
            }
        }
    }

    private Response handleRequest(Request request, String clientInfo) {
        String action = request.getAction().trim().toUpperCase();

        switch (action) {
            case "PING":
                return new Response(
                        true,
                        "PONG from server",
                        "Time: " + getCurrentTime()
                );

            case "MESSAGE":
                log(clientInfo, "Message content: " + request.getMessage());
                return new Response(
                        true,
                        "Server received your message successfully.",
                        request.getMessage()
                );

            case "EXIT":
                return new Response(
                        true,
                        "Goodbye! Disconnecting from server...",
                        null
                );

            default:
                return new Response(
                        false,
                        "Unknown action: " + action,
                        null
                );
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