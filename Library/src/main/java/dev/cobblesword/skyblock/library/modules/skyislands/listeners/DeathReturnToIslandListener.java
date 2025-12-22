package dev.cobblesword.skyblock.library.modules.skyislands.listeners;

import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.skyislands.SkyIslandsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathReturnToIslandListener implements Listener
{
    private SkyIslandsModule skyIslandsModule;

    public DeathReturnToIslandListener(SkyIslandsModule skyIslandsModule)
    {
        this.skyIslandsModule = skyIslandsModule;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
        Player player = e.getEntity();

        Sync.get().run(() -> this.skyIslandsModule.sendToIsland(player, player.getUniqueId())).delay(4).execute();
    }
}
