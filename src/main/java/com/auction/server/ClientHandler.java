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
                    response = requestProcessor.process(request);

                } catch (Exception e) {
                    response = new Response(false, "JSON parse error: " + e.getMessage(), null);
                }

                String responseJson = gson.toJson(response);
                output.println(responseJson);

                log(clientInfo, "Response sent: " + responseJson);

                if ("Goodbye! Disconnecting from server...".equals(response.getMessage())) {
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

    private void log(String clientInfo, String message) {
        System.out.println("[" + getCurrentTime() + "] [CLIENT " + clientInfo + "] " + message);
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}