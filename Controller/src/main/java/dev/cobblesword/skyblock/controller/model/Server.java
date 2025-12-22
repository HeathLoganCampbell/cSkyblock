package dev.cobblesword.skyblock.controller.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Server {
    private String id;
    private String type; // "spawn" or "islands"
    private String host;
    private int port;
    private int playerCount;
    private int maxPlayers;
    private long lastHeartbeat;
    
    // Performance metrics
    private double tps;           // Ticks per second (20 = perfect)
    private long memoryUsed;      // MB
    private long memoryMax;       // MB
    private double cpuUsage;      // Percentage (0-100)
    
    // Islands with metrics
    private List<IslandMetrics> islands = new ArrayList<>();

    public Server(String id, String type, String host, int port) {
        this.id = id;
        this.type = type;
        this.host = host;
        this.port = port;
        this.playerCount = 0;
        this.maxPlayers = 100;
        this.lastHeartbeat = System.currentTimeMillis();
        this.tps = 20.0;
        this.memoryUsed = 0;
        this.memoryMax = 0;
        this.cpuUsage = 0;
    }

    public boolean isOnline() {
        return System.currentTimeMillis() - lastHeartbeat < 30_000;
    }

    public boolean isIslandServer() {
        return "islands".equalsIgnoreCase(type);
    }

    public boolean isSpawnServer() {
        return "spawn".equalsIgnoreCase(type);
    }
    
    public int getMemoryPercent() {
        if (memoryMax == 0) return 0;
        return (int) ((memoryUsed * 100) / memoryMax);
    }
    
    @Data
    public static class IslandMetrics {
        private String id;
        private int chunks;
        private int entities;
        private int tileEntities;
        private int players;
        
        public IslandMetrics() {}
        
        public IslandMetrics(String id, int chunks, int entities, int tileEntities, int players) {
            this.id = id;
            this.chunks = chunks;
            this.entities = entities;
            this.tileEntities = tileEntities;
            this.players = players;
        }
        
        // Calculate a "load score" - higher = more resource intensive
        public int getLoadScore() {
            return chunks + (entities / 10) + (tileEntities * 2);
        }
    }
}
