package com.auction.client;

import com.auction.common.AuctionDTO;
import com.auction.common.Request;
import com.auction.common.RequestType;
import com.auction.common.Response;
import com.auction.dto.DepositRequestDto;
import com.auction.dto.LoginResultDto;
import com.auction.dto.NotificationDto;
import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.Role;
import com.auction.model.Seller;
import com.auction.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ServerApiClient implements Closeable {
    private static final String DEFAULT_HOST = System.getenv().getOrDefault("AUCTION_SERVER_HOST", "localhost");
    private static final int DEFAULT_PORT = Integer.parseInt(System.getenv().getOrDefault("AUCTION_SERVER_PORT", "9999"));
    private static ServerApiClient instance;

    private final String host;
    private final int port;
    private final Gson gson = new Gson();
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();
    private final CopyOnWriteArrayList<Consumer<Response>> realtimeListeners = new CopyOnWriteArrayList<>();

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean running;
    private User currentUser;

    private ServerApiClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static synchronized ServerApiClient getInstance() {
        if (instance == null) {
            instance = new ServerApiClient(DEFAULT_HOST, DEFAULT_PORT);
        }
        return instance;
    }

    public synchronized void connectIfNeeded() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        running = true;
        Thread readerThread = new Thread(this::readLoop, "auction-server-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                Response response = gson.fromJson(line, Response.class);
                if (isRealtime(response)) {
                    for (Consumer<Response> listener : realtimeListeners) {
                        listener.accept(response);
                    }
                } else {
                    responseQueue.offer(response);
                }
            }
        } catch (Exception e) {
            if (running) {
                responseQueue.offer(new Response(false, "Mất kết nối server: " + e.getMessage(), null));
            }
        } finally {
            running = false;
        }
    }

    private boolean isRealtime(Response response) {
        return response != null && response.getMessage() != null && response.getMessage().startsWith("REALTIME_");
    }

    public void addRealtimeListener(Consumer<Response> listener) {
        if (listener != null) {
            realtimeListeners.addIfAbsent(listener);
        }
    }

    public void removeRealtimeListener(Consumer<Response> listener) {
        realtimeListeners.remove(listener);
    }

    public synchronized Response send(Request request) throws IOException {
        connectIfNeeded();
        responseQueue.clear();
        writer.println(gson.toJson(request));
        try {
            Response response = responseQueue.poll(10, TimeUnit.SECONDS);
            if (response == null) {
                return new Response(false, "Server không phản hồi trong 10 giây.", null);
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Response(false, "Luồng chờ phản hồi bị ngắt.", null);
        }
    }

    public User login(String username, String password) throws IOException {
        Request request = new Request();
        request.setType(RequestType.LOGIN);
        request.setUsername(username);
        request.setPassword(password);
        Response response = send(request);
        ensureSuccess(response);

        LoginResultDto dto = convertData(response, LoginResultDto.class);
        User user = createUser(dto);
        user.setBalance(dto.getBalance());
        this.currentUser = user;
        return user;
    }

    public void register(String username, String password, String role) throws IOException {
        Request request = new Request();
        request.setType(RequestType.REGISTER);
        request.setUsername(username);
        request.setPassword(password);
        request.setDisplayName(username);
        request.setRole(role);
        ensureSuccess(send(request));
    }

    public List<AuctionDTO> getAuctions() throws IOException {
        Request request = new Request();
        request.setType(RequestType.GET_AUCTIONS);
        Response response = send(request);
        if (!response.isSuccess()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<AuctionDTO>>() {}.getType();
        return gson.fromJson(gson.toJsonTree(response.getData()), type);
    }

    public void createAuction(String itemName, String description, String category, double startPrice, int durationMinutes) throws IOException {
        Request request = new Request();
        request.setType(RequestType.CREATE_AUCTION);
        request.setItemName(itemName);
        request.setDescription(description);
        request.setCategory(category);
        request.setAmount(startPrice);
        request.setDurationMinutes(durationMinutes);
        ensureSuccess(send(request));
    }

    public void placeBid(int auctionId, double amount) throws IOException {
        Request request = new Request();
        request.setType(RequestType.PLACE_BID);
        request.setAuctionId(auctionId);
        request.setAmount(amount);
        ensureSuccess(send(request));
    }

    public void registerAutoBid(int auctionId, double maxBid, double increment) throws IOException {
        Request request = new Request();
        request.setType(RequestType.REGISTER_AUTO_BID);
        request.setAuctionId(auctionId);
        request.setMaxBid(maxBid);
        request.setIncrement(increment);
        ensureSuccess(send(request));
    }

    public void requestDeposit(double amount) throws IOException {
        Request request = new Request();
        request.setType(RequestType.DEPOSIT_MONEY);
        request.setAmount(amount);
        ensureSuccess(send(request));
    }

    public double getBalance() throws IOException {
        Request request = new Request();
        request.setType(RequestType.GET_BALANCE);
        Response response = send(request);
        ensureSuccess(response);
        if (response.getData() instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(response.getData()));
    }

    public List<DepositRequestDto> getPendingDepositRequests() throws IOException {
        Request request = new Request();
        request.setType(RequestType.GET_PENDING_DEPOSIT_REQUESTS);
        Response response = send(request);
        if (!response.isSuccess()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<DepositRequestDto>>() {}.getType();
        return gson.fromJson(gson.toJsonTree(response.getData()), type);
    }

    public void approveDeposit(int requestId) throws IOException {
        Request request = new Request();
        request.setType(RequestType.APPROVE_DEPOSIT);
        request.setDepositRequestId(requestId);
        ensureSuccess(send(request));
    }

    public void rejectDeposit(int requestId, String reason) throws IOException {
        Request request = new Request();
        request.setType(RequestType.REJECT_DEPOSIT);
        request.setDepositRequestId(requestId);
        request.setRejectionReason(reason);
        ensureSuccess(send(request));
    }

    public void approveAuction(int auctionId) throws IOException {
        Request request = new Request();
        request.setType(RequestType.APPROVE_AUCTION);
        request.setAuctionId(auctionId);
        ensureSuccess(send(request));
    }

    public void finishAuction(int auctionId) throws IOException {
        Request request = new Request();
        request.setType(RequestType.FINISH_AUCTION);
        request.setAuctionId(auctionId);
        ensureSuccess(send(request));
    }

    public List<NotificationDto> getUnreadNotifications() throws IOException {
        Request request = new Request();
        request.setType(RequestType.GET_UNREAD_NOTIFICATIONS);
        Response response = send(request);
        if (!response.isSuccess()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<NotificationDto>>() {}.getType();
        return gson.fromJson(gson.toJsonTree(response.getData()), type);
    }

    public void markNotificationRead(int notificationId) throws IOException {
        Request request = new Request();
        request.setType(RequestType.MARK_NOTIFICATION_READ);
        request.setNotificationId(notificationId);
        ensureSuccess(send(request));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private <T> T convertData(Response response, Class<T> clazz) {
        return gson.fromJson(gson.toJsonTree(response.getData()), clazz);
    }

    private User createUser(LoginResultDto dto) {
        Role role = dto.getRole();
        String displayName = dto.getDisplayName() == null || dto.getDisplayName().isBlank()
                ? dto.getUsername()
                : dto.getDisplayName();
        if (role == Role.ADMIN) {
            return new Admin(Integer.parseInt(dto.getUserId()), displayName, dto.getUsername(), "", role.name());
        }
        if (role == Role.SELLER) {
            return new Seller(Integer.parseInt(dto.getUserId()), displayName, dto.getUsername(), "", role.name());
        }
        return new Bidder(Integer.parseInt(dto.getUserId()), displayName, dto.getUsername(), "", role.name());
    }

    private void ensureSuccess(Response response) {
        if (response == null) {
            throw new IllegalStateException("Không nhận được phản hồi từ server.");
        }
        if (!response.isSuccess()) {
            throw new IllegalStateException(response.getMessage());
        }
    }

    @Override
    public synchronized void close() throws IOException {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
