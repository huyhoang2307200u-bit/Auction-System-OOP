package com.auction.server;

import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.server.controller.AuctionController;
import com.auction.server.controller.AuthController;

public class RequestProcessor {
    private final AuthController authController;
    private final AuctionController auctionController;

    public RequestProcessor() {
        this.authController = new AuthController();
        this.auctionController = new AuctionController();
    }

    public Response process(Request request) {
        if (request == null || request.getAction() == null) {
            return new Response(false, "Invalid request.", null);
        }

        String action = request.getAction().trim().toUpperCase();

        switch (action) {
            case "PING":
                return new Response(true, "PONG from server", "Server is alive");

            case "MESSAGE":
                return new Response(true, "Message received successfully.", request.getMessage());

            case "LOGIN":
                return authController.login(request);

            case "GET_AUCTIONS":
                return auctionController.getAuctions();

            case "EXIT":
                return new Response(true, "Goodbye! Disconnecting from server...", null);

            default:
                return new Response(false, "Unknown action: " + action, null);
        }
    }
}