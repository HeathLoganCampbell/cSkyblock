package dev.cobblesword.skyblock.proxy.servers;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.exchange.protocols.KeepAlive;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerManager
{
    private HashMap<String, ServerStatus> serverStatusMap = new HashMap<>();

    public ServerManager(IExchange exchange, Plugin plugin)
    {
        exchange.subscribe(exchangeMessage -> {
            if(exchangeMessage instanceof KeepAlive keepAlive)
            {
                System.out.println("Recieved keep alive from " + keepAlive.getServerId());
                String serverId = keepAlive.getServerId();

                ServerStatus serverStatus = serverStatusMap.get(serverId);
                if(serverStatus == null)
                {
                    serverStatus = new ServerStatus();
                    serverStatus.setServerId(serverId);
                    serverStatus.setAddress(keepAlive.getAddress());
                    serverStatus.setPort(keepAlive.getPort());
                    serverStatus.setLastActiveTimestamp(System.currentTimeMillis());
                    serverStatusMap.put(serverId, serverStatus);

                    onNewServerFound(serverStatus);
                }
                else
                {
                    serverStatus.setLastActiveTimestamp(System.currentTimeMillis());
                }
            }
        });

        ProxyServer.getInstance().getScheduler().schedule(plugin, this::checkServerStatus, 0L, 3L, TimeUnit.SECONDS);
    }

    private void checkServerStatus() {
        long currentTime = System.currentTimeMillis();
        Iterator<ServerStatus> iterator = serverStatusMap.values().iterator();
        while (iterator.hasNext())
        {
            ServerStatus serverStatus = iterator.next();
            if ((currentTime - serverStatus.getLastActiveTimestamp()) > 10000)
            {
                onDeactiveServer(serverStatus);
                iterator.remove();
            }
        }
    }

    public void addServer(String name, String address, int port, boolean restricted) {
        ProxyServer proxy = ProxyServer.getInstance();
        InetSocketAddress serverAddress = new InetSocketAddress(address, port);

        // Create a ServerInfo object for the new server
        ServerInfo serverInfo = proxy.constructServerInfo(name, serverAddress, "A new server", restricted);

        // Add the server to the list of known servers
        proxy.getServers().put(name, serverInfo);
    }

    public void removeServer(String name) {
        ProxyServer proxy = ProxyServer.getInstance();

        if (proxy.getServers().containsKey(name)) {
            proxy.getServers().remove(name);
        } else {
            proxy.getLogger().info("Server " + name + " not found, cannot remove.");
        }
    }

    public void onNewServerFound(ServerStatus serverStatus)
    {
        System.out.println("Added new server " + serverStatus.getServerId() + " on " + serverStatus.getAddress() + ":" + serverStatus.getPort());
        addServer(serverStatus.getServerId(), serverStatus.getAddress(), serverStatus.getPort(), false);
    }

    public void onDeactiveServer(ServerStatus serverStatus)
    {
        removeServer(serverStatus.getServerId());
        System.out.println("Remove inactive server " + serverStatus.getServerId() + " on " + serverStatus.getAddress() + ":" + serverStatus.getPort());
    }

    public String getFittestServerForIsland() {
        List<ServerStatus> serverStatusList = new ArrayList<>(serverStatusMap.values());
        Collections.shuffle(serverStatusList);

        ServerStatus fittestServer = null;
        for (ServerStatus serverStatus : serverStatusList)
        {
            if (!serverStatus.getServerId().toLowerCase().startsWith("island-"))
            {
                continue;
            }

            if (fittestServer == null || fittestServer.getPlayerCount() > serverStatus.getPlayerCount())
            {
                fittestServer = serverStatus;
            }
        }

        if (fittestServer == null) {
            return null;
        }

        return fittestServer.getServerId();
    }
}
