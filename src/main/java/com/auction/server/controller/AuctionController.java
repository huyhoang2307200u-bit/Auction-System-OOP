package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;

import java.util.ArrayList;
import java.util.List;

public class AuctionController {

    public Response getAuctions() {
        List<String> auctions = new ArrayList<>();

        auctions.add("1 - Điện thoại iPhone 13 - Giá hiện tại: 5,000,000");
        auctions.add("2 - Laptop Dell XPS - Giá hiện tại: 12,000,000");
        auctions.add("3 - Đồng hồ Casio - Giá hiện tại: 800,000");

        return new Response(true, "Lấy danh sách phiên đấu giá thành công.", auctions);
    }

    public Response placeBid(Request request) {
        if (request.getAuctionId() == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        if (request.getAmount() == null) {
            return new Response(false, "Thiếu số tiền đặt giá.", null);
        }

        if (request.getAmount() <= 0) {
            return new Response(false, "Số tiền đặt giá phải lớn hơn 0.", null);
        }

        String result = "Đã đặt giá "
                + request.getAmount()
                + " cho phiên đấu giá có mã "
                + request.getAuctionId();

        return new Response(true, "Đặt giá thành công.", result);
    }
}