package com.github.Syaaddd.omniRepair.integration;

import com.github.Syaaddd.omniRepair.OmniRepair;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Handles integration with Vault economy.
 * Provides soft-dependency support - safely degrades if Vault is not installed.
 */
public class VaultHook {

    private final OmniRepair plugin;
    private Economy economy = null;

    public VaultHook(OmniRepair plugin) {
        this.plugin = plugin;
        
        // Check if Vault is available
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                economy = plugin.getServer().getServicesManager().getRegistration(Economy.class)
                        .getProvider();
                
                if (economy != null) {
                    plugin.getLogger().info("✓ Vault economy found: " + economy.getName());
                } else {
                    plugin.getLogger().info("✗ No economy plugin found via Vault");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("✗ Error connecting to Vault: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("✗ Vault not found - economy features disabled");
        }
    }

    /**
     * Check if Vault economy is available and enabled.
     */
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("economy.enabled", true) 
                && economy != null 
                && plugin.getConfig().getBoolean("settings.use-economy", true);
    }

    /**
     * Check if a player has enough money.
     * @param player The player to check
     * @param amount The amount needed
     * @return true if player has enough, false otherwise
     */
    public boolean hasEnough(Player player, double amount) {
        if (!isEnabled() || economy == null) {
            return true; // Free if economy disabled
        }
        
        // Check for free repair permission
        if (player.hasPermission("omnirepair.free")) {
            return true;
        }
        
        return economy.has(player, amount);
    }

    /**
     * Withdraw money from a player.
     * @param player The player to withdraw from
     * @param amount The amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdraw(Player player, double amount) {
        if (!isEnabled() || economy == null) {
            return true; // Free if economy disabled
        }
        
        // Check for free repair permission
        if (player.hasPermission("omnirepair.free")) {
            return true;
        }
        
        if (!hasEnough(player, amount)) {
            return false;
        }
        
        economy.withdrawPlayer(player, amount);
        return true;
    }

    /**
     * Get a player's balance.
     * @param player The player
     * @return The player's balance
     */
    public double getBalance(Player player) {
        if (!isEnabled() || economy == null) {
            return Double.MAX_VALUE; // Infinite if economy disabled
        }
        
        return economy.getBalance(player);
    }

    /**
     * Get a player's balance (formatted string).
     * @param player The player
     * @return Formatted balance string
     */
    public String getBalanceFormatted(Player player) {
        if (!isEnabled() || economy == null) {
            return "∞";
        }
        
        return economy.format(getBalance(player));
    }

    /**
     * Format a currency amount.
     * @param amount The amount
     * @return Formatted string
     */
    public String format(double amount) {
        if (economy == null) {
            return "$" + String.format("%.2f", amount);
        }
        
        return economy.format(amount);
    }

    /**
     * Check if economy is enabled in config.
     */
    public boolean isEconomyEnabled() {
        return plugin.getConfig().getBoolean("economy.enabled", true);
    }

    /**
     * Check if economy should be used for repairs.
     */
    public boolean shouldUseEconomy() {
        return plugin.getConfig().getBoolean("settings.use-economy", true);
    }
}
