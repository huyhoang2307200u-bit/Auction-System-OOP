package com.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exception.InvalidBidException;
import com.auction.model.Bidder;
import com.auction.model.Item;
import com.auction.model.ItemFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuctionManagerTest {
    private AuctionManager manager;
    private Item item;
    private Bidder bidderA;
    private Bidder bidderB;

    @BeforeEach
    void setUp() {
        manager = AuctionManager.getInstance();
        manager.resetForTesting();
        item = ItemFactory.createItem("ELECTRONICS", "seller", "Test laptop", 100.0);
        item.setEndTime(LocalDateTime.now().plusMinutes(10));
        manager.addNewItem(item);
        bidderA = new Bidder("alice", "Alice", "", "secret");
        bidderB = new Bidder("bob", "Bob", "", "secret");
        bidderA.deposit(1000.0);
        bidderB.deposit(1000.0);
    }

    @Test
    void placeBidRejectsAmountNotGreaterThanCurrentPrice() {
        assertThrows(InvalidBidException.class, () -> manager.placeBid(item.getId(), 100.0, bidderA));
    }

    @Test
    void placeBidUpdatesPriceWinnerAndHistory() throws Exception {
        manager.placeBid(item.getId(), 150.0, bidderA);

        assertEquals(150.0, item.getCurrentPrice(), 0.001);
        assertEquals("Alice", item.getHighestBidderName());
        assertEquals(1, manager.getBidHistory(item.getId()).size());
    }

    @Test
    void concurrentBiddingKeepsOnlyHighestPriceWithoutRollback() throws Exception {
        int bidderCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(6);
        CountDownLatch ready = new CountDownLatch(bidderCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Exception> errors = new ArrayList<>();

        for (int i = 0; i < bidderCount; i++) {
            final int index = i;
            pool.submit(() -> {
                try {
                    ready.countDown();
                    start.await(2, TimeUnit.SECONDS);
                    Bidder bidder = new Bidder("bidder" + index, "Bidder " + index, "", "pw");
                    bidder.deposit(1000.0);
                    manager.placeBid(item.getId(), 101.0 + index, bidder);
                } catch (Exception e) {
                    synchronized (errors) {
                        errors.add(e);
                    }
                }
            });
        }

        ready.await(2, TimeUnit.SECONDS);
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(120.0, item.getCurrentPrice(), 0.001);
        assertEquals("Bidder 19", item.getHighestBidderName());
        assertTrue(manager.getBidHistory(item.getId()).size() >= 1);
    }

    @Test
    void antiSnipingExtendsAuctionWhenBidArrivesInLastSeconds() throws Exception {
        LocalDateTime originalEnd = LocalDateTime.now().plusSeconds(5);
        item.setEndTime(originalEnd);

        manager.placeBid(item.getId(), 150.0, bidderA);

        assertTrue(item.getEndTime().isAfter(originalEnd));
    }

    @Test
    void autoBidRespondsWhenOpponentPlacesBid() throws Exception {
        manager.registerAutoBid(item.getId(), bidderA, 500.0, 25.0);
        double afterRegister = item.getCurrentPrice();

        manager.placeBid(item.getId(), 200.0, bidderB);

        assertTrue(afterRegister > 100.0);
        assertTrue(item.getCurrentPrice() > 200.0);
        assertEquals("AutoBid-" + bidderA.getId(), item.getHighestBidderName());
    }
}
