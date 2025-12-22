package dev.cobblesword.skyblock.spawn;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.saga.SkyblockApi;
import dev.cobblesword.skyblock.spawn.listeners.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnPlugin extends JavaPlugin
{
    private static SpawnPlugin INSTANCE;

    private SkyblockLibrary skyblockLibrary;

    private SkyblockApi skyblockApi;

    @Override
    public void onLoad()
    {
        skyblockLibrary = new SkyblockLibrary();
        skyblockLibrary.load(this);
    }

    @Override
    public void onEnable()
    {
        skyblockLibrary.enable(this);
        INSTANCE = this;

        this.skyblockApi = new SkyblockApi(skyblockLibrary.getMessageExchange(), "Spawn-01", false);

        Bukkit.getPluginManager().registerEvents(new ChatListener(skyblockApi), this);
    }

    @Override
    public void onDisable()
    {
        skyblockLibrary.disable();

        INSTANCE = null;
    }

    public static SpawnPlugin getInstance()
    {
        return INSTANCE;
    }

    public SkyblockApi getApi()
    {
        return skyblockApi;
    }
}
