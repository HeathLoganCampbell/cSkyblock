package dev.cobblesword.skyblock.library.modules.skyislands.commands;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import dev.cobblesword.skyblock.library.modules.ICommands;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.SkyIslandsModule;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IslandCommand implements ICommands {

    private final SkyIslandsModule skyIslandsModule;

    public IslandCommand(SkyIslandsModule skyIslandsModule) {
        this.skyIslandsModule = skyIslandsModule;
    }

    public void registerCommands() {
        new CommandAPICommand("island")
                .withAliases("is")
                .withSubcommand(
                        new CommandAPICommand("create")
                                .withPermission("island.create")
                                .executesPlayer((sender, args) -> {
                                    // Create = go to your own island
                                    skyIslandsModule.goToIsland(sender, sender.getUniqueId());
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("goto")
                                .withPermission("island.goto")
                                .withArguments(new PlayerArgument("targetPlayer"))
                                .executesPlayer((sender, args) -> {
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get(0);
                                    skyIslandsModule.goToIsland(sender, targetPlayer.getUniqueId());
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("leave")
                                .withPermission("island.leave")
                                .executesPlayer((sender, args) -> {
                                    skyIslandsModule.goToSpawn(sender);
                                })
                )
                .executesPlayer((player, args) -> {
                    // Default: go to your own island
                    if (!SkyblockLibrary.get().getServerTrackerModule().isServerTypeAvailable(ServerType.ISLANDS)) {
                        Messages.error(player, "Island servers are currently unavailable. Try again in a moment!");
                        return;
                    }

                    skyIslandsModule.goToIsland(player, player.getUniqueId());
                })
                .register();
    }
}
