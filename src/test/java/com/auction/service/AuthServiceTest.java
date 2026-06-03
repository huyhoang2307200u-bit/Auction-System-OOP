package com.auction.service;

import com.auction.model.Admin;
import com.auction.model.Bidder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @Test
    void seededAdminCanLoginButNewAdminCannotRegister() {
        assertNotNull(AuthService.login("admin", "123"));
        assertFalse(AuthService.register(new Admin(1000, "Fake Admin", "fake_admin", "123", "ADMIN")));
    }

    @Test
    void publicRegistrationAllowsBidder() {
        String username = "bidder_test_" + System.nanoTime();
        assertTrue(AuthService.register(new Bidder(123, "Bidder Test", username, "123", "BIDDER")));
        assertNotNull(AuthService.login(username, "123"));
    }
}
