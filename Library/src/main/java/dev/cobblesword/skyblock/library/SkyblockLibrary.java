package dev.cobblesword.skyblock.library;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.exchange.impl.redis.RedisExchange;
import dev.cobblesword.exchange.impl.redis.RedisSettings;
import dev.cobblesword.exchange.protocols.KeepAlive;
import dev.cobblesword.skyblock.library.database.Database;
import dev.cobblesword.skyblock.library.modules.chat.ChatModule;
import dev.cobblesword.skyblock.library.modules.playersync.PlayerSyncModule;
import dev.cobblesword.skyblock.library.modules.prepare.PrepareModule;
import dev.cobblesword.skyblock.library.modules.remoteworlds.RemoteWorldsModule;
import dev.cobblesword.skyblock.library.modules.scoreboard.ScoreboardModule;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerTrackerModule;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.SkyIslandsModule;
import dev.cobblesword.skyblock.saga.SkyblockApi;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class SkyblockLibrary
{
    private static SkyblockLibrary skyblockLibrary;

    @Getter
    private JavaPlugin plugin;

    private IExchange exchange;

    @Getter
    private Database database;

    private SkyblockApi skyblockApi;

    @Getter
    private ServerTrackerModule serverTrackerModule;

    @Getter
    private SkyIslandsModule skyIslandsModule;

    @Getter
    private ScoreboardModule scoreboardModule;

    public static SkyblockLibrary get()
    {
        return skyblockLibrary;
    }

    public SkyblockApi getApi()
    {
        return this.skyblockApi;
    }


    public IExchange getMessageExchange()
    {
        return this.exchange;
    }

    private IExchange getExchange() {
        RedisSettings redisSettings = new RedisSettings("localhost");
        return new RedisExchange(redisSettings);
    }

    public void registerCommands(Class<?> clazz)
    {
        CommandAPI.registerCommand(clazz);
    }

    public void load(JavaPlugin plugin)
    {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).silentLogs(true));
    }

    public void enable(JavaPlugin plugin)
    {
        skyblockLibrary = this;
        this.plugin = plugin;

        CommandAPI.onEnable();

        this.database = new Database();
        this.exchange = getExchange();

        this.serverTrackerModule = new ServerTrackerModule();
        this.serverTrackerModule.enable();

        this.skyblockApi = new SkyblockApi(this.getMessageExchange(), skyblockLibrary.getServerId(), false);

        RemoteWorldsModule remoteWorldsModule = new RemoteWorldsModule();
        remoteWorldsModule.enable();

        PrepareModule prepareModule = new PrepareModule();
        prepareModule.enable();

        skyIslandsModule = new SkyIslandsModule(prepareModule, remoteWorldsModule, this.exchange);
        skyIslandsModule.enable();
        new PlayerSyncModule(this.database, this.exchange).enable();
        new ChatModule(this.exchange).enable();

        this.scoreboardModule = new ScoreboardModule();
        this.scoreboardModule.enable();
    }

    public void disable()
    {
        skyIslandsModule.disable();

        exchange.stop();
    }

    public String getServerId()
    {
        return this.serverTrackerModule.getServerId();
    }

    public ServerType getServerType()
    {
        return this.serverTrackerModule.getServerType();
    }
}
