package main.java.com.auction.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
            output.println("Connected to Auction Server successfully.");
            output.println("Type your message. Type 'exit' to disconnect.");

            String message;

            while ((message = input.readLine()) != null) {
                log(clientInfo, "Received: " + message);

                if ("exit".equalsIgnoreCase(message.trim())) {
                    output.println("Goodbye! Disconnecting from server...");
                    log(clientInfo, "Client requested disconnection.");
                    break;
                }

                String response = buildResponse(message);
                output.println(response);
                log(clientInfo, "Response sent.");
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

    private String buildResponse(String message) {
        return "Server received your message: \"" + message + "\"";
    }

    private void log(String clientInfo, String message) {
        System.out.println("[" + getCurrentTime() + "] [CLIENT " + clientInfo + "] " + message);
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}