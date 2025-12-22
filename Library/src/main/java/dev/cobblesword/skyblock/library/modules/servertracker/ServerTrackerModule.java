package dev.cobblesword.skyblock.library.modules.servertracker;

import dev.cobblesword.exchange.protocols.KeepAlive;
import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.jorel.commandapi.CommandAPICommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ServerTrackerModule extends Module
{
    private HashMap<String, ServerStatus> serverStatusHashMap = new HashMap<>();

    @Getter
    private String serverId = "unknown-00";

    public ServerTrackerModule()
    {
        super("ServerTracker");
    }

    @Override
    protected void onEnable()
    {
        JavaPlugin plugin = SkyblockLibrary.get().getPlugin();
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("server.id")) {
            config.set("server.id", "Island-01");
            plugin.saveConfig();
        }

        this.serverId = config.getString("server.id", "unknown-00");

        String serverId = SkyblockLibrary.get().getServerId();

        Sync.get().run(() -> {
            KeepAlive keepAlive = new KeepAlive();
            keepAlive.setServerId(serverId);
            keepAlive.setAddress(Bukkit.getIp());
            keepAlive.setPort(Bukkit.getPort());
            keepAlive.setPlayerCount(Bukkit.getOnlinePlayers().size());
            keepAlive.setTimestamp(System.currentTimeMillis());

            SkyblockLibrary.get().getMessageExchange().publishMessage(keepAlive);

            removeInactiveServers();
        }).interval(60).execute();

        SkyblockLibrary.get().getMessageExchange().subscribe((message) -> {
            if (message instanceof KeepAlive keepAlive)
            {
                ServerStatus otherServerStatus = serverStatusHashMap.get(keepAlive.getServerId());

                if(otherServerStatus == null)
                {
                    otherServerStatus = new ServerStatus(keepAlive.getServerId());
                    otherServerStatus.setServerType(this.getServerTypeByServerId(otherServerStatus.getServerId()));
                    serverStatusHashMap.put(keepAlive.getServerId(), otherServerStatus);
                }

                otherServerStatus.setPlayerCount(keepAlive.getPlayerCount());
                otherServerStatus.setLastUpdated(System.currentTimeMillis());
            }
        });

        registerCommands();
    }

    public void registerCommands()
    {
        new CommandAPICommand("networkStatus")
                .executes((sender, args) -> {
                    List<String> serverStatusList = new ArrayList<>();
                    for (ServerStatus value : this.serverStatusHashMap.values())
                    {
                        if (value.getServerId().equalsIgnoreCase(getServerId()))
                        {
                            serverStatusList.add(ChatColor.GREEN + value.getServerId() + ChatColor.GRAY + " (" + value.getPlayerCount() + " players )");
                        }
                        else
                        {
                            serverStatusList.add(ChatColor.WHITE + value.getServerId() + ChatColor.GRAY + " (" + value.getPlayerCount() + " players )");
                        }
                    }
                    String serverStatusMessage = String.join(ChatColor.GRAY + ", ", serverStatusList);
                    sender.sendMessage(ChatColor.GRAY + "Active Servers: " + serverStatusMessage);
                })
                .register();
    }

    private void removeInactiveServers()
    {
        serverStatusHashMap.values().removeIf(next -> System.currentTimeMillis() - next.getLastUpdated() > 5_000L);
    }

    @Override
    protected void onDisable()
    {

    }

    private ServerType getServerTypeByServerId(String serverId)
    {
        if(serverId.toLowerCase().startsWith("spawn-"))
        {
            return ServerType.SPAWN;
        }
        else if(serverId.toLowerCase().startsWith("islands-") || serverId.toLowerCase().startsWith("island-"))
        {
            return ServerType.ISLANDS;
        }
        else
        {
            return ServerType.UNKNOWN;
        }
    }

    public ServerType getServerType()
    {
        return  getServerTypeByServerId(this.getServerId());
    }

    public boolean isServerTypeAvailable(ServerType serverType)
    {
        for (ServerStatus serverStatus : this.serverStatusHashMap.values()) {
            if (serverStatus.getServerType().equals(serverType)) {
                return true;
            }
        }

        return false;
    }
}