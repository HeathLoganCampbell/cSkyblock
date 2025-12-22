package dev.cobblesword.skyblock.library.modules.skyislands;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.controller.ControllerClient;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.cobblesword.skyblock.library.modules.prepare.PrepareModule;
import dev.cobblesword.skyblock.library.modules.remoteworlds.RemoteWorldsModule;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.commands.IslandCommand;
import dev.cobblesword.skyblock.library.modules.skyislands.commands.SpawnCommand;
import dev.cobblesword.skyblock.library.modules.skyislands.listeners.DeathReturnToIslandListener;
import dev.cobblesword.skyblock.library.modules.skyislands.models.Island;
import dev.cobblesword.skyblock.library.common.world.Worlds;
import dev.cobblesword.skyblock.protocol.requests.RequestLoadIsland;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToSpawn;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerRoute;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkyIslandsModule extends Module
{
    private final IExchange exchange;
    private final PrepareModule prepareModule;
    private final RemoteWorldsModule remoteWorldsModule;
    private final ControllerClient controller;

    public SkyIslandsModule(PrepareModule prepareModule, RemoteWorldsModule remoteWorldsModule, 
                            IExchange exchange, ControllerClient controller)
    {
        super("SkyIslands");
        this.exchange = exchange;
        this.prepareModule = prepareModule;
        this.remoteWorldsModule = remoteWorldsModule;
        this.controller = controller;
    }

    @Override
    protected void onEnable()
    {
        // Listen for island load requests (on island servers)
        if (SkyblockLibrary.get().getServerType() == ServerType.ISLANDS) {
            setupIslandLoadListener();
        }
        
        this.registerCommands(new SpawnCommand(this));
        this.registerCommands(new IslandCommand(this));
        this.registerListener(new DeathReturnToIslandListener(this));
    }

    /**
     * Listen for requests to load islands on this server
     */
    private void setupIslandLoadListener() {
        String myServerId = SkyblockLibrary.get().getServerId();
        
        exchange.subscribe(e -> {
            if (e instanceof RequestLoadIsland request) {
                // Only handle if this request is for us
                if (!myServerId.equals(request.getTargetServer())) {
                    return;
                }
                
                UUID playerId = request.getPlayerId();
                UUID islandId = request.getIslandId();
                
                System.out.println("📥 Loading island " + islandId + " for player " + playerId);
                
                // Load the island, then send player
                loadIslandThenRoutePlayer(playerId, islandId);
            }
        });
    }

    @Override
    protected void onDisable()
    {
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().startsWith("islands/")) {
                UUID islandId = UUID.fromString(world.getName().replace("islands/", ""));
                saveAndUnloadIsland(islandId);
            }
        }
    }

    /**
     * Request to go to an island (called from command)
     */
    public void goToIsland(Player player, UUID islandId) {
        Messages.teleporting(player, "Your Island");

        // Check if island world is already loaded on THIS server
        World localWorld = Bukkit.getWorld("islands/" + islandId);
        if (localWorld != null) {
            player.teleport(localWorld.getSpawnLocation());
            Messages.arrived(player, "Your Island");
            return;
        }

        // Ask controller where to route
        controller.getOrCreateIsland(islandId).thenAccept(response -> {
            if (response.error() != null) {
                Sync.get().run(() -> Messages.error(player, response.error())).execute();
                return;
            }

            String targetServer = response.serverId();
            String currentServer = SkyblockLibrary.get().getServerId();
            
            System.out.println("Island routing: " + currentServer + " -> " + targetServer + " for " + player.getName());
            
            if (targetServer.equals(currentServer)) {
                // We're on the target server - load locally
                loadIslandThenRoutePlayer(player.getUniqueId(), islandId);
            } else {
                // Send request to target server to load island
                RequestLoadIsland loadRequest = new RequestLoadIsland();
                loadRequest.setPlayerId(player.getUniqueId());
                loadRequest.setIslandId(islandId);
                loadRequest.setTargetServer(targetServer);
                exchange.publishMessage(loadRequest);
            }
            
        }).exceptionally(e -> {
            Sync.get().run(() -> Messages.error(player, "Failed to connect to controller")).execute();
            e.printStackTrace();
            return null;
        });
    }

    /**
     * Load island and then route player to this server
     */
    private void loadIslandThenRoutePlayer(UUID playerId, UUID islandId) {
        String myServerId = SkyblockLibrary.get().getServerId();
        
        // Check if already loaded
        World existingWorld = Bukkit.getWorld("islands/" + islandId);
        if (existingWorld != null) {
            System.out.println("✓ Island already loaded, routing player");
            routePlayerHere(playerId, myServerId);
            prepareModule.registerRunnable(playerId, p -> {
                p.teleport(existingWorld.getSpawnLocation());
                Messages.arrived(p, "Your Island");
            });
            return;
        }
        
        // Load the world
        remoteWorldsModule.loadWorld("islands/" + islandId, world -> {
            // Apply memory optimizations (reduces chunks from ~400 to ~25)
            Worlds.optimizeIslandWorld(world);
            
            world.setSpawnLocation(0, 100, 0);
            world.getWorldBorder().setCenter(0, 0);
            world.getWorldBorder().setSize(100);  // 100 blocks diameter

            // Tell controller island is ready
            controller.markIslandReady(islandId, myServerId);

            // Register teleport for when player arrives
            prepareModule.registerRunnable(playerId, p -> {
                p.teleport(world.getSpawnLocation());
                Messages.arrived(p, "Your Island");
            });

            System.out.println("✓ Island " + islandId + " loaded, routing player");
            
            // NOW send the player
            routePlayerHere(playerId, myServerId);
        });
    }

    /**
     * Tell proxy to send player to this server
     */
    private void routePlayerHere(UUID playerId, String serverId) {
        RequestPlayerRoute routeRequest = new RequestPlayerRoute();
        routeRequest.setPlayerId(playerId);
        routeRequest.setTargetServer(serverId);
        exchange.publishMessage(routeRequest);
    }

    /**
     * Request to go to spawn
     */
    public void goToSpawn(Player player) {
        Messages.teleporting(player, "Spawn");

        RequestPlayerGoToSpawn request = new RequestPlayerGoToSpawn();
        request.setPlayerId(player.getUniqueId());
        exchange.publishMessage(request);
    }

    public void saveAndUnloadIsland(UUID islandId)
    {
        String worldName = "islands/" + islandId;
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            for (Player currentPlayer : world.getPlayers()) {
                goToSpawn(currentPlayer);
            }
            this.remoteWorldsModule.saveAndUnloadWorld(worldName);
        }

        controller.markIslandUnloaded(islandId);
    }

    public World getIslandOnServer(UUID islandId)
    {
        return Bukkit.getWorld("islands/" + islandId);
    }

    public Island getIsland(UUID islandId)
    {
        Island island = new Island();
        island.setId(islandId);
        island.setWorldName("islands/" + islandId);
        return island;
    }

    public Island getIslandPlayerIsOn(Player player)
    {
        World world = player.getWorld();
        return getIslandByWorld(world);
    }

    public Island getIslandByWorld(World world)
    {
        String worldName = world.getName();
        if (!worldName.startsWith("islands/")) {
            return null;
        }

        UUID islandId = UUID.fromString(worldName.replace("islands/", ""));
        return getIsland(islandId);
    }
}
