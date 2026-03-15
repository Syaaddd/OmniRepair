package com.github.Syaaddd.omniRepair.economy;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all economy-related operations for repair.
 * Supports Vault economy, item cost, and XP cost.
 */
public class EconomyHandler {

    private final OmniRepair plugin;

    public EconomyHandler(OmniRepair plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a player can afford a repair.
     * 
     * @param player The player
     * @param cost The cost amount
     * @return true if player can afford, false otherwise
     */
    public boolean canAfford(Player player, double cost) {
        if (cost <= 0) {
            return true; // Free repair
        }

        // Check for free repair permission
        if (player.hasPermission("omnirepair.free")) {
            return true;
        }

        // Determine payment method
        if (plugin.getConfig().getBoolean("economy.use-xp-cost", false)) {
            return canAffordXP(player, cost);
        } else if (plugin.getConfig().getBoolean("economy.use-item-cost", false)) {
            return canAffordItems(player, cost);
        } else {
            return canAffordMoney(player, cost);
        }
    }

    /**
     * Check if player can afford with money.
     */
    private boolean canAffordMoney(Player player, double cost) {
        if (!plugin.getVaultHook().isEnabled()) {
            return true; // Free if economy disabled
        }
        return plugin.getVaultHook().hasEnough(player, cost);
    }

    /**
     * Check if player can afford with XP levels.
     */
    private boolean canAffordXP(Player player, double cost) {
        int xpCost = (int) Math.ceil(cost);
        return player.getLevel() >= xpCost;
    }

    /**
     * Check if player can afford with items.
     */
    private boolean canAffordItems(Player player, double cost) {
        String materialName = plugin.getConfig().getString("economy.cost-item.material", "IRON_INGOT");
        double amountPerPercent = plugin.getConfig().getDouble("economy.cost-item.amount-per-percent", 0.01);
        
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            material = Material.IRON_INGOT;
        }

        int requiredAmount = (int) Math.ceil(cost * amountPerPercent * 100);
        
        int playerAmount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                playerAmount += item.getAmount();
            }
        }

        return playerAmount >= requiredAmount;
    }

    /**
     * Withdraw payment from a player.
     * 
     * @param player The player
     * @param cost The cost amount
     * @return true if successful, false otherwise
     */
    public boolean withdraw(Player player, double cost) {
        if (cost <= 0) {
            return true; // Free repair
        }

        // Check for free repair permission
        if (player.hasPermission("omnirepair.free")) {
            return true;
        }

        // Determine payment method
        if (plugin.getConfig().getBoolean("economy.use-xp-cost", false)) {
            return withdrawXP(player, cost);
        } else if (plugin.getConfig().getBoolean("economy.use-item-cost", false)) {
            return withdrawItems(player, cost);
        } else {
            return withdrawMoney(player, cost);
        }
    }

    /**
     * Withdraw money from player.
     */
    private boolean withdrawMoney(Player player, double cost) {
        if (!plugin.getVaultHook().isEnabled()) {
            return true; // Free if economy disabled
        }
        return plugin.getVaultHook().withdraw(player, cost);
    }

    /**
     * Withdraw XP levels from player.
     */
    private boolean withdrawXP(Player player, double cost) {
        int xpCost = (int) Math.ceil(cost);
        if (player.getLevel() < xpCost) {
            return false;
        }
        player.setLevel(player.getLevel() - xpCost);
        return true;
    }

    /**
     * Withdraw items from player.
     */
    private boolean withdrawItems(Player player, double cost) {
        String materialName = plugin.getConfig().getString("economy.cost-item.material", "IRON_INGOT");
        double amountPerPercent = plugin.getConfig().getDouble("economy.cost-item.amount-per-percent", 0.01);
        
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            material = Material.IRON_INGOT;
        }

        int requiredAmount = (int) Math.ceil(cost * amountPerPercent * 100);
        
        // Remove items from inventory
        int removed = 0;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (removed >= requiredAmount) {
                break;
            }
            
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) {
                int take = Math.min(requiredAmount - removed, item.getAmount());
                item.setAmount(item.getAmount() - take);
                removed += take;
                
                if (item.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                }
            }
        }

        return removed >= requiredAmount;
    }

    /**
     * Get the formatted cost string.
     */
    public String getCostString(double cost) {
        if (cost <= 0) {
            return "Free";
        }

        if (plugin.getConfig().getBoolean("economy.use-xp-cost", false)) {
            return (int) Math.ceil(cost) + " XP Levels";
        } else if (plugin.getConfig().getBoolean("economy.use-item-cost", false)) {
            String materialName = plugin.getConfig().getString("economy.cost-item.material", "IRON_INGOT");
            double amountPerPercent = plugin.getConfig().getDouble("economy.cost-item.amount-per-percent", 0.01);
            int amount = (int) Math.ceil(cost * amountPerPercent * 100);
            return amount + "x " + formatMaterialName(materialName);
        } else {
            return plugin.getVaultHook().format(cost);
        }
    }

    /**
     * Format material name for display.
     */
    private String formatMaterialName(String materialName) {
        return materialName.replace("_", " ").toLowerCase();
    }

    /**
     * Get the required items for a repair cost.
     */
    public Map<Material, Integer> getRequiredItems(double cost) {
        Map<Material, Integer> required = new HashMap<>();
        
        if (!plugin.getConfig().getBoolean("economy.use-item-cost", false)) {
            return required;
        }

        String materialName = plugin.getConfig().getString("economy.cost-item.material", "IRON_INGOT");
        double amountPerPercent = plugin.getConfig().getDouble("economy.cost-item.amount-per-percent", 0.01);
        
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            material = Material.IRON_INGOT;
        }

        int amount = (int) Math.ceil(cost * amountPerPercent * 100);
        required.put(material, amount);

        return required;
    }

    /**
     * Check if economy is being used (vs free repair).
     */
    public boolean isUsingEconomy() {
        return plugin.getConfig().getBoolean("settings.use-economy", true)
                && (plugin.getVaultHook().isEnabled()
                    || plugin.getConfig().getBoolean("economy.use-xp-cost", false)
                    || plugin.getConfig().getBoolean("economy.use-item-cost", false));
    }

    /**
     * Get the payment method currently in use.
     */
    public PaymentMethod getPaymentMethod() {
        if (plugin.getConfig().getBoolean("economy.use-xp-cost", false)) {
            return PaymentMethod.XP;
        } else if (plugin.getConfig().getBoolean("economy.use-item-cost", false)) {
            return PaymentMethod.ITEMS;
        } else if (plugin.getVaultHook().isEnabled()) {
            return PaymentMethod.MONEY;
        } else {
            return PaymentMethod.FREE;
        }
    }

    public enum PaymentMethod {
        MONEY, XP, ITEMS, FREE
    }
}
