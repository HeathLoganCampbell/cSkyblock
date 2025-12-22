package dev.cobblesword.skyblock.proxy.controller;

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
 * HTTP client for the Skyblock Controller service (Proxy version - no Bukkit dependencies)
 */
public class ProxyControllerClient {

    private static final Gson GSON = new GsonBuilder().create();
    
    private final String baseUrl;
    private final HttpClient httpClient;

    public ProxyControllerClient(String controllerUrl) {
        this.baseUrl = controllerUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Get routing info for a player - where should they go?
     */
    public CompletableFuture<RouteResponse> getRoute(UUID playerId) {
        return get("/api/route/" + playerId)
                .thenApply(json -> GSON.fromJson(json, RouteResponse.class));
    }

    /**
     * Get or create island, returns server to send player to
     */
    public CompletableFuture<IslandResponse> getOrCreateIsland(UUID playerId) {
        return post("/api/islands/" + playerId, "")
                .thenApply(json -> GSON.fromJson(json, IslandResponse.class));
    }

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

    private CompletableFuture<String> post(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // Response types
    public record RouteResponse(String serverId, String error) {}
    public record IslandResponse(Island island, String serverId, boolean needsLoading, String error) {}
    public record Island(String id, String ownerId, String serverId, String status) {}
}

