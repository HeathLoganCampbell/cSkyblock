package dev.cobblesword.skyblock.proxy.islands;

import dev.cobblesword.skyblock.protocol.models.IslandModel;
import dev.cobblesword.skyblock.protocol.responses.IslandCreated;
import dev.cobblesword.skyblock.protocol.responses.IslandLoaded;
import dev.cobblesword.skyblock.proxy.servers.ServerManager;
import dev.cobblesword.skyblock.saga.SkyblockApi;
import dev.cobblesword.skyblock.saga.flows.island.create.ICreateIslandBungeeHandler;
import dev.cobblesword.skyblock.saga.flows.island.load.ILoadIslandBungeeHandler;
import dev.cobblesword.skyblock.saga.flows.island.teleport.ITeleportToIslandBungeeHandler;
import dev.cobblesword.skyblock.saga.flows.spawn.teleport.ITeleportToSpawnBungeeHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.UUID;

public class IslandManager
{
    private HashMap<UUID, Island> activeIslandsMap = new HashMap<>();

    private HashMap<UUID, IslandPlayer> activePlayerMap = new HashMap<>();

    public IslandManager(ServerManager serverManager, SkyblockApi skyblockApi, ProxyServer proxy) {
        skyblockApi.getCreateIslandFlow().setBungeeHandler(new ICreateIslandBungeeHandler() {
            @Override
            public String getBestFitServerForNewIsland()
            {
                return serverManager.getFittestServerForIsland();
            }

            @Override
            public void onIslandCreated(IslandCreated islandCreated)
            {
                Island island = new Island();
                island.setId(islandCreated.getIslandId());
                island.setServerId(islandCreated.getServerId());

                IslandPlayer islandPlayer = new IslandPlayer();
                islandPlayer.setIslandId(island.getId());
                islandPlayer.setPlayerId(islandCreated.getRequesterPlayerId());

                activePlayerMap.put(islandPlayer.getPlayerId(), islandPlayer);
                activeIslandsMap.put(island.getId(), island);
                System.out.println("Created " + island.getId() + " on " + islandCreated.getServerId());
            }
        });

        skyblockApi.getTeleportToIslandFlow().setBungeeHandler(new ITeleportToIslandBungeeHandler() {
            @Override
            public void sendPlayerToServer(UUID playerId, String serverId)
            {
                System.out.println("sending " + playerId + " to " + serverId);
                ProxiedPlayer player = proxy.getPlayer(playerId);

                if (player != null) {
                    // Get the target server by name
                    ServerInfo targetServer = proxy.getServerInfo(serverId);

                    if (targetServer != null) {
                        // Use the connect() method to send the player to the target server
                        player.connect(targetServer);
                    } else {
                        player.sendMessage("§c✖ §7Server unavailable. Please try again.");
                    }
                } else {
                    // Handle the case where the player is not online
                    // You can also log this or send a message to the console
                    System.out.println("Player with UUID " + playerId + " is not online.");
                }
            }

            @Override
            public String getServerOfIsland(UUID islandId) {
                Island island = activeIslandsMap.get(islandId);

                if (island == null) {
                    System.out.println(" could not find island " + islandId);
                    return null;
                }

                return island.getServerId();
            }
        });

        skyblockApi.getLoadIslandFlow().setBungeeHandler(new ILoadIslandBungeeHandler() {
            @Override
            public IslandModel getActiveIsland(UUID islandId) {
                Island island = activeIslandsMap.get(islandId);

                if(island == null)
                {
                    return null;
                }

                return new IslandModel(island.getServerId(), island.getId(), island.getId());
            }

            @Override
            public String getBestFitServerForNewIsland() {
                return serverManager.getFittestServerForIsland();
            }

            @Override
            public void onIslandLoaded(IslandLoaded islandLoaded)
            {
                Island island = new Island();
                island.setId(islandLoaded.getIslandId());
                island.setServerId(islandLoaded.getServerId());

                IslandPlayer islandPlayer = new IslandPlayer();
                islandPlayer.setIslandId(island.getId());
                islandPlayer.setPlayerId(islandLoaded.getRequesterPlayerId());

                activePlayerMap.put(islandPlayer.getPlayerId(), islandPlayer);
                activeIslandsMap.put(island.getId(), island);
                System.out.println("Loaded " + island.getId() + " on " + islandLoaded.getServerId());
            }
        });

        skyblockApi.getTeleportToSpawnFlow().setBungeeHandler(new ITeleportToSpawnBungeeHandler() {
            @Override
            public void sendPlayerToServer(UUID playerId, String serverId)
            {
                System.out.println("sending " + playerId + " to " + serverId);
                ProxiedPlayer player = proxy.getPlayer(playerId);

                if (player != null) {
                    // Get the target server by name
                    ServerInfo targetServer = proxy.getServerInfo(serverId);

                    if (targetServer != null) {
                        // Use the connect() method to send the player to the target server
                        player.connect(targetServer);
                    } else {
                        player.sendMessage("§c✖ §7Server unavailable. Please try again.");
                    }
                } else {
                    // Handle the case where the player is not online
                    // You can also log this or send a message to the console
                    System.out.println("Player with UUID " + playerId + " is not online.");
                }
            }
        });
    }
}
