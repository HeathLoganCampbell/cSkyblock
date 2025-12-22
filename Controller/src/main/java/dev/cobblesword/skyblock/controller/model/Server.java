package dev.cobblesword.skyblock.controller.model;

import lombok.Data;

@Data
public class Server {
    private String id;
    private String type; // "spawn" or "islands"
    private String host;
    private int port;
    private int playerCount;
    private int maxPlayers;
    private long lastHeartbeat;

    public Server(String id, String type, String host, int port) {
        this.id = id;
        this.type = type;
        this.host = host;
        this.port = port;
        this.playerCount = 0;
        this.maxPlayers = 100;
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public boolean isOnline() {
        // Consider offline if no heartbeat for 30 seconds
        return System.currentTimeMillis() - lastHeartbeat < 30_000;
    }

    public boolean isIslandServer() {
        return "islands".equalsIgnoreCase(type);
    }

    public boolean isSpawnServer() {
        return "spawn".equalsIgnoreCase(type);
    }
}

