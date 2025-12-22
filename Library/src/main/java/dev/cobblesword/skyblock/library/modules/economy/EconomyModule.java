package dev.cobblesword.skyblock.library.modules.economy;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.modules.Module;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyModule extends Module
{
    private static Economy econ = null;

    public EconomyModule() {
        super("Economy");
    }

    @Override
    protected void onEnable() {
        setupEconomy();
    }

    @Override
    protected void onDisable() {

    }

    private boolean setupEconomy() {
        Server server = SkyblockLibrary.get().getPlugin().getServer();

        Plugin vaultPlugin = server.getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean deposit(Player player, double amount)
    {
        EconomyResponse response = econ.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean withdraw(OfflinePlayer offlinePlayer, double amount)
    {
        EconomyResponse economyResponse = econ.withdrawPlayer(offlinePlayer, amount);
        return economyResponse.transactionSuccess();
    }

    public double getBalance(OfflinePlayer offlinePlayer)
    {
        return econ.getBalance(offlinePlayer);
    }
}
