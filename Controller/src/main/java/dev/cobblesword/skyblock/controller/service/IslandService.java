package dev.cobblesword.skyblock.controller.service;

import dev.cobblesword.skyblock.controller.model.Island;
import dev.cobblesword.skyblock.controller.model.Server;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IslandService {

    private final ServerRegistry serverRegistry;
    private final Map<UUID, Island> islands = new ConcurrentHashMap<>();

    public IslandService(ServerRegistry serverRegistry) {
        this.serverRegistry = serverRegistry;
    }

    /**
     * Get or create an island for a player
     */
    public IslandResult getOrCreate(UUID playerId) {
        Island island = islands.computeIfAbsent(playerId, Island::new);

        // If already loaded, return it
        if (island.isLoaded()) {
            island.setLastAccessed(System.currentTimeMillis());
            return new IslandResult(island, false);
        }

        // Need to load it - pick a server
        Optional<Server> serverOpt = serverRegistry.getBestIslandServer();
        if (serverOpt.isEmpty()) {
            return new IslandResult(null, false, "No island servers available");
        }

        Server server = serverOpt.get();
        island.setServerId(server.getId());
        island.setStatus(Island.IslandStatus.LOADING);

        return new IslandResult(island, true);
    }

    /**
     * Get island info
     */
    public Optional<Island> get(UUID islandId) {
        return Optional.ofNullable(islands.get(islandId));
    }

    /**
     * Mark island as ready (called by island server after loading)
     */
    public void markReady(UUID islandId, String serverId) {
        Island island = islands.get(islandId);
        if (island != null) {
            island.setServerId(serverId);
            island.setStatus(Island.IslandStatus.READY);
            island.setLastAccessed(System.currentTimeMillis());
            System.out.println("Island " + islandId + " is now ready on " + serverId);
        }
    }

    /**
     * Mark island as unloaded
     */
    public void markUnloaded(UUID islandId) {
        Island island = islands.get(islandId);
        if (island != null) {
            String oldServer = island.getServerId();
            island.setServerId(null);
            island.setStatus(Island.IslandStatus.UNLOADED);
            System.out.println("Island " + islandId + " unloaded from " + oldServer);
        }
    }

    /**
     * Get where a player should be sent
     */
    public RouteResult route(UUID playerId) {
        Island island = islands.get(playerId);

        if (island == null || !island.isLoaded()) {
            // No island or not loaded - create/load it first
            IslandResult result = getOrCreate(playerId);
            if (result.error != null) {
                return new RouteResult(null, result.error);
            }
            island = result.island;
        }

        return new RouteResult(island.getServerId(), null);
    }

    // Stats methods for dashboard
    public int getTotalIslands() {
        return islands.size();
    }

    public int getLoadedIslands() {
        return (int) islands.values().stream()
                .filter(Island::isLoaded)
                .count();
    }

    public List<Island> getRecentIslands() {
        return islands.values().stream()
                .sorted(Comparator.comparingLong(Island::getLastAccessed).reversed())
                .limit(10)
                .toList();
    }

    // Result types
    public record IslandResult(Island island, boolean needsLoading, String error) {
        public IslandResult(Island island, boolean needsLoading) {
            this(island, needsLoading, null);
        }
    }

    public record RouteResult(String serverId, String error) {}
}

