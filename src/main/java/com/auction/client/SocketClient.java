package main.java.com.auction.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("         AUCTION CLIENT STARTING        ");
        System.out.println("========================================");

        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)
        ) {
            log("Connected to server " + HOST + ":" + PORT);

            String welcome1 = input.readLine();
            String welcome2 = input.readLine();

            System.out.println(welcome1);
            System.out.println(welcome2);

            while (true) {
                System.out.print("\nEnter message: ");
                String message = scanner.nextLine();

                output.println(message);
                log("Sent: " + message);

                String response = input.readLine();
                System.out.println("Server response: " + response);

                if ("exit".equalsIgnoreCase(message.trim())) {
                    log("Client stopped.");
                    break;
                }
            }

        } catch (IOException e) {
            log("Client error: " + e.getMessage());
        }
    }

    private static void log(String message) {
        System.out.println("[" + getCurrentTime() + "] [CLIENT] " + message);
    }

    private static String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
