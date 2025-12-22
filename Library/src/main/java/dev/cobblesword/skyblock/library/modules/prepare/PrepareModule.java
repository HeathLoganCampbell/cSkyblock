package dev.cobblesword.skyblock.library.modules.prepare;

import dev.cobblesword.skyblock.library.modules.Module;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class PrepareModule extends Module implements Listener
{
    private HashMap<UUID, Consumer> taskMap = new HashMap<>();

    public PrepareModule()
    {
        super("Prepare");
    }

    @Override
    protected void onEnable() {
        this.registerListener(this);
    }

    @Override
    protected void onDisable() {

    }

    public void registerRunnable(UUID playerId, Consumer runnable)
    {
        this.taskMap.put(playerId, runnable);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        System.out.println("Player joinned");
        Consumer runnable = this.taskMap.get(e.getPlayer().getUniqueId());
        System.out.println("has consumer " + runnable);

        if(runnable == null) return;

        System.out.println("APPLYING");
        runnable.accept(e.getPlayer());
    }
}
