package com.github.Syaaddd.omniRepair.repair;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.utils.NBTProtection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles repair for vanilla Minecraft items.
 * Uses standard durability system.
 */
public class VanillaRepair extends RepairHandler {

    private final NBTProtection nbtProtection;

    public VanillaRepair(OmniRepair plugin) {
        super(plugin);
        this.nbtProtection = plugin.getNBTProtection();
    }

    @Override
    public boolean canRepair(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        // Check if it's an MMOItem - if so, let MMOItemsRepair handle it
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()) {
            if (plugin.getMmoItemsHook().isMMOItem(item)) {
                return false;
            }
        }

        // Check if it's a durability item
        if (!plugin.getItemUtils().isDurabilityItem(item.getType())) {
            return false;
        }

        // Check if it has damage
        return plugin.getItemUtils().hasVanillaDamage(item);
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
            ItemStack result = nbtProtection.cloneSafely(item);

            // Copy custom enchantments from AdvancedEnchantments and other custom enchant plugins FIRST
            // This must be done BEFORE setting item meta to preserve NBT data
            if (plugin.getCustomEnchantHook() != null && plugin.getCustomEnchantHook().isEnabled()) {
                try {
                    boolean customEnchantsCopied = plugin.getCustomEnchantHook().copyCustomEnchantments(item, result);
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        if (customEnchantsCopied) {
                            plugin.getLogger().info("[DEBUG] Custom enchantments copied successfully");
                        } else {
                            plugin.getLogger().info("[DEBUG] No custom enchantments to copy");
                        }
                    }
                } catch (Exception e) {
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().warning("[DEBUG] Error copying custom enchantments: " + e.getMessage());
                    }
                }
            }

            // Set damage to 0 (fully repaired)
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(0);
                result.setItemMeta(meta);
            }

            // Verify NBT was preserved
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                nbtProtection.logVerification(original, result, "VanillaRepair");
            }

            return result;
        } catch (Exception e) {
            plugin.getLogger().warning("Error in VanillaRepair: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Repair an item without a player (for bulk repair).
     */
    public ItemStack repair(ItemStack item) {
        return repair(item, null);
    }
}
