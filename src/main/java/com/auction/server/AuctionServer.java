package com.auction.server;

import com.auction.server.service.AuctionService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionServer {
    private final int port;
    private boolean running;
    private ScheduledExecutorService scheduler;

    public AuctionServer(int port) {
        this.port = port;
        this.running = true;
    }

    public void start() {
        System.out.println("        AUCTION SERVER STARTING         ");
        startAuctionClosingScheduler();

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
                clientThread.setDaemon(true);
                clientThread.start();
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        } finally {
            stopScheduler();
        }

        log("Server stopped.");
    }

    private void startAuctionClosingScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "auction-expiration-checker");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int finishedCount = new AuctionService().finishExpiredAuctionsAndNotify();
                if (finishedCount > 0) {
                    log("Đã tự động kết thúc " + finishedCount + " phiên hết hạn.");
                }
            } catch (Exception e) {
                log("Lỗi scheduler kết thúc phiên: " + e.getMessage());
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    private void stopScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void log(String message) {
        System.out.println("[" + getCurrentTime() + "] [SERVER] " + message);
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
