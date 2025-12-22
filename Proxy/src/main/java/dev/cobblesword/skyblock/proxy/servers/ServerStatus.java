package dev.cobblesword.skyblock.proxy.servers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatus
{
    private String serverId;

    private String address;

    private int port;

    private int playerCount;

    private long lastActiveTimestamp = System.currentTimeMillis();
}
