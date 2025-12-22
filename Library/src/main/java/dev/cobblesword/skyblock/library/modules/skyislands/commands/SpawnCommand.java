package dev.cobblesword.skyblock.library.modules.skyislands.commands;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.ICommands;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.SkyIslandsModule;
import dev.cobblesword.skyblock.library.modules.skyislands.models.Island;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;

public class SpawnCommand implements ICommands
{
    private SkyIslandsModule skyIslandsModule;

    public SpawnCommand(SkyIslandsModule skyIslandsModule) {
        this.skyIslandsModule = skyIslandsModule;
    }

    public void registerCommands()
    {
        new CommandAPICommand("spawn")
                .executesPlayer((player, args) -> {

                    if(SkyblockLibrary.get().getServerType() == ServerType.SPAWN)
                    {
                        player.teleport(player.getWorld().getSpawnLocation());
                        Messages.arrived(player, "Spawn");
                        return;
                    }

                    Island islandPlayerIsOn = SkyblockLibrary.get().getSkyIslandsModule().getIslandPlayerIsOn(player);
                    if(islandPlayerIsOn != null)
                    {
                        Messages.teleporting(player, "Spawn");

                        for (Player currentPlayer : islandPlayerIsOn.getPlayersOnIsland()) {
                            SkyblockLibrary.get().getApi().sendPlayerToSpawn(currentPlayer.getUniqueId());
                        }

                        Sync.get().run(() -> {
                            skyIslandsModule.saveAndUnloadIsland(islandPlayerIsOn.getId());
                        }).delay(4).execute();
                    }
                }).register();
    }
}
