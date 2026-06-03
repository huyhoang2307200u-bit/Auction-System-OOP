package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.server.security.AuthenticatedUser;
import com.auction.server.service.NotificationService;

public class NotificationController {
    private final NotificationService notificationService = new NotificationService();

    public Response getUnreadNotifications(AuthenticatedUser user) {
        return notificationService.getUnreadNotifications(user);
    }

    public Response markNotificationRead(AuthenticatedUser user, Request request) {
        return notificationService.markNotificationRead(user, request.getNotificationId());
    }
}
