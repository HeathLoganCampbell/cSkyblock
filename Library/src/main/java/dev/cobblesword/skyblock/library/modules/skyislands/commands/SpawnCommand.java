package dev.cobblesword.skyblock.library.modules.skyislands.commands;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import dev.cobblesword.skyblock.library.modules.ICommands;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.SkyIslandsModule;
import dev.jorel.commandapi.CommandAPICommand;

public class SpawnCommand implements ICommands
{
    private final SkyIslandsModule skyIslandsModule;

    public SpawnCommand(SkyIslandsModule skyIslandsModule) {
        this.skyIslandsModule = skyIslandsModule;
    }

    public void registerCommands()
    {
        new CommandAPICommand("spawn")
                .executesPlayer((player, args) -> {
                    if (SkyblockLibrary.get().getServerType() == ServerType.SPAWN) {
                        // Already on spawn server, just teleport to spawn point
                        player.teleport(player.getWorld().getSpawnLocation());
                        Messages.arrived(player, "Spawn");
                        return;
                    }

                    // On island server, go to spawn server
                    skyIslandsModule.goToSpawn(player);
                }).register();
    }
}
