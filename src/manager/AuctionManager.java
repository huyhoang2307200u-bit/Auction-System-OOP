package manager;

public class AuctionManager {
    private static AuctionManager instance;

    private AuctionManager() {}

    public static AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void info() {
        System.out.println("Hệ thống đấu giá đang chạy!");
    }
}