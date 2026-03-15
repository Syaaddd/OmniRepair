package com.github.Syaaddd.omniRepair.repair;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Abstract base class for all repair handlers.
 * Defines the contract for repairing items.
 */
public abstract class RepairHandler {

    protected final OmniRepair plugin;

    public RepairHandler(OmniRepair plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if this handler can repair the given item.
     */
    public abstract boolean canRepair(ItemStack item);

    /**
     * Calculate the repair cost for an item.
     * @param item The item to calculate cost for
     * @return The cost amount
     */
    public abstract double getRepairCost(ItemStack item);

    /**
     * Perform the repair operation.
     * @param item The item to repair
     * @param player The player performing the repair
     * @return The repaired item, or null if repair failed
     */
    public abstract ItemStack repair(ItemStack item, Player player);

    /**
     * Get the damage percentage of an item.
     */
    public double getDamagePercent(ItemStack item) {
        return plugin.getItemUtils().getDamagePercent(item);
    }

    /**
     * Get the current durability of an item.
     */
    public double getCurrentDurability(ItemStack item) {
        return plugin.getItemUtils().getCurrentDurability(item);
    }

    /**
     * Get the max durability of an item.
     */
    public double getMaxDurability(ItemStack item) {
        return plugin.getItemUtils().getMaxDurability(item);
    }

    /**
     * Check if the cost exceeds the maximum allowed.
     */
    protected boolean exceedsMaxCost(double cost) {
        double maxCost = plugin.getConfig().getDouble("settings.max-cost", 5000.0);
        return cost > maxCost;
    }

    /**
     * Apply the cost multiplier for MMOItems.
     */
    protected double applyMMOCostMultiplier(double baseCost) {
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()) {
            double multiplier = plugin.getConfig().getDouble("mmoitems.custom-cost-multiplier", 1.5);
            return baseCost * multiplier;
        }
        return baseCost;
    }

    /**
     * Get the minimum cost from config.
     */
    protected double getMinCost() {
        return plugin.getConfig().getDouble("settings.min-cost", 5.0);
    }

    /**
     * Get the cost per percent from config.
     */
    protected double getCostPerPercent() {
        return plugin.getConfig().getDouble("settings.cost-per-percent", 10.0);
    }
}
