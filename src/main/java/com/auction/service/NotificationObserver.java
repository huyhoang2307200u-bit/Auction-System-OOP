package com.auction.service;

import com.auction.model.Notification;

public interface NotificationObserver {
    void onNotification(Notification notification);
}
