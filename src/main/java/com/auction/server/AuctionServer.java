package com.auction.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuctionServer {
    private final int port;
    private boolean running;

    public AuctionServer(int port) {
        this.port = port;
        this.running = true;
    }

    public void start() {
        System.out.println("        AUCTION SERVER STARTING         ");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server is running on port " + port);
            log("Waiting for clients to connect...");

            while (running) {
                Socket clientSocket = serverSocket.accept();

                String clientInfo = clientSocket.getInetAddress().getHostAddress()
                        + ":" + clientSocket.getPort();

                log("New client connected -> " + clientInfo);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }

        log("Server stopped.");
    }

    private void log(String message) {
        System.out.println("[" + getCurrentTime() + "] [SERVER] " + message);
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}