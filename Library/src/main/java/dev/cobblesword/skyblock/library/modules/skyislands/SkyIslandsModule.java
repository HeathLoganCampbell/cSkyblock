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
        this.registerCommands(new SpawnCommand(this));
        this.registerCommands(new IslandCommand(this));
        this.registerListener(new DeathReturnToIslandListener(this));
    }

    @Override
    protected void onDisable()
    {
        // Unload all islands on this server
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().startsWith("islands/")) {
                UUID islandId = UUID.fromString(world.getName().replace("islands/", ""));
                saveAndUnloadIsland(islandId);
            }
        }
    }

    /**
     * Request to go to an island (called from command)
     * Controller handles routing - proxy sends player to correct server
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

        // Ask controller to route us (proxy will handle server transfer)
        controller.getOrCreateIsland(islandId).thenAccept(response -> {
            if (response.error() != null) {
                Sync.get().run(() -> Messages.error(player, response.error())).execute();
                return;
            }

            String targetServer = response.serverId();
            
            // If we're on the target server, load the island locally
            if (targetServer.equals(SkyblockLibrary.get().getServerId())) {
                loadIslandLocally(player, islandId);
            }
            // Otherwise, proxy will route us via the route API
            
        }).exceptionally(e -> {
            Sync.get().run(() -> Messages.error(player, "Failed to connect to controller")).execute();
            e.printStackTrace();
            return null;
        });
    }

    /**
     * Load island world on this server and teleport player
     */
    public void loadIslandLocally(Player player, UUID islandId) {
        remoteWorldsModule.loadWorld("islands/" + islandId, world -> {
            world.setSpawnLocation(0, 100, 0);
            world.getWorldBorder().setCenter(0, 0);
            world.getWorldBorder().setSize(50);

            // Tell controller island is ready
            controller.markIslandReady(islandId, SkyblockLibrary.get().getServerId());

            // Register teleport for when player arrives (or if already here)
            prepareModule.registerRunnable(player.getUniqueId(), p -> {
                p.teleport(world.getSpawnLocation());
                Sync.get().run(() -> Messages.arrived(p, "Your Island")).delay(5).execute();
            });

            // If player is already on this server, teleport now
            Player localPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (localPlayer != null && localPlayer.isOnline()) {
                localPlayer.teleport(world.getSpawnLocation());
                Messages.arrived(localPlayer, "Your Island");
            }

            System.out.println("✓ Island " + islandId + " loaded for " + player.getName());
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
