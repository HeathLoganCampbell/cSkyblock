package dev.cobblesword.skyblock.library.modules;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Module
{
    private String name;

    public Module(String name)
    {
        this.name = name;
    }

    public void enable()
    {
        long start = System.currentTimeMillis();
        this.log("Enabling...");
        this.onEnable();
        this.log("Enabled in " + (System.currentTimeMillis() - start) + "ms");
    }

    public void disable()
    {
        long start = System.currentTimeMillis();
        this.log("Disabling...");
        this.onDisable();
        this.log("Disabled in " + (System.currentTimeMillis() - start) + "ms");
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    protected void log(String message)
    {
        System.out.println(this.name + " ] " + message);
    }

    public void registerListener(Listener listener)
    {
        Bukkit.getPluginManager().registerEvents(listener, SkyblockLibrary.get().getPlugin());
    }

    public void registerCommands(Class<?> clazz)
    {
        SkyblockLibrary.get().registerCommands(clazz);
    }

    public void registerCommands(ICommands commandInstance)
    {
        commandInstance.registerCommands();
    }
}
