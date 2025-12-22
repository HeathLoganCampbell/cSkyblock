package dev.cobblesword.skyblock.controller.api;

import dev.cobblesword.skyblock.controller.model.Server;
import dev.cobblesword.skyblock.controller.service.ServerRegistry;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ServerRoutes {

    public static void register(Javalin app, ServerRegistry registry) {

        // Register a server
        app.post("/api/servers", ctx -> {
            RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
            Server server = new Server(req.id, req.type, req.host, req.port);
            registry.register(server);
            ctx.json(new Response("registered", server.getId()));
        });

        // Heartbeat
        app.post("/api/servers/{id}/heartbeat", ctx -> {
            String serverId = ctx.pathParam("id");
            HeartbeatRequest req = ctx.bodyAsClass(HeartbeatRequest.class);
            registry.heartbeat(serverId, req.playerCount);
            ctx.json(new Response("ok", serverId));
        });

        // Unregister
        app.delete("/api/servers/{id}", ctx -> {
            String serverId = ctx.pathParam("id");
            registry.unregister(serverId);
            ctx.json(new Response("unregistered", serverId));
        });

        // List all servers
        app.get("/api/servers", ctx -> {
            ctx.json(registry.getOnlineServers());
        });

        // Get specific server
        app.get("/api/servers/{id}", ctx -> {
            String serverId = ctx.pathParam("id");
            registry.get(serverId).ifPresentOrElse(
                ctx::json,
                () -> ctx.status(404).json(new ErrorResponse("Server not found"))
            );
        });
    }

    // Request/Response types
    record RegisterRequest(String id, String type, String host, int port) {}
    record HeartbeatRequest(int playerCount) {}
    record Response(String status, String serverId) {}
    record ErrorResponse(String error) {}
}

