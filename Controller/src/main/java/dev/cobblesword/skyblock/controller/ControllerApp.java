package dev.cobblesword.skyblock.controller;

import dev.cobblesword.skyblock.controller.api.IslandRoutes;
import dev.cobblesword.skyblock.controller.api.ServerRoutes;
import dev.cobblesword.skyblock.controller.service.IslandService;
import dev.cobblesword.skyblock.controller.service.ServerRegistry;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class ControllerApp {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // Services
        ServerRegistry serverRegistry = new ServerRegistry();
        IslandService islandService = new IslandService(serverRegistry);

        // Custom JSON mapper using Gson
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public String toJsonString(Object obj, Type type) {
                return GSON.toJson(obj, type);
            }

            @Override
            public <T> T fromJsonString(String json, Type type) {
                return GSON.fromJson(json, type);
            }
        };

        // Create Javalin app
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(gsonMapper);
            config.http.defaultContentType = "application/json";
        });

        // Register routes
        ServerRoutes.register(app, serverRegistry);
        IslandRoutes.register(app, islandService);

        // Health check
        app.get("/health", ctx -> ctx.json(new HealthResponse("ok")));

        // Start server
        app.start(port);

        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║     Skyblock Controller Started       ║");
        System.out.println("║     http://localhost:" + port + "              ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down controller...");
            app.stop();
        }));
    }

    record HealthResponse(String status) {}
}

