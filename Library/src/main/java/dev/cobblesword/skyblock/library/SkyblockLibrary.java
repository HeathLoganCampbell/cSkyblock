package dev.cobblesword.skyblock.library;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.exchange.impl.redis.RedisExchange;
import dev.cobblesword.exchange.impl.redis.RedisSettings;
import dev.cobblesword.skyblock.library.controller.ControllerClient;
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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

    @Getter
    private ControllerClient controller;

    private static final String CONTROLLER_URL = "http://localhost:8080";

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
        this.controller = new ControllerClient(CONTROLLER_URL);

        this.serverTrackerModule = new ServerTrackerModule();
        this.serverTrackerModule.enable();

        // Register with controller
        String serverId = getServerId();
        String serverType = getServerType().name().toLowerCase();
        controller.registerServer(serverId, serverType, "localhost", Bukkit.getPort())
            .thenRun(() -> System.out.println("✓ Registered with controller: " + serverId))
            .exceptionally(e -> { System.err.println("✗ Failed to register with controller: " + e.getMessage()); return null; });

        // Start heartbeat task with metrics
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Runtime runtime = Runtime.getRuntime();
            long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long memoryMax = runtime.maxMemory() / (1024 * 1024);
            double tps = getTps();
            double cpuUsage = getCpuUsage();
            
            // Get island metrics (worlds starting with "islands/")
            java.util.List<ControllerClient.IslandMetrics> islands = Bukkit.getWorlds().stream()
                .filter(w -> w.getName().startsWith("islands/"))
                .map(w -> new ControllerClient.IslandMetrics(
                    w.getName().replace("islands/", ""),
                    w.getLoadedChunks().length,
                    w.getEntities().size(),
                    countTileEntities(w),
                    w.getPlayers().size()
                ))
                .toList();
            
            // Run HTTP call async
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                controller.heartbeat(serverId, Bukkit.getOnlinePlayers().size(), 
                                    tps, memoryUsed, memoryMax, cpuUsage, islands);
            });
        }, 200L, 200L); // Every 10 seconds

        this.skyblockApi = new SkyblockApi(this.getMessageExchange(), serverId, false);

        RemoteWorldsModule remoteWorldsModule = new RemoteWorldsModule();
        remoteWorldsModule.enable();

        PrepareModule prepareModule = new PrepareModule();
        prepareModule.enable();

        skyIslandsModule = new SkyIslandsModule(prepareModule, remoteWorldsModule, this.exchange, this.controller);
        skyIslandsModule.enable();
        new PlayerSyncModule(this.database, this.exchange).enable();
        new ChatModule(this.exchange).enable();

        this.scoreboardModule = new ScoreboardModule();
        this.scoreboardModule.enable();
    }

    public void disable()
    {
        // Unregister from controller
        controller.unregisterServer(getServerId());
        
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

    /**
     * Count tile entities in a world
     */
    private int countTileEntities(org.bukkit.World world) {
        int count = 0;
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            count += chunk.getTileEntities().length;
        }
        return count;
    }

    /**
     * Get server TPS using reflection (Paper/Spigot)
     */
    private double getTps() {
        try {
            // Paper has Bukkit.getTPS()
            Object tpsArray = Bukkit.class.getMethod("getTPS").invoke(null);
            return ((double[]) tpsArray)[0]; // 1 minute average
        } catch (Exception e) {
            return 20.0; // Default to perfect TPS if unavailable
        }
    }

    /**
     * Get CPU usage estimate
     */
    private double getCpuUsage() {
        try {
            java.lang.management.OperatingSystemMXBean os = 
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean sunOs) {
                return sunOs.getProcessCpuLoad() * 100;
            }
            return os.getSystemLoadAverage() / os.getAvailableProcessors() * 100;
        } catch (Exception e) {
            return 0;
        }
    }
}
