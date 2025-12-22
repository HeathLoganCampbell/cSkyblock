package dev.cobblesword.skyblock.proxy.islands;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class IslandPlayer
{
    private UUID playerId;

    private UUID islandId;
}
