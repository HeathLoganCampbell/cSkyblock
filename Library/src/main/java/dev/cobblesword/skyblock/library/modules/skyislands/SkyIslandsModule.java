package dev.cobblesword.skyblock.library.modules.skyislands;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.cobblesword.skyblock.library.modules.prepare.PrepareModule;
import dev.cobblesword.skyblock.library.modules.remoteworlds.RemoteWorldsModule;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.commands.IslandCommand;
import dev.cobblesword.skyblock.library.modules.skyislands.commands.SpawnCommand;
import dev.cobblesword.skyblock.library.modules.skyislands.listeners.DeathReturnToIslandListener;
import dev.cobblesword.skyblock.library.modules.skyislands.models.Island;
import dev.cobblesword.skyblock.protocol.models.IslandModel;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class SkyIslandsModule extends Module
{
    private IExchange exchange;

    private PrepareModule prepareModule;

    private RemoteWorldsModule remoteWorldsModule;

    public SkyIslandsModule(PrepareModule prepareModule, RemoteWorldsModule remoteWorldsModule, IExchange exchange)
    {
        super("SkyIslands");
        this.exchange = exchange;
        this.prepareModule = prepareModule;
        this.remoteWorldsModule = remoteWorldsModule;
    }

    @Override
    protected void onEnable()
    {

        if(SkyblockLibrary.get().getServerType() == ServerType.ISLANDS)
        {
            SkyblockLibrary.get().getApi().getCreateIslandFlow().setServerHandler((requesterPlayerId, callback) -> {
                UUID islandId = requesterPlayerId;

                int islandSize = 50;

                remoteWorldsModule.loadWorld("islands/" + islandId, world -> {
                    world.setSpawnLocation(0, 100, 0);
                    world.getWorldBorder().setCenter(0, 0);
                    world.getWorldBorder().setSize(islandSize);
                    callback.accept(new IslandModel(SkyblockLibrary.get().getServerId(), islandId, islandId));
                    setIslandsServer(islandId, SkyblockLibrary.get().getServerId());
                });
            });

            SkyblockLibrary.get().getApi().getTeleportToIslandFlow().setServerHandler((playerId, serverId, islandId, playerData) -> {
                World world = Bukkit.getWorld("islands/" + islandId);

                if(world == null)
                {
                    System.out.println(">>>>>>>>World not loaded yet");
                    System.out.println(" could not find world islands/" + islandId);
                    return false;
                }

                Player onlinePlayer = Bukkit.getPlayer(playerId);
                boolean isOnCurrentServer = onlinePlayer != null;
                if(isOnCurrentServer)
                {
                    System.out.println("Player already on server " + onlinePlayer);

                    Sync.get().run(() -> onlinePlayer.teleport(world.getSpawnLocation())).execute();

                    return false;
                }
                else
                {
                    System.out.println("Player not on same server");
                    prepareModule.registerRunnable(playerId, player -> {
                        player.setPlayerListHeaderFooter("Skyblock", "Server: " + SkyblockLibrary.get().getServerId());
                        player.teleport(world.getSpawnLocation());
                        
                        // Show arrival message
                        Sync.get().run(() -> {
                            Messages.arrived(player, "Your Island");
                        }).delay(5).execute();
                    });
                    return true;
                }
            });

            SkyblockLibrary.get().getApi().getLoadIslandFlow().setServerHandler((requesterPlayerId, callback) -> {
                UUID islandId = requesterPlayerId;

                remoteWorldsModule.loadWorld("islands/" + islandId, world -> {
                    callback.accept(new IslandModel(SkyblockLibrary.get().getServerId(), islandId, islandId));
                    setIslandsServer(islandId, SkyblockLibrary.get().getServerId());
                });
            });
        }

        this.registerCommands(new SpawnCommand(this));
        this.registerCommands(new IslandCommand(this));

        this.registerListener(new DeathReturnToIslandListener(this));
    }

    @Override
    protected void onDisable()
    {
        String serverId = SkyblockLibrary.get().getServerId();
        List<String> islandIds = SkyblockLibrary.get().getMessageExchange().getListItems("server-islands-" + serverId);
        for (String islandIdStr : islandIds)
        {
            UUID islandId = UUID.fromString(islandIdStr);
            World world = getIslandOnServer(islandId);
            if (world != null)
            {
                this.saveAndUnloadIsland(islandId);
            }
        }

        SkyblockLibrary.get().getMessageExchange().delete("server-islands-" + serverId);
    }

    public void sendToIsland(Player player, UUID islandId)
    {
        SkyblockLibrary.get().getApi().sendPlayerToIsland(player.getUniqueId(), islandId);
    }

    public World getIslandOnServer(UUID islandId)
    {
        return Bukkit.getWorld("islands/" + islandId);
    }

    public String getIslandsServer(UUID islandId)
    {
        return this.exchange.get("island-" + islandId);
    }

    public void setIslandsServer(UUID islandId, String serverId)
    {
        this.exchange.set("island-" + islandId, serverId);
        this.exchange.addToList("server-islands-" + serverId, islandId.toString());
    }

    private void deleteIslandsServer(UUID islandId)
    {
        String serverId = SkyblockLibrary.get().getServerId();
        SkyblockLibrary.get().getMessageExchange().delete("island-" + islandId);
        SkyblockLibrary.get().getMessageExchange().removeFromList("server-islands-" + serverId, islandId.toString());
    }

    public void saveAndUnloadIsland(UUID islandId)
    {
        String worldName = "islands/" + islandId;

        World world = Bukkit.getWorld(worldName);

        for (Player currentPlayer : world.getPlayers()) {
            SkyblockLibrary.get().getApi().sendPlayerToSpawn(currentPlayer.getUniqueId());
        }

        this.remoteWorldsModule.saveAndUnloadWorld("islands/" + islandId);
        this.deleteIslandsServer(islandId);
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
        if(!worldName.startsWith("islands/"))
        {
            return null;
        }

        UUID islandId = UUID.fromString(worldName.replace("islands/", ""));

        return getIsland(islandId);
    }
}
