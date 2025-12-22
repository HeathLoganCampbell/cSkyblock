package dev.cobblesword.skyblock.saga.flows.island.load;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import dev.cobblesword.skyblock.protocol.models.IslandModel;
import dev.cobblesword.skyblock.protocol.requests.LoadIslandOnServer;
import dev.cobblesword.skyblock.protocol.requests.LoadIslandRequest;
import dev.cobblesword.skyblock.protocol.responses.IslandLoaded;
import dev.cobblesword.skyblock.saga.flows.island.teleport.TeleportToIslandFlow;
import lombok.Setter;

import java.util.UUID;

public class LoadIslandFlow
{
    private IExchange exchange;

    private TeleportToIslandFlow teleportToIslandFlow;

    @Setter
    private ILoadIslandServerHandler serverHandler = null;

    @Setter
    private ILoadIslandBungeeHandler bungeeHandler = null;

    public LoadIslandFlow(IExchange exchange, TeleportToIslandFlow teleportToIslandFlow)
    {
        this.exchange = exchange;
        this.teleportToIslandFlow = teleportToIslandFlow;
    }

//    public void sendRequestToCreateIsland(UUID playerId)
//    {
//        CreateIslandRequest createIslandRequest = new CreateIslandRequest();
//        createIslandRequest.setPlayerId(playerId);
//
//        this.exchange.publishMessage(createIslandRequest);
//    }

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

                if (isBungee && e instanceof LoadIslandRequest loadIsland)
                {
                    System.out.println("loadIslandRequest recieved");

                    // Check if island already exists
                    IslandModel activeIsland = bungeeHandler.getActiveIsland(loadIsland.getIslandId());
                    if(activeIsland != null && activeIsland.getServerId() != null)
                    {
                        System.out.println("> Island already loaded, Teleporting player");
                        teleportToIslandFlow.sendRequestToGoToIsland(loadIsland.getRequesterPlayerId(), activeIsland.getIslandId());
                    }
                    else
                    {
                        System.out.println("> Island not loaded, gonna load it");
                        String serverId = bungeeHandler.getBestFitServerForNewIsland();
                        UUID playerId = loadIsland.getRequesterPlayerId();

                        LoadIslandOnServer loadIslandOnServer = new LoadIslandOnServer();
                        loadIslandOnServer.setPlayerId(playerId);
                        loadIslandOnServer.setServerId(serverId);
                        loadIslandOnServer.setCorrelationId(loadIsland.getCorrelationId());

                        exchange.publishMessage(loadIslandOnServer);
                    }
                }

                if (isTargedServer && e instanceof LoadIslandOnServer loadIslandOnServer) {
                    System.out.println("CreateIslandOnServer recieved");
                    UUID requesterPlayerId = loadIslandOnServer.getRequesterPlayerId();

                    serverHandler.loadIsland(requesterPlayerId, island -> {
                        IslandLoaded islandLoaded = new IslandLoaded();
                        islandLoaded.setCorrelationId(loadIslandOnServer.getCorrelationId());
                        islandLoaded.setRequesterPlayerId(requesterPlayerId);
                        islandLoaded.setIslandId(island.getIslandId());
                        islandLoaded.setServerId(loadIslandOnServer.getServerId());

                        exchange.publishMessage(islandLoaded);
                    });
                }

                if (isBungee && e instanceof IslandLoaded islandLoaded) {
                    System.out.println("IslandCreated recieved");
                    bungeeHandler.onIslandLoaded(islandLoaded);

                    teleportToIslandFlow.sendRequestToGoToIsland(islandLoaded.getRequesterPlayerId(), islandLoaded.getIslandId());
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        });
    }
}
