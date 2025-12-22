package dev.cobblesword.skyblock.library.modules.remoteworlds;

import dev.cobblesword.skyblock.library.common.messages.CC;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.cobblesword.skyblock.library.modules.remoteworlds.models.SoftWorld;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RemoteWorldsModule extends Module
{
    private WorldManager worldManager;

    public RemoteWorldsModule()
    {
        super("Remote Worlds");
        this.worldManager = new WorldManager();
    }

    @Override
    protected void onEnable()
    {
        registerCommands();
    }

    @Override
    protected void onDisable()
    {

    }

    private void registerCommands()
    {
        CommandAPICommand worldsCmd = new CommandAPICommand("worlds");

        CommandAPICommand loadSubCmd = new CommandAPICommand("load")
                .withAliases("l")
                .executes((sender, args) -> {

                });

        CommandAPICommand reloadSubCmd = new CommandAPICommand("reload")
                .withAliases("r")
                .executes((sender, args) -> {

                });

        CommandAPICommand unloadSubCmd = new CommandAPICommand("unload")
                .withAliases("u")
                .executes((sender, args) -> {

                });

        CommandAPICommand saveSubCmd = new CommandAPICommand("save")
                .withAliases("s")
                .executes((sender, args) -> {
                });

        CommandAPICommand listSubCmd = new CommandAPICommand("list")
                .withHelp("Lists worlds", "Lists all the current worlds in our system and if they are loaded on the current server")
                .withAliases("ls")
                .withOptionalArguments(new IntegerArgument("page"))
                .executes((sender, args) -> {
                    int page = 1;

                    if(args.count() > 0 && args.get(0) != null) {
                        page = (Integer) args.get(0);
                    }

                    List<SoftWorld> remoteWorlds = this.worldManager.getRemoteWorlds(page, 10);

                    sender.sendMessage("================[ " + page + " page ]================");
                    for (SoftWorld remoteWorld : remoteWorlds)
                    {
                        if (remoteWorld.isLoaded())
                        {
                            sender.sendMessage(CC.green + remoteWorld.getWorldId() + CC.gray + ":" + CC.green + remoteWorld.getWorldName());
                        }
                        else
                        {
                            sender.sendMessage(CC.white + remoteWorld.getWorldId() + CC.gray + ":" + CC.white + remoteWorld.getWorldName());
                        }
                    }
                });

        worldsCmd.withSubcommand(loadSubCmd);
        worldsCmd.withSubcommand(reloadSubCmd);
        worldsCmd.withSubcommand(unloadSubCmd);
        worldsCmd.withSubcommand(saveSubCmd);
        worldsCmd.withSubcommand(listSubCmd);

        worldsCmd.register();
    }

    public List<SoftWorld> getRemoteWorlds(int page, int limit)
    {
        return this.worldManager.getRemoteWorlds(page, limit);
    }

    public void loadWorld(String worldName, Consumer<World> worldLoadedCallback)
    {
        Sync.get().run(() -> {
            worldManager.loadOrCreateWorld(worldName);
            World world = Bukkit.getWorld(worldName);
            worldLoadedCallback.accept(world);
        }).execute();
    }

    public void save(String worldName)
    {
        this.worldManager.save(worldName);
    }

    public void saveAndUnloadWorld(String worldName)
    {
        this.worldManager.unloadAndSave(worldName);
    }
}
