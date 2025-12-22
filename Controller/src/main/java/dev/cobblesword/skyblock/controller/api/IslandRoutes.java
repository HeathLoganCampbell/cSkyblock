package dev.cobblesword.skyblock.controller.api;

import dev.cobblesword.skyblock.controller.model.Island;
import dev.cobblesword.skyblock.controller.service.IslandService;
import io.javalin.Javalin;

import java.util.UUID;

public class IslandRoutes {

    public static void register(Javalin app, IslandService islandService) {

        // Get or create island for player
        // POST /api/islands/{playerId}
        // Returns: { island: {...}, serverId: "...", needsLoading: true/false }
        app.post("/api/islands/{playerId}", ctx -> {
            UUID playerId = UUID.fromString(ctx.pathParam("playerId"));
            IslandService.IslandResult result = islandService.getOrCreate(playerId);

            if (result.error() != null) {
                ctx.status(503).json(new ErrorResponse(result.error()));
                return;
            }

            ctx.json(new IslandResponse(
                result.island(),
                result.island().getServerId(),
                result.needsLoading()
            ));
        });

        // Get island info
        // GET /api/islands/{islandId}
        app.get("/api/islands/{islandId}", ctx -> {
            UUID islandId = UUID.fromString(ctx.pathParam("islandId"));
            islandService.get(islandId).ifPresentOrElse(
                ctx::json,
                () -> ctx.status(404).json(new ErrorResponse("Island not found"))
            );
        });

        // Mark island as ready (called by island server after loading world)
        // POST /api/islands/{islandId}/ready
        app.post("/api/islands/{islandId}/ready", ctx -> {
            UUID islandId = UUID.fromString(ctx.pathParam("islandId"));
            ReadyRequest req = ctx.bodyAsClass(ReadyRequest.class);
            islandService.markReady(islandId, req.serverId);
            ctx.json(new StatusResponse("ready"));
        });

        // Mark island as unloaded
        // POST /api/islands/{islandId}/unload
        app.post("/api/islands/{islandId}/unload", ctx -> {
            UUID islandId = UUID.fromString(ctx.pathParam("islandId"));
            islandService.markUnloaded(islandId);
            ctx.json(new StatusResponse("unloaded"));
        });

        // Route player - where should they go?
        // GET /api/route/{playerId}
        // Returns: { serverId: "islands-1" } or { error: "..." }
        app.get("/api/route/{playerId}", ctx -> {
            UUID playerId = UUID.fromString(ctx.pathParam("playerId"));
            IslandService.RouteResult result = islandService.route(playerId);

            if (result.error() != null) {
                ctx.status(503).json(new ErrorResponse(result.error()));
                return;
            }

            ctx.json(new RouteResponse(result.serverId()));
        });
    }

    // Request/Response types
    record ReadyRequest(String serverId) {}
    record IslandResponse(Island island, String serverId, boolean needsLoading) {}
    record RouteResponse(String serverId) {}
    record StatusResponse(String status) {}
    record ErrorResponse(String error) {}
}

