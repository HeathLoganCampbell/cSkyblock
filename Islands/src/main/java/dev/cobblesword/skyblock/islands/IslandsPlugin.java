package dev.cobblesword.skyblock.islands;

import dev.cobblesword.skyblock.library.modules.skyislands.commands.IslandCommand;
import dev.cobblesword.skyblock.library.modules.prepare.PrepareModule;
import dev.cobblesword.skyblock.library.modules.skyislands.models.Island;
import dev.cobblesword.skyblock.library.common.messages.CC;
import dev.cobblesword.skyblock.library.modules.remoteworlds.RemoteWorldsModule;
import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.database.Database;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class IslandsPlugin extends JavaPlugin
{
    public static IslandsPlugin Instance;

    private SkyblockLibrary skyblockLibrary;

    @Override
    public void onLoad()
    {
        this.skyblockLibrary = new SkyblockLibrary();
        this.skyblockLibrary.load(this);
    }

    @Override
    public void onEnable()
    {
        this.skyblockLibrary.enable(this);

        Instance = this;
    }

    @Override
    public void onDisable()
    {
        this.skyblockLibrary.disable();

        Instance = null;
    }
}
