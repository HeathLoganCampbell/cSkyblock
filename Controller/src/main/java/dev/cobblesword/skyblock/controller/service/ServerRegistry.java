package dev.cobblesword.skyblock.controller.service;

import dev.cobblesword.skyblock.controller.api.ServerRoutes.HeartbeatRequest;
import dev.cobblesword.skyblock.controller.model.Server;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerRegistry {

    private final Map<String, Server> servers = new ConcurrentHashMap<>();

    public void register(Server server) {
        servers.put(server.getId(), server);
        System.out.println("Server registered: " + server.getId() + " (" + server.getType() + ")");
    }

    public void heartbeat(String serverId, HeartbeatRequest req) {
        Server server = servers.get(serverId);
        if (server != null) {
            server.setLastHeartbeat(System.currentTimeMillis());
            server.setPlayerCount(req.playerCount());
            server.setTps(req.tps());
            server.setMemoryUsed(req.memoryUsed());
            server.setMemoryMax(req.memoryMax());
            server.setCpuUsage(req.cpuUsage());
            
            // Update island metrics
            if (req.islands() != null) {
                server.setIslands(
                    req.islands().stream()
                        .map(m -> new Server.IslandMetrics(m.id(), m.chunks(), m.entities(), m.tileEntities(), m.players()))
                        .collect(Collectors.toList())
                );
            }
        }
    }

    public void unregister(String serverId) {
        servers.remove(serverId);
        System.out.println("Server unregistered: " + serverId);
    }

    public Optional<Server> get(String serverId) {
        return Optional.ofNullable(servers.get(serverId));
    }

    public Collection<Server> getAll() {
        return servers.values();
    }

    public Collection<Server> getOnlineServers() {
        return servers.values().stream()
                .filter(Server::isOnline)
                .toList();
    }

    public Collection<Server> getIslandServers() {
        return servers.values().stream()
                .filter(Server::isOnline)
                .filter(Server::isIslandServer)
                .toList();
    }

    public Optional<Server> getSpawnServer() {
        return servers.values().stream()
                .filter(Server::isOnline)
                .filter(Server::isSpawnServer)
                .findFirst();
    }

    /**
     * Get the best island server for a new island (least players)
     */
    public Optional<Server> getBestIslandServer() {
        return servers.values().stream()
                .filter(Server::isOnline)
                .filter(Server::isIslandServer)
                .min(Comparator.comparingInt(Server::getPlayerCount));
    }

    /**
     * Get total player count across all servers
     */
    public int getTotalPlayers() {
        return servers.values().stream()
                .filter(Server::isOnline)
                .mapToInt(Server::getPlayerCount)
                .sum();
    }
}
