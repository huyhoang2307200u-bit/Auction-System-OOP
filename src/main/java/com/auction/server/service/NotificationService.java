package com.auction.server.service;

import com.auction.common.Response;
import com.auction.dto.NotificationDto;
import com.auction.server.dao.NotificationDAO;
import com.auction.server.security.AuthenticatedUser;

import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public Response getUnreadNotifications(AuthenticatedUser user) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để xem thông báo.", null);
        }
        List<NotificationDto> notifications = notificationDAO.getUnreadNotifications(user.getUsername());
        return new Response(true, "Lấy thông báo chưa đọc thành công.", notifications);
    }

    public Response markNotificationRead(AuthenticatedUser user, Integer notificationId) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để đánh dấu thông báo.", null);
        }
        if (notificationId == null) {
            return new Response(false, "Thiếu mã thông báo.", null);
        }
        boolean updated = notificationDAO.markRead(user.getUsername(), notificationId);
        return new Response(updated, updated ? "Đã đánh dấu thông báo đã đọc." : "Không tìm thấy thông báo.", null);
    }
}
