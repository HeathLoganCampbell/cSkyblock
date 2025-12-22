package dev.cobblesword.skyblock.library.modules.playersync;

import dev.cobblesword.exchange.api.ExchangeListener;
import dev.cobblesword.exchange.api.ExchangeMessage;
import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.database.Database;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.cobblesword.skyblock.library.modules.playersync.database.PlayerSyncRepository;
import dev.cobblesword.skyblock.library.modules.playersync.listeners.PlayerSyncListener;
import dev.cobblesword.skyblock.protocol.requests.RePullInventoryRequest;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerSyncModule extends Module
{
    private PlayerSyncRepository repository;

    private IExchange exchange;

    public PlayerSyncModule(Database database, IExchange exchange)
    {
        super("PlayerSync");
        this.repository = new PlayerSyncRepository(database);
        this.exchange = exchange;
    }

    @Override
    protected void onEnable() {
        this.registerListener(new PlayerSyncListener( this.repository, this.exchange, this));

        this.exchange.subscribe(new ExchangeListener() {
            @Override
            public void onEvent(ExchangeMessage exchangeMessage)
            {
                if (exchangeMessage instanceof RePullInventoryRequest rePullInventoryRequest)
                {
                    System.out.println("Recieved RePull message");
                    String sourceServerId = rePullInventoryRequest.getServerId();
                    if(sourceServerId.equalsIgnoreCase(SkyblockLibrary.get().getServerId()))
                    {
                        return;
                    }

                    Player player = Bukkit.getPlayer(rePullInventoryRequest.getPlayerId());
                    if(player == null || !player.isOnline())
                    {
                        System.out.println("PLAYER NOT ONLINE");
                        return;
                    }

                    applyPlayerSync(player);
                }
            }
        });

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(SkyblockLibrary.get().getPlugin(), () -> {
            long start = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers())
            {
                this.storePlayerSync(player);
            }

            System.out.println("Updated all players sync data in " + (System.currentTimeMillis() - start) + " ms");
        }, 20L * 60L, 20L  * 60L);
    }

    @Override
    protected void onDisable() {

    }

    public void storePlayerSync(Player player)
    {
        String invBase64 = InventoryShareUtil.inventoryToBase64(player);

        String gamemode = player.getGameMode().name(); // Get the name of the GameMode enum
        double health = player.getHealth();
        int foodLevel = player.getFoodLevel();
        float saturation = player.getSaturation();
        float exp = player.getExp();
        int level = player.getLevel();
        int fireTicks = player.getFireTicks();
        int inventorySlot = player.getInventory().getHeldItemSlot();

        this.repository.savePlayerData(player.getUniqueId(), invBase64, inventorySlot, gamemode, health, foodLevel, saturation, exp, level, fireTicks);
    }

    public void applyPlayerSync(Player player)
    {
        player.getInventory().clear();
        PlayerSyncRepository.PlayerData playerData = repository.getPlayerData(player.getUniqueId());

        if (playerData == null) {
            // First time player - no sync needed
            return;
        }

        String invBase64 = playerData.getInventoryBase64();
        InventoryShareUtil.inventoryFromBase64(player, invBase64);

        GameMode gameMode = GameMode.SURVIVAL;
        try
        {
             gameMode = GameMode.valueOf(playerData.getGamemode()); // Convert string to GameMode
        }
        catch (Exception ignored) {}

        player.setGameMode(gameMode);
        player.setHealth(playerData.getHealth());
        player.setFoodLevel(playerData.getFoodLevel());
        player.setSaturation((float) playerData.getSaturation());
        player.setExp((float) playerData.getExp());
        player.setLevel(playerData.getLevel());
        player.setFireTicks(playerData.getFireTicks());
        player.getInventory().setHeldItemSlot(playerData.getInventorySelectedSlot());
        
        // Silent sync - no message needed, player expects their inventory
    }

    public void triggerRePull(UUID playerId)
    {
        RePullInventoryRequest rePullInventoryRequest = new RePullInventoryRequest();
        rePullInventoryRequest.setServerId(SkyblockLibrary.get().getServerId());
        rePullInventoryRequest.setPlayerId(playerId);
        this.exchange.publishMessage(rePullInventoryRequest);
    }
}
