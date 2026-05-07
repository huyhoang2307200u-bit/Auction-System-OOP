package com.auction.client;

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
import java.util.Scanner;

public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) {
        Gson gson = new Gson();

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

            while (true) {
                System.out.println("\n===== MENU =====");
                System.out.println("1. Send PING");
                System.out.println("2. Send MESSAGE");
                System.out.println("3. LOGIN");
                System.out.println("4. GET_AUCTIONS");
                System.out.println("5. PLACE_BID");
                System.out.println("6. EXIT");
                System.out.print("Choose: ");

                String choice = scanner.nextLine();
                Request request = new Request();

                switch (choice) {
                    case "1":
                        request.setAction("PING");
                        request.setMessage("Hello Server");
                        break;

                    case "2":
                        System.out.print("Enter your message: ");
                        request.setAction("MESSAGE");
                        request.setMessage(scanner.nextLine());
                        break;

                    case "3":
                        System.out.print("Enter username: ");
                        request.setUsername(scanner.nextLine());

                        System.out.print("Enter password: ");
                        request.setPassword(scanner.nextLine());

                        request.setAction("LOGIN");
                        break;

                    case "4":
                        request.setAction("GET_AUCTIONS");
                        request.setMessage("fetch auctions");
                        break;

                    case "5":
                        System.out.print("Enter username: ");
                        request.setUsername(scanner.nextLine());

                        System.out.print("Enter auction id: ");
                        request.setAuctionId(Integer.parseInt(scanner.nextLine()));

                        System.out.print("Enter bid amount: ");
                        request.setAmount(Double.parseDouble(scanner.nextLine()));

                        request.setAction("PLACE_BID");
                        break;

                    case "6":
                        request.setAction("EXIT");
                        request.setMessage("Disconnect");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                        continue;
                }

                String requestJson = gson.toJson(request);
                output.println(requestJson);

                log("Sent JSON: " + requestJson);

                String responseJson = input.readLine();
                Response response = gson.fromJson(responseJson, Response.class);

                System.out.println("\n----- SERVER RESPONSE -----");
                System.out.println("Success : " + response.isSuccess());
                System.out.println("Message : " + response.getMessage());
                System.out.println("Data    : " + response.getData());
                System.out.println("---------------------------");

                if ("6".equals(choice)) {
                    log("Client stopped.");
                    break;
                }
            }

        } catch (IOException e) {
            log("Client error: " + e.getMessage());
        } catch (Exception e) {
            log("Input error: " + e.getMessage());
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