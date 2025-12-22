package dev.cobblesword.skyblock.library.modules.chat;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.cobblesword.skyblock.protocol.requests.ChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatModule extends Module implements Listener
{
    private IExchange exchange;

    public ChatModule(IExchange exchange)
    {
        super("Chat");
        this.exchange = exchange;
    }

    @Override
    protected void onEnable()
    {
        this.registerListener(this);

        this.exchange.subscribe((message) -> {
            if(message instanceof ChatMessage chatMessage)
            {
                String sourceServer = chatMessage.getServerId();
                boolean isSelf = sourceServer.equalsIgnoreCase(SkyblockLibrary.get().getServerId());

                if (isSelf)
                {
                    return;
                }

                Bukkit.broadcastMessage(chatMessage.getMessage());
            }
        });
    }

    @Override
    protected void onDisable() {

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e)
    {
        Player player = e.getPlayer();
        String displayName = player.getDisplayName();
        String chatMessage = e.getMessage();
        String format = e.getFormat();

        String message = String.format(format, displayName, chatMessage);

        ChatMessage chatMessagePacket = new ChatMessage();
        chatMessagePacket.setServerId(SkyblockLibrary.get().getServerId());
        chatMessagePacket.setMessage(message);
        this.exchange.publishMessage(chatMessagePacket);
    }
}
