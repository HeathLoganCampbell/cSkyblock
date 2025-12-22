package dev.cobblesword.skyblock.library.modules.skyislands.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Island
{
    private UUID id;

    private String displayName = "";

    private UUID worldId;

    private String worldName;

    private int size = 50;

    private int level = 1;

    public World getWorld()
    {
        return Bukkit.getWorld(worldName);
    }

    public List<Player> getPlayersOnIsland()
    {
        return getWorld().getPlayers();
    }
}
