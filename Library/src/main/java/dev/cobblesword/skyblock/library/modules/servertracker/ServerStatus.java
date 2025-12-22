package dev.cobblesword.skyblock.library.modules.servertracker;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatus
{
    private String serverId;

    private ServerType serverType;

    private int playerCount = 0;

    private long lastUpdated = System.currentTimeMillis();

    public ServerStatus(String serverId) {
        this.serverId = serverId;
    }
}
