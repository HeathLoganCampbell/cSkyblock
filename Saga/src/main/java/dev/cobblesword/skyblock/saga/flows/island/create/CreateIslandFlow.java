package dev.cobblesword.skyblock.saga.flows.island.create;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import dev.cobblesword.skyblock.protocol.requests.CreateIslandOnServer;
import dev.cobblesword.skyblock.protocol.requests.CreateIslandRequest;
import dev.cobblesword.skyblock.protocol.responses.IslandCreated;
import dev.cobblesword.skyblock.saga.flows.island.teleport.TeleportToIslandFlow;
import lombok.Setter;

import java.util.UUID;

public class CreateIslandFlow
{
    private IExchange exchange;

    private TeleportToIslandFlow teleportToIslandFlow;

    @Setter
    private ICreateIslandServerHandler serverHandler = null;

    @Setter
    private ICreateIslandBungeeHandler bungeeHandler = null;

    public CreateIslandFlow(IExchange exchange, TeleportToIslandFlow teleportToIslandFlow)
    {
        this.exchange = exchange;
        this.teleportToIslandFlow = teleportToIslandFlow;
    }

    public void sendRequestToCreateIsland(UUID playerId)
    {
        CreateIslandRequest createIslandRequest = new CreateIslandRequest();
        createIslandRequest.setPlayerId(playerId);

        this.exchange.publishMessage(createIslandRequest);
    }

    // Server > Bungee     => Bungee > Server      => server > bungee  => bungee > server        => server > bungee
    // CreateIslandRequest => CreateIslandOnServer => IslandCreated    => SendPlayerDataToServer => PlayerDataReceived
    public void onSaga(String currentServerId, boolean isBungee)
    {
        this.exchange.subscribe(e -> {
            try {
                boolean isTargedServer = false;

                if (e instanceof ExchangeMessageWithServer messageWithServer) {
                    isTargedServer = messageWithServer.getServerId().equalsIgnoreCase(currentServerId);
                }

                if (isBungee && e instanceof CreateIslandRequest createIsland) {
                    System.out.println("CreateIslandRequest recieved");
                    String serverId = bungeeHandler.getBestFitServerForNewIsland();
                    UUID playerId = createIsland.getPlayerId();

                    CreateIslandOnServer createIslandOnServer = new CreateIslandOnServer(playerId);
                    createIslandOnServer.setServerId(serverId);
                    createIslandOnServer.setCorrelationId(createIsland.getCorrelationId());

                    exchange.publishMessage(createIslandOnServer);
                }


                if (isTargedServer && e instanceof CreateIslandOnServer createIslandOnServer) {
                    System.out.println("CreateIslandOnServer recieved");
                    UUID requesterPlayerId = createIslandOnServer.getRequesterPlayerId();


                   serverHandler.createIsland(requesterPlayerId, island -> {
                       IslandCreated islandCreated = new IslandCreated();
                       islandCreated.setCorrelationId(createIslandOnServer.getCorrelationId());
                       islandCreated.setRequesterPlayerId(requesterPlayerId);
                       islandCreated.setIslandId(island.getIslandId());
                       islandCreated.setServerId(createIslandOnServer.getServerId());

                       exchange.publishMessage(islandCreated);
                   });
                }

                if (isBungee && e instanceof IslandCreated islandCreated) {
                    System.out.println("IslandCreated recieved");
                    bungeeHandler.onIslandCreated(islandCreated);
                    teleportToIslandFlow.sendRequestToGoToIsland(islandCreated.getRequesterPlayerId(), islandCreated.getIslandId());
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        });
    }
}
