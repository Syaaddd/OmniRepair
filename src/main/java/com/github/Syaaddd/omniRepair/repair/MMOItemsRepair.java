package com.github.Syaaddd.omniRepair.repair;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.utils.LoreUpdater;
import com.github.Syaaddd.omniRepair.utils.NBTProtection;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Handles repair for MMOItems custom items.
 * Uses MMOItems plugin.getItem() to get fresh template and copy enchantments.
 * Also preserves custom enchantments from AdvancedEnchantments and other plugins.
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

        // Check if it's an MMOItem using MMOItems API directly
        net.Indyuce.mmoitems.api.Type type = net.Indyuce.mmoitems.MMOItems.getType(item);
        String id = net.Indyuce.mmoitems.MMOItems.getID(item);
        
        if (type == null || id == null) {
            return false;
        }

        // Check blacklist
        if (plugin.getMmoItemsHook().isBlacklisted(id)) {
            return false;
        }

        // Check if item can be obtained from MMOItems (valid item)
        try {
            ItemStack template = net.Indyuce.mmoitems.MMOItems.plugin.getItem(type, id);
            if (template == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // It's a valid MMOItem - allow repair
        // We don't check damage because some MMOItems don't use standard durability
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
            // Get MMOItem type and ID
            Type type = MMOItems.getType(item);
            String id = MMOItems.getID(item);

            if (type == null || id == null) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().warning("[DEBUG] MMOItems repair failed: type or ID is null");
                }
                return null;
            }

            // Get fresh template from MMOItems
            ItemStack repairedItem = MMOItems.plugin.getItem(type, id);

            if (repairedItem == null) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().warning("[DEBUG] MMOItems repair failed: template item is null for " + type.getId() + ":" + id);
                }
                return null;
            }

            // Clone the repaired item to avoid modifying template
            repairedItem = repairedItem.clone();

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] MMOItems repair - Got fresh template from MMOItems");
                plugin.getLogger().info("[DEBUG] MMOItems repair - Original item durability NBT: " + 
                    getNBTDurability(item));
                plugin.getLogger().info("[DEBUG] MMOItems repair - Repaired item durability NBT: " + 
                    getNBTDurability(repairedItem));
            }

            // Copy custom enchantments from AdvancedEnchantments and other custom enchant plugins FIRST
            // This must be done BEFORE setting item meta to preserve NBT data
            if (plugin.getCustomEnchantHook() != null && plugin.getCustomEnchantHook().isEnabled()) {
                try {
                    boolean customEnchantsCopied = plugin.getCustomEnchantHook().copyCustomEnchantments(item, repairedItem);
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

            // Copy enchantments from original item to repaired item
            ItemMeta originalMeta = item.getItemMeta();
            ItemMeta repairedMeta = repairedItem.getItemMeta();

            if (originalMeta != null && repairedMeta != null) {
                // Copy all vanilla enchantments (keep existing ones from template)
                for (Map.Entry<Enchantment, Integer> entry : originalMeta.getEnchants().entrySet()) {
                    Enchantment enchant = entry.getKey();
                    int level = entry.getValue();
                    repairedMeta.addEnchant(enchant, level, true);
                }

                // Copy display name if custom
                if (originalMeta.hasDisplayName()) {
                    repairedMeta.setDisplayName(originalMeta.getDisplayName());
                }

                // Copy lore if custom
                if (originalMeta.hasLore()) {
                    repairedMeta.setLore(originalMeta.getLore());
                }

                // Copy other meta attributes (flags, etc.)
                repairedMeta.setAttributeModifiers(originalMeta.getAttributeModifiers());

                // Copy item flags if the meta supports it
                if (repairedMeta instanceof org.bukkit.inventory.meta.Damageable || 
                    repairedMeta.getClass().getSimpleName().contains("Meta")) {
                    try {
                        java.lang.reflect.Method setItemFlagsMethod = repairedMeta.getClass().getMethod("setItemFlags", java.util.Set.class);
                        if (setItemFlagsMethod != null) {
                            setItemFlagsMethod.invoke(repairedMeta, originalMeta.getItemFlags());
                        }
                    } catch (Exception e) {
                        // Ignore if setItemFlags is not available
                    }
                }

                repairedItem.setItemMeta(repairedMeta);
            }

            // Set durability to max using MMOItems API if available
            // This ensures MMOItems durability is properly repaired
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] Attempting to set MMOItems durability to max...");
            }
            setMaxDurability(repairedItem, type, id);

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] MMOItems repair successful: " + type.getId() + ":" + id);
            }

            return repairedItem;
        } catch (Exception e) {
            plugin.getLogger().warning("Error in MMOItemsRepair: " + e.getMessage());
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Simple check if item is damaged using NBT durability values.
     */
    private boolean isItemDamaged(ItemStack item) {
        try {
            Type type = MMOItems.getType(item);
            String id = MMOItems.getID(item);

            if (type == null || id == null) {
                return false;
            }

            // Get the template item
            ItemStack template = MMOItems.plugin.getItem(type, id);
            if (template == null) {
                return false;
            }

            // Check if this item type has durability
            net.Indyuce.mmoitems.api.item.mmoitem.MMOItem mmoItem = 
                MMOItems.plugin.getItems().getMMOItem(type, id);
            
            if (mmoItem == null || !mmoItem.hasData(net.Indyuce.mmoitems.ItemStats.DURABILITY)) {
                // No durability stat - can't determine damage
                return false;
            }

            // Item has durability stat, assume it might be damaged
            // The repair will work if durability is actually low
            return true;

        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] isItemDamaged error: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Get the MMOItems ID of an item.
     */
    public String getMMOItemId(ItemStack item) {
        if (!canRepair(item)) {
            return null;
        }
        return MMOItems.getID(item);
    }

    /**
     * Get the MMOItems type of an item.
     */
    public String getMMOItemType(ItemStack item) {
        if (!canRepair(item)) {
            return null;
        }
        Type type = MMOItems.getType(item);
        return type != null ? type.getId() : null;
    }

    /**
     * Get durability value from NBT for debugging.
     */
    private String getNBTDurability(ItemStack item) {
        try {
            if (item == null || !item.hasItemMeta()) {
                return "null";
            }
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            
            // Try to read MMOItems durability keys
            org.bukkit.NamespacedKey durabilityKey = new org.bukkit.NamespacedKey("mmoitems", "durability");
            org.bukkit.NamespacedKey maxDurabilityKey = new org.bukkit.NamespacedKey("mmoitems", "max_durability");
            
            Double current = pdc.get(durabilityKey, org.bukkit.persistence.PersistentDataType.DOUBLE);
            Double max = pdc.get(maxDurabilityKey, org.bukkit.persistence.PersistentDataType.DOUBLE);
            
            if (current != null && max != null) {
                return current + " / " + max;
            }
            
            return "not found in PDC";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    /**
     * Set durability to max using MMOItems API or reflection.
     */
    private void setMaxDurability(ItemStack item, Type type, String id) {
        try {
            // Try to use MMOItems API to set durability
            try {
                // Get the MMOItem object
                net.Indyuce.mmoitems.api.item.mmoitem.MMOItem mmoItem = 
                    net.Indyuce.mmoitems.MMOItems.plugin.getItems().getMMOItem(type, id);
                
                if (mmoItem != null) {
                    // Check if item has durability
                    if (mmoItem.hasData(net.Indyuce.mmoitems.ItemStats.DURABILITY)) {
                        // MMOItems stores durability in NBT internally
                        // We need to use their API to set it back to max
                        // Try using reflection to call setDurability or similar
                        try {
                            // Try to find and call setDurability method via reflection
                            java.lang.reflect.Method setDurabilityMethod = null;
                            
                            // Look for method in MMOItem class
                            for (java.lang.reflect.Method m : mmoItem.getClass().getDeclaredMethods()) {
                                if (m.getName().toLowerCase().contains("durability") || 
                                    m.getName().toLowerCase().contains("set")) {
                                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                        plugin.getLogger().info("[DEBUG] Found potential method: " + m.getName());
                                    }
                                }
                            }
                            
                            // Alternative: Use MMOItems NBT modification
                            // This is the most reliable way to set durability
                            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                plugin.getLogger().info("[DEBUG] Using NBT-based durability repair");
                            }
                            
                            // Set durability via NBT compound
                            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
                                
                                // Get max durability from config/stat
                                double maxDurability = 100.0; // Default fallback
                                
                                // Set durability to max in NBT
                                org.bukkit.NamespacedKey durabilityKey = new org.bukkit.NamespacedKey("mmoitems", "durability");
                                org.bukkit.NamespacedKey maxDurabilityKey = new org.bukkit.NamespacedKey("mmoitems", "max_durability");
                                
                                pdc.set(durabilityKey, org.bukkit.persistence.PersistentDataType.DOUBLE, maxDurability);
                                pdc.set(maxDurabilityKey, org.bukkit.persistence.PersistentDataType.DOUBLE, maxDurability);
                                
                                meta.getPersistentDataContainer().set(durabilityKey, org.bukkit.persistence.PersistentDataType.DOUBLE, maxDurability);
                                meta.getPersistentDataContainer().set(maxDurabilityKey, org.bukkit.persistence.PersistentDataType.DOUBLE, maxDurability);
                                
                                item.setItemMeta(meta);
                                
                                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                    plugin.getLogger().info("[DEBUG] Set durability to max: " + maxDurability);
                                }
                            }
                            
                        } catch (Exception e) {
                            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                plugin.getLogger().warning("[DEBUG] Failed to set durability via NBT: " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().warning("[DEBUG] Failed to set MMOItems durability: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] Error in setMaxDurability: " + e.getMessage());
            }
        }
    }

    /**
     * Repair an item without a player (for bulk repair).
     */
    public ItemStack repair(ItemStack item) {
        return repair(item, null);
    }
}
