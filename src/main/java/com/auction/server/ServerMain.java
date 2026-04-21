package com.auction.server;

public class ServerMain {
    public static void main(String[] args) {
        int port = 9999;
        AuctionServer server = new AuctionServer(port);
        server.start();
    }
}