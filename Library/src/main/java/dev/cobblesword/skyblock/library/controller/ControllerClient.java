package dev.cobblesword.skyblock.library.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client for the Skyblock Controller service
 */
public class ControllerClient {

    private static final Gson GSON = new GsonBuilder().create();
    
    private final String baseUrl;
    private final HttpClient httpClient;

    public ControllerClient(String controllerUrl) {
        this.baseUrl = controllerUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // Server Registration
    // ═══════════════════════════════════════════════════════════════

    public CompletableFuture<Void> registerServer(String serverId, String type, String host, int port) {
        var body = new ServerRegisterRequest(serverId, type, host, port);
        return post("/api/servers", body).thenApply(r -> null);
    }

    public CompletableFuture<Void> heartbeat(String serverId, int playerCount, double tps, 
                                              long memoryUsed, long memoryMax, double cpuUsage,
                                              java.util.List<IslandMetrics> islands) {
        var body = new HeartbeatRequest(playerCount, tps, memoryUsed, memoryMax, cpuUsage, islands);
        return post("/api/servers/" + serverId + "/heartbeat", body).thenApply(r -> null);
    }

    public CompletableFuture<Void> unregisterServer(String serverId) {
        return delete("/api/servers/" + serverId).thenApply(r -> null);
    }

    // ═══════════════════════════════════════════════════════════════
    // Island Operations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get or create an island for a player.
     * Returns info about which server to send them to.
     */
    public CompletableFuture<IslandResponse> getOrCreateIsland(UUID playerId) {
        return post("/api/islands/" + playerId, "")
                .thenApply(json -> GSON.fromJson(json, IslandResponse.class));
    }

    /**
     * Get routing info for a player - where should they go?
     */
    public CompletableFuture<RouteResponse> getRoute(UUID playerId) {
        return get("/api/route/" + playerId)
                .thenApply(json -> GSON.fromJson(json, RouteResponse.class));
    }

    /**
     * Mark an island as ready (after loading the world)
     */
    public CompletableFuture<Void> markIslandReady(UUID islandId, String serverId) {
        var body = new MarkReadyRequest(serverId);
        return post("/api/islands/" + islandId + "/ready", body).thenApply(r -> null);
    }

    /**
     * Mark an island as unloaded
     */
    public CompletableFuture<Void> markIslandUnloaded(UUID islandId) {
        return post("/api/islands/" + islandId + "/unload", "").thenApply(r -> null);
    }

    // ═══════════════════════════════════════════════════════════════
    // HTTP Helpers
    // ═══════════════════════════════════════════════════════════════

    private CompletableFuture<String> get(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    private CompletableFuture<String> post(String path, Object body) {
        String json = body instanceof String ? (String) body : GSON.toJson(body);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    private CompletableFuture<String> delete(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // ═══════════════════════════════════════════════════════════════
    // Request/Response Types
    // ═══════════════════════════════════════════════════════════════

    public record ServerRegisterRequest(String id, String type, String host, int port) {}
    public record HeartbeatRequest(int playerCount, double tps, long memoryUsed, long memoryMax, 
                                   double cpuUsage, java.util.List<IslandMetrics> islands) {}
    public record IslandMetrics(String id, int chunks, int entities, int tileEntities, int players) {}
    public record MarkReadyRequest(String serverId) {}

    public record IslandResponse(Island island, String serverId, boolean needsLoading, String error) {}
    public record RouteResponse(String serverId, String error) {}
    
    public record Island(
        String id,
        String ownerId, 
        String serverId,
        String status,
        long createdAt,
        long lastAccessed
    ) {
        public boolean isReady() {
            return "READY".equals(status);
        }
    }
}

