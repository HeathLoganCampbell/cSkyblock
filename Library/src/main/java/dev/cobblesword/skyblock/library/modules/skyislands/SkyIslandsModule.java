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
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToIsland;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToSpawn;
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
        // Listen for teleport requests (from proxy)
        if (SkyblockLibrary.get().getServerType() == ServerType.ISLANDS) {
            setupIslandServerHandlers();
        }
        
        setupTeleportListeners();

        this.registerCommands(new SpawnCommand(this));
        this.registerCommands(new IslandCommand(this));
        this.registerListener(new DeathReturnToIslandListener(this));
    }

    /**
     * Setup handlers for island server (loading worlds, preparing players)
     */
    private void setupIslandServerHandlers() {
        // Listen for "load this island" requests
        exchange.subscribe(e -> {
            if (e instanceof RequestPlayerGoToIsland request) {
                String targetServer = request.getTargetServerId();
                if (targetServer == null || !targetServer.equals(SkyblockLibrary.get().getServerId())) {
                    return;
                }

                UUID playerId = request.getPlayerId();
                UUID islandId = request.getIslandId();

                System.out.println("Loading island " + islandId + " for player " + playerId);

                // Load the island world
                remoteWorldsModule.loadWorld("islands/" + islandId, world -> {
                    world.setSpawnLocation(0, 100, 0);
                    world.getWorldBorder().setCenter(0, 0);
                    world.getWorldBorder().setSize(50);

                    // Tell controller island is ready
                    controller.markIslandReady(islandId, SkyblockLibrary.get().getServerId());

                    // Prepare for player arrival
                    prepareModule.registerRunnable(playerId, player -> {
                        player.teleport(world.getSpawnLocation());
                        Sync.get().run(() -> Messages.arrived(player, "Your Island")).delay(5).execute();
                    });

                    System.out.println("✓ Island " + islandId + " ready, waiting for player");
                });
            }
        });
    }

    /**
     * Setup listeners for teleport requests
     */
    private void setupTeleportListeners() {
        // Listen for "go to spawn" requests
        exchange.subscribe(e -> {
            if (e instanceof RequestPlayerGoToSpawn request) {
                Player player = Bukkit.getPlayer(request.getPlayerId());
                if (player != null && player.isOnline()) {
                    // Player is on this server, need to send them to spawn via proxy
                    // The proxy will handle the actual server transfer
                }
            }
        });
    }

    @Override
    protected void onDisable()
    {
        // Unload all islands on this server
        String serverId = SkyblockLibrary.get().getServerId();
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

        // Check if island world is already loaded on this server
        World localWorld = Bukkit.getWorld("islands/" + islandId);
        if (localWorld != null) {
            player.teleport(localWorld.getSpawnLocation());
            Messages.arrived(player, "Your Island");
            return;
        }

        // Ask controller where to go
        controller.getOrCreateIsland(islandId).thenAccept(response -> {
            if (response.error() != null) {
                Sync.get().run(() -> Messages.error(player, response.error())).execute();
                return;
            }

            String targetServer = response.serverId();
            boolean needsLoading = response.needsLoading();

            // Send request to load island (proxy will route the player)
            RequestPlayerGoToIsland request = new RequestPlayerGoToIsland();
            request.setPlayerId(player.getUniqueId());
            request.setIslandId(islandId);
            request.setTargetServerId(targetServer);
            exchange.publishMessage(request);

        }).exceptionally(e -> {
            Sync.get().run(() -> Messages.error(player, "Failed to connect to controller")).execute();
            e.printStackTrace();
            return null;
        });
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
            // Kick all players to spawn
            for (Player currentPlayer : world.getPlayers()) {
                goToSpawn(currentPlayer);
            }

            // Save and unload
            this.remoteWorldsModule.saveAndUnloadWorld(worldName);
        }

        // Tell controller
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
