package com.auction.service;

import com.auction.exception.AuthorizationException;
import com.auction.model.DepositRequest;
import com.auction.model.Role;
import com.auction.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WalletManager {
    private static WalletManager instance;

    private final CopyOnWriteArrayList<DepositRequest> depositRequests = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<WalletObserver> observers = new CopyOnWriteArrayList<>();

    private WalletManager() {
    }

    public static synchronized WalletManager getInstance() {
        if (instance == null) {
            instance = new WalletManager();
        }
        return instance;
    }

    public void addObserver(WalletObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(WalletObserver observer) {
        observers.remove(observer);
    }

    public DepositRequest requestDeposit(User user, double amount) {
        if (user == null) {
            throw new IllegalArgumentException("Bạn cần đăng nhập trước khi yêu cầu nạp tiền.");
        }
        if (user.getRole() != Role.BIDDER) {
            throw new AuthorizationException("Chỉ Bidder cần gửi yêu cầu nạp tiền vào ví.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0.");
        }
        DepositRequest request = new DepositRequest(user.getUsername(), user.getName(), amount);
        depositRequests.add(request);
        notifyObservers(user.getUsername());
        return request;
    }

    public List<DepositRequest> getPendingRequests() {
        List<DepositRequest> result = new ArrayList<>();
        for (DepositRequest request : depositRequests) {
            if (request.isPending()) {
                result.add(request);
            }
        }
        return result;
    }

    public List<DepositRequest> getRequestsByUsername(String username) {
        List<DepositRequest> result = new ArrayList<>();
        if (username == null || username.isBlank()) {
            return result;
        }
        for (DepositRequest request : depositRequests) {
            if (username.trim().equalsIgnoreCase(request.getUsername())) {
                result.add(request);
            }
        }
        return result;
    }

    public DepositRequest approveDeposit(String requestId, User admin) {
        ensureAdmin(admin);
        DepositRequest request = findRequestOrThrow(requestId);
        User targetUser = AuthService.findUserByUsername(request.getUsername());
        if (targetUser == null) {
            throw new IllegalStateException("Không tìm thấy người dùng yêu cầu nạp tiền.");
        }
        synchronized (request) {
            request.approve(admin.getUsername());
            targetUser.deposit(request.getAmount());
        }
        notifyObservers(request.getUsername());
        return request;
    }

    public DepositRequest rejectDeposit(String requestId, User admin, String reason) {
        ensureAdmin(admin);
        DepositRequest request = findRequestOrThrow(requestId);
        synchronized (request) {
            request.reject(admin.getUsername(), reason);
        }
        notifyObservers(request.getUsername());
        return request;
    }

    public void resetForTesting() {
        depositRequests.clear();
    }

    private void notifyObservers(String username) {
        for (WalletObserver observer : observers) {
            observer.onWalletChanged(username);
        }
    }

    private void ensureAdmin(User user) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Chỉ Admin mới được kiểm duyệt yêu cầu nạp tiền.");
        }
    }

    private DepositRequest findRequestOrThrow(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu nạp tiền không được để trống.");
        }
        for (DepositRequest request : depositRequests) {
            if (request.getId().equals(requestId)) {
                return request;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy yêu cầu nạp tiền.");
    }
}
