package com.auction.server;

import com.auction.common.Request;
import com.auction.common.RequestType;
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
        if (request == null || request.getType() == null) {
            return new Response(false, "Yêu cầu không hợp lệ.", null);
        }

        RequestType type = request.getType();

        switch (type) {
            case PING:
                return new Response(true, "Server đã phản hồi PING.", "Server đang hoạt động.");

            case MESSAGE:
                return new Response(true, "Server đã nhận tin nhắn.", request.getMessage());

            case LOGIN:
                return authController.login(request);

            case GET_AUCTIONS:
                return auctionController.getAuctions();

            case PLACE_BID:
                return auctionController.placeBid(request);

            case EXIT:
                return new Response(true, "Tạm biệt! Đang ngắt kết nối với server...", null);

            default:
                return new Response(false, "Loại yêu cầu chưa được hỗ trợ: " + type, null);
        }
    }
}