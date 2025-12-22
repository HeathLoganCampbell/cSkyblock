package dev.cobblesword.skyblock.saga.flows.island.teleport;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import dev.cobblesword.skyblock.protocol.models.PlayerDataModel;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToIsland;
import dev.cobblesword.skyblock.protocol.requests.SendPlayerDataWithServer;
import dev.cobblesword.skyblock.protocol.responses.PlayerDataReceived;
import lombok.Setter;

import java.util.UUID;

public class TeleportToIslandFlow
{
    private IExchange exchange;

    @Setter
    private ITeleportToIslandServerHandler serverHandler = null;

    @Setter
    private ITeleportToIslandBungeeHandler bungeeHandler = null;


    public TeleportToIslandFlow(IExchange exchange) {
        this.exchange = exchange;
    }

    // Create
    // Load
    //   Already loaded
    //   Not loaded

    // Server > Bungee         => Bungee > Server           => server > bungee
    // RequestPlayerGoToIsland => SendPlayerDataWithServer => PlayerDataReceived
    public void onSaga(String currentServerId, boolean isBungee)
    {
        exchange.subscribe(e -> {
            boolean isTargedServer = false;

            if(e instanceof ExchangeMessageWithServer messageWithServer)
            {
                isTargedServer = messageWithServer.getServerId().equalsIgnoreCase(currentServerId);
            }

            if(isBungee && e instanceof RequestPlayerGoToIsland requestPlayerGoToIsland)
            {
                System.out.println("requestPlayerGoToIsland <");
                PlayerDataModel playerData = new PlayerDataModel();

                String serverId = bungeeHandler.getServerOfIsland(requestPlayerGoToIsland.getIslandId());

                SendPlayerDataWithServer sendPlayerDataToServer = new SendPlayerDataWithServer();
                sendPlayerDataToServer.setCorrelationId(sendPlayerDataToServer.getCorrelationId());
                sendPlayerDataToServer.setPlayerId(requestPlayerGoToIsland.getPlayerId());
                sendPlayerDataToServer.setServerId(serverId);
                sendPlayerDataToServer.setTargetIslandId(requestPlayerGoToIsland.getIslandId());
                sendPlayerDataToServer.setPlayerData(playerData);

                exchange.publishMessage(sendPlayerDataToServer);
            }

            if(isTargedServer && e instanceof SendPlayerDataWithServer sendPlayerDataToServer)
            {
                System.out.println("sendPlayerDataToServer <");
                UUID playerId = sendPlayerDataToServer.getPlayerId();
                String serverId = sendPlayerDataToServer.getServerId();
                UUID islandId = sendPlayerDataToServer.getTargetIslandId();

                boolean sendPlayerToServer = serverHandler.prepareToRecievePlayer(playerId, serverId, islandId, sendPlayerDataToServer.getPlayerData());

                if(sendPlayerToServer)
                {
                    PlayerDataReceived playerDataReceived = new PlayerDataReceived();
                    playerDataReceived.setCorrelationId(sendPlayerDataToServer.getCorrelationId());
                    playerDataReceived.setPlayerId(playerId);
                    playerDataReceived.setServerId(serverId);

                    exchange.publishMessage(playerDataReceived);
                }
            }

            if(isBungee && e instanceof PlayerDataReceived playerDataReceived)
            {
                System.out.println("playerDataReceived <");
                UUID playerId = playerDataReceived.getPlayerId();
                String serverId = playerDataReceived.getServerId();

                bungeeHandler.sendPlayerToServer(playerId, serverId);
            }
        });
    }

    public void sendRequestToGoToIsland(UUID playerId, UUID islandId)
    {
        RequestPlayerGoToIsland requestPlayerGoToIsland = new RequestPlayerGoToIsland();
        requestPlayerGoToIsland.setPlayerId(playerId);
        requestPlayerGoToIsland.setIslandId(islandId);

        exchange.publishMessage(requestPlayerGoToIsland);
    }
}
