package dev.cobblesword.skyblock.protocol.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class IslandModel
{
    public String serverId;

    public UUID islandId;

    public UUID worldId;
}
