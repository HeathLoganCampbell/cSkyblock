package dev.cobblesword.skyblock.library.modules.skyislands.commands;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.CC;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.ICommands;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.SkyIslandsModule;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IslandCommand implements ICommands {

    private SkyIslandsModule skyIslandsModule;

    public IslandCommand(SkyIslandsModule skyIslandsModule) {
        this.skyIslandsModule = skyIslandsModule;
    }

    public void registerCommands() {
        // Create Island Command
        new CommandAPICommand("island")
                .withAliases("is")
                .withSubcommand(
                        new CommandAPICommand("create")
                                .withPermission("island.create")
                                .executesPlayer((sender, args) -> {
                                    Messages.info(sender, "Creating your island...");
                                    Messages.teleporting(sender, "New Island");
                                    SkyblockLibrary.get().getApi().createIsland(sender.getUniqueId());
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("goto")
                                .withPermission("island.goto")
                                .withArguments(new PlayerArgument("targetPlayer"))
                                .executesPlayer((sender, args) -> {
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get(0);
                                    UUID targetPlayerId = targetPlayer.getUniqueId();
                                    Messages.teleporting(sender, targetPlayer.getName() + "'s Island");
                                    SkyblockLibrary.get().getApi().sendPlayerToIsland(sender.getUniqueId(), targetPlayerId);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("leave")
                                .withPermission("island.leave")
                                .executesPlayer((sender, args) -> {
                                    Messages.info(sender, "Leaving island...");
                                    saveIsland(sender, sender);
                                })
                                .withArguments(new PlayerArgument("targetPlayer"))
                                .executesPlayer((sender, args) -> {
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get(0);
                                    Messages.info(sender, "Saving island...");
                                    saveIsland(sender, targetPlayer);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("load")
                                .withPermission("island.load")
                                .withArguments(new PlayerArgument("targetPlayer"))
                                .executesPlayer((sender, args) -> {
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get(0);
                                    UUID targetIslandId = targetPlayer.getUniqueId();
                                    Messages.info(sender, "Saving island " + Messages.highlight(targetPlayer.getName()));
                                    SkyblockLibrary.get().getSkyIslandsModule().saveAndUnloadIsland(targetIslandId);
                                })
                )
                .executesPlayer((player, args) -> {
                    if (!SkyblockLibrary.get().getServerTrackerModule().isServerTypeAvailable(ServerType.ISLANDS))
                    {
                        Messages.error(player, "Island servers are currently unavailable. Try again in a moment!");
                        return;
                    }

                    UUID islandId = player.getUniqueId();
                    String islandServerId = this.skyIslandsModule.getIslandsServer(islandId);

                    if(islandServerId == null)
                    {
                        // Need to create/load island
                        Messages.teleporting(player, "Your Island");
                        SkyblockLibrary.get().getApi().createIsland(player.getUniqueId());
                    }
                    else if(islandServerId.equalsIgnoreCase(SkyblockLibrary.get().getServerId()))
                    {
                        // already on the server
                        World islandOnServer = this.skyIslandsModule.getIslandOnServer(islandId);

                        if(islandOnServer == null)
                        {
                            Messages.error(player, "Something went wrong! Please contact an admin.");
                            return;
                        }

                        player.teleport(islandOnServer.getSpawnLocation());
                        Messages.arrived(player, "Your Island");
                    }
                    else
                    {
                        // go to server
                        Messages.teleporting(player, "Your Island");
                        SkyblockLibrary.get().getApi().sendPlayerToIsland(player.getUniqueId(), islandId);
                    }
                })
                .register();
    }

    private static void saveIsland(Player sender, OfflinePlayer targetPlayer) {
        UUID targetIslandId = targetPlayer.getUniqueId();
        Sync.get().run(() -> {
            SkyblockLibrary.get().getSkyIslandsModule().saveAndUnloadIsland(targetIslandId);
            Messages.success(sender, "Island saved!");
        }).execute();
    }
}

