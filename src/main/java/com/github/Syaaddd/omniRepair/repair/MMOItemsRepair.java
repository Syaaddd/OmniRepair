package com.github.Syaaddd.omniRepair.repair;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.utils.LoreUpdater;
import com.github.Syaaddd.omniRepair.utils.NBTProtection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Handles repair for MMOItems custom items.
 * Uses MMOItems API for durability management.
 */
public class MMOItemsRepair extends RepairHandler {

    private final NBTProtection nbtProtection;
    private final LoreUpdater loreUpdater;

    public MMOItemsRepair(OmniRepair plugin) {
        super(plugin);
        this.nbtProtection = plugin.getNBTProtection();
        this.loreUpdater = plugin.getLoreUpdater();
    }

    @Override
    public boolean canRepair(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        // Check if MMOItems hook is available
        if (plugin.getMmoItemsHook() == null || !plugin.getMmoItemsHook().isEnabled()) {
            return false;
        }

        // Check if it's an MMOItem
        if (!plugin.getMmoItemsHook().isMMOItem(item)) {
            return false;
        }

        // Check if it's damaged
        if (!plugin.getMmoItemsHook().isDamaged(item)) {
            return false;
        }

        // Check blacklist
        String mmoId = net.Indyuce.mmoitems.MMOItems.getID(item);
        if (mmoId != null && plugin.getMmoItemsHook().isBlacklisted(mmoId)) {
            return false;
        }

        return true;
    }

    @Override
    public double getRepairCost(ItemStack item) {
        if (!canRepair(item)) {
            return 0;
        }

        double damagePercent = getDamagePercent(item);
        if (damagePercent < 0) {
            return 0;
        }

        double baseCost = damagePercent * getCostPerPercent();
        
        // Apply MMOItems cost multiplier
        baseCost = applyMMOCostMultiplier(baseCost);
        
        // Apply minimum cost
        baseCost = Math.max(baseCost, getMinCost());

        // Check max cost limit
        if (exceedsMaxCost(baseCost)) {
            return plugin.getConfig().getDouble("settings.max-cost", 5000.0);
        }

        return baseCost;
    }

    @Override
    public ItemStack repair(ItemStack item, Player player) {
        if (!canRepair(item)) {
            return null;
        }

        try {
            // Clone the item to preserve NBT
            ItemStack original = nbtProtection.cloneSafely(item);

            // Use MMOItems API to repair
            boolean success = plugin.getMmoItemsHook().repair(item);

            if (!success) {
                plugin.getLogger().warning("MMOItems API repair failed, item may not be fully repaired");
                return null;
            }

            // Update lore if enabled
            if (plugin.getConfig().getBoolean("mmoitems.sync-lore", true)) {
                double maxDurability = plugin.getMmoItemsHook().getMaxDurability(item);
                item = loreUpdater.updateDurabilityLore(item, maxDurability, maxDurability);
            }

            // Verify NBT was preserved
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                nbtProtection.logVerification(original, item, "MMOItemsRepair");
            }

            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Error in MMOItemsRepair: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the MMOItems ID of an item.
     */
    public String getMMOItemId(ItemStack item) {
        if (!canRepair(item)) {
            return null;
        }
        return net.Indyuce.mmoitems.MMOItems.getID(item);
    }

    /**
     * Get the MMOItems type of an item.
     */
    public String getMMOItemType(ItemStack item) {
        if (!canRepair(item)) {
            return null;
        }
        // Convert Type object to String
        Object type = net.Indyuce.mmoitems.MMOItems.getType(item);
        return type != null ? type.toString() : null;
    }

    /**
     * Repair an item without a player (for bulk repair).
     */
    public ItemStack repair(ItemStack item) {
        return repair(item, null);
    }
}
