package dev.cobblesword.skyblock.controller.model;

import lombok.Data;
import java.util.UUID;

@Data
public class Island {
    private UUID id;
    private UUID ownerId;
    private String serverId;      // Which server it's loaded on (null if not loaded)
    private IslandStatus status;
    private long createdAt;
    private long lastAccessed;

    public Island(UUID ownerId) {
        this.id = ownerId; // Island ID = Owner ID for simplicity
        this.ownerId = ownerId;
        this.status = IslandStatus.UNLOADED;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessed = System.currentTimeMillis();
    }

    public boolean isLoaded() {
        return serverId != null && status == IslandStatus.READY;
    }

    public enum IslandStatus {
        UNLOADED,    // Not on any server
        LOADING,     // Being loaded
        READY,       // Loaded and ready
        UNLOADING    // Being saved/unloaded
    }
}

