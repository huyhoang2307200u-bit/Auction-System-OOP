package com.auction.server;

import com.auction.common.Response;
import com.google.gson.Gson;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class RealtimeClientRegistry {
    private static final Set<PrintWriter> CLIENTS = new CopyOnWriteArraySet<>();
    private static final Gson GSON = new Gson();

    private RealtimeClientRegistry() {
    }

    public static void register(PrintWriter writer) {
        if (writer != null) {
            CLIENTS.add(writer);
        }
    }

    public static void unregister(PrintWriter writer) {
        CLIENTS.remove(writer);
    }

    public static void broadcast(Response event) {
        String json = GSON.toJson(event);
        for (PrintWriter client : CLIENTS) {
            client.println(json);
        }
    }
}
