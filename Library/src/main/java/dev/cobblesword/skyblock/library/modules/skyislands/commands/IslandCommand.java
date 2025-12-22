package dev.cobblesword.skyblock.library.modules.skyislands.commands;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.CC;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.ICommands;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.SkyIslandsModule;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
                                    SkyblockLibrary.get().getApi().createIsland(sender.getUniqueId());
                                    sender.sendMessage(ChatColor.GREEN + "Creating island");
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("goto")
                                .withPermission("island.goto")
                                .withArguments(new PlayerArgument("targetPlayer"))
                                .executesPlayer((sender, args) -> {
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get(0);
                                    UUID targetPlayerId = targetPlayer.getUniqueId();
                                    SkyblockLibrary.get().getApi().sendPlayerToIsland(sender.getUniqueId(), targetPlayerId);
                                    sender.sendMessage(ChatColor.GREEN + "Teleported to island!");
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("leave")
                                .withPermission("island.leave")
                                .executesPlayer((sender, args) -> {
                                    saveIsland(sender, sender);
                                })
                                .withArguments(new PlayerArgument("targetPlayer"))
                                .executesPlayer((sender, args) -> {
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get(0);
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
                                    SkyblockLibrary.get().getSkyIslandsModule().saveAndUnloadIsland(targetIslandId);
                                    sender.sendMessage(ChatColor.GREEN + "Saving " + targetIslandId);
                                })
                )
                .executesPlayer((player, args) -> {
                    if (!SkyblockLibrary.get().getServerTrackerModule().isServerTypeAvailable(ServerType.ISLANDS))
                    {
                        player.sendMessage(CC.error("SkyIslands", "Looks like islands are unAvailable at the moment, please try again in a minute"));
                        return;
                    }

                    UUID islandId = player.getUniqueId();
                    String islandServerId = this.skyIslandsModule.getIslandsServer(islandId);

                    System.out.println("]]]]]]]] " + islandServerId);
                    if(islandServerId == null)
                    {
                        // Need to load island
                        SkyblockLibrary.get().getApi().createIsland(player.getUniqueId());
                        player.sendMessage(CC.success("SkyIslands", "Going to island..."));
                    }
                    else if(islandServerId.equalsIgnoreCase(SkyblockLibrary.get().getServerId()))
                    {
                        // already on the server
                        World islandOnServer = this.skyIslandsModule.getIslandOnServer(islandId);

                        if(islandOnServer == null)
                        {
                            player.sendMessage(CC.error("SkyIslands", "Sorry! there has been an issue, please contact an admin."));
                            return;
                        }

                        player.teleport(islandOnServer.getSpawnLocation());
                    }
                    else
                    {
                        // got to server
                        SkyblockLibrary.get().getApi().sendPlayerToIsland(player.getUniqueId(), islandId);
                    }
                })
                .register();
    }

    private static void saveIsland(Player sender, OfflinePlayer targetPlayer) {
        UUID targetIslandId = targetPlayer.getUniqueId();
        Sync.get().run(() ->  SkyblockLibrary.get().getSkyIslandsModule().saveAndUnloadIsland(targetIslandId)).execute();
    }
}

