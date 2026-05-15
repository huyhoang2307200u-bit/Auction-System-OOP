package com.auction.service;

import com.auction.model.Item;
import com.auction.model.User;

public class BidService {

    public boolean placeBid(Item item, User user, double amount) throws Exception {
        if (item == null || user == null) {
            return false;
        }
        AuctionManager.getInstance().placeBid(item.getId(), amount, user);
        return true;
    }
}
