package com.auction.service;

import com.auction.model.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private static NotificationManager instance;

    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<NotificationObserver> observers = new CopyOnWriteArrayList<>();

    private NotificationManager() {
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addObserver(NotificationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public Notification addNotification(String recipientUsername, String title, String message) {
        Notification notification = new Notification(recipientUsername, title, message);
        notifications.add(notification);
        for (NotificationObserver observer : observers) {
            observer.onNotification(notification);
        }
        return notification;
    }

    public List<Notification> getUnreadNotifications(String username) {
        List<Notification> result = new ArrayList<>();
        if (username == null || username.isBlank()) {
            return result;
        }
        for (Notification notification : notifications) {
            if (!notification.isRead()
                    && username.trim().equalsIgnoreCase(notification.getRecipientUsername())) {
                result.add(notification);
            }
        }
        return result;
    }

    public void markRead(Notification notification) {
        if (notification != null && !notification.isRead()) {
            notification.markRead();
        }
    }

    public void markAllRead(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        boolean changed = false;
        for (Notification notification : notifications) {
            if (!notification.isRead()
                    && username.trim().equalsIgnoreCase(notification.getRecipientUsername())) {
                notification.markRead();
                changed = true;
            }
        }
        if (changed) {
        }
    }

    public List<Notification> snapshotNotifications() {
        return new ArrayList<>(notifications);
    }

    public void restoreNotifications(List<Notification> savedNotifications) {
        notifications.clear();
        if (savedNotifications != null) {
            notifications.addAll(savedNotifications);
        }
    }

    public void resetForTesting() {
        notifications.clear();
    }
}
