package com.github.Syaaddd.omniRepair.repair;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.utils.LoreUpdater;
import com.github.Syaaddd.omniRepair.utils.NBTProtection;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
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

        // Check if item can be obtained from MMOItems (valid item) and has durability stat
        try {
            ItemStack template = net.Indyuce.mmoitems.MMOItems.plugin.getItem(type, id);
            if (template == null) {
                return false;
            }

            // Restrict to items that actually have a durability stat
            net.Indyuce.mmoitems.api.item.mmoitem.MMOItem mmoItem =
                    net.Indyuce.mmoitems.MMOItems.plugin.getItems().getMMOItem(type, id);
            if (mmoItem != null && !mmoItem.hasData(net.Indyuce.mmoitems.ItemStats.DURABILITY)) {
                // Also accept if the item already has durability stored in NBT
                if (plugin.getMmoItemsHook().getDurability(item) < 0) {
                    // Also accept if item has vanilla damage (Max Vanilla Durability mode)
                    if (!plugin.getItemUtils().hasVanillaDamage(item)) {
                        return false;
                    }
                }
            }

            // Check if the item is actually damaged
            boolean isDamaged = plugin.getMmoItemsHook().isDamaged(item);
            if (!isDamaged) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] MMOItems canRepair: Item " + type.getId() + ":" + id + " is not damaged");
                }
                return false;
            }
        } catch (Exception e) {
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

                // Merge lore: keep template's fresh durability lines, add custom non-durability lines from original
                repairedMeta.setLore(mergeLore(item, repairedItem));

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

            // Check if item uses vanilla durability (Max Vanilla Durability mode)
            boolean usesVanillaDurability = plugin.getMmoItemsHook().getDurability(repairedItem) < 0
                && plugin.getMmoItemsHook().getMaxDurability(repairedItem) < 0
                && plugin.getItemUtils().isDamageable(item);

            if (usesVanillaDurability) {
                // Repair via vanilla Damageable system for MMOItems with Max Vanilla Durability
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Item uses Max Vanilla Durability, repairing via vanilla system");
                }
                org.bukkit.inventory.meta.ItemMeta meta = repairedItem.getItemMeta();
                if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                    ((org.bukkit.inventory.meta.Damageable) meta).setDamage(0);
                    repairedItem.setItemMeta(meta);
                }
            } else {
                // Set durability to max using actual max durability from MMOItems API/NBT
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Attempting to set MMOItems durability to max...");
                }
                setMaxDurability(repairedItem, type, id);
            }

            // Update lore to reflect correct durability values
            if (plugin.getConfig().getBoolean("mmoitems.sync-lore", true)) {
                try {
                    double current = plugin.getMmoItemsHook().getDurability(repairedItem);
                    double max = plugin.getMmoItemsHook().getMaxDurability(repairedItem);
                    if (current > 0 && max > 0) {
                        repairedItem = loreUpdater.updateDurabilityLore(repairedItem, current, max);
                    }
                } catch (Exception e) {
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().warning("[DEBUG] Failed to update durability lore: " + e.getMessage());
                    }
                }
            }

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
     * Merge lore from original item and fresh template.
     * Keeps fresh durability display lines from template,
     * adds custom non-durability lines from original item.
     */
    private List<String> mergeLore(ItemStack original, ItemStack freshTemplate) {
        List<String> durabilityPatterns = plugin.getConfig().getStringList("mmoitems.lore-patterns");
        if (durabilityPatterns.isEmpty()) {
            durabilityPatterns = List.of(".*Durability:.*", ".*Durability.*", ".*HP:.*");
        }

        List<String> originalLore = original.hasItemMeta() && original.getItemMeta().hasLore()
                ? original.getItemMeta().getLore()
                : new ArrayList<>();
        List<String> templateLore = freshTemplate.hasItemMeta() && freshTemplate.getItemMeta().hasLore()
                ? freshTemplate.getItemMeta().getLore()
                : new ArrayList<>();

        List<String> merged = new ArrayList<>();

        // Start with fresh template lore (has correct durability display)
        merged.addAll(templateLore);

        // Add custom lines from original that are NOT durability-related
        if (originalLore != null) {
            for (String line : originalLore) {
                boolean isDurabilityLine = false;
                for (String pattern : durabilityPatterns) {
                    if (line.toLowerCase().matches(pattern.toLowerCase())) {
                        isDurabilityLine = true;
                        break;
                    }
                }
                if (!isDurabilityLine && !templateLore.contains(line)) {
                    merged.add(line);
                }
            }
        }

        return merged;
    }

    /**
     * Set durability to max using actual max durability from MMOItems API/NBT.
     */
    private void setMaxDurability(ItemStack item, Type type, String id) {
        try {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] setMaxDurability for " + type.getId() + ":" + id);
            }

            // 1. Try reading existing max durability from item's PDC (preserved from template)
            double maxDurability = plugin.getMmoItemsHook().getMaxDurability(item);

            // 2. Fallback: get fresh template lookup
            if (maxDurability <= 0) {
                ItemStack template = MMOItems.plugin.getItem(type, id);
                if (template != null) {
                    maxDurability = plugin.getMmoItemsHook().getMaxDurability(template);
                }
            }

            // 3. Fallback: read directly from item's PDC keys
            if (maxDurability <= 0) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    NamespacedKey key = NamespacedKey.fromString("mmoitems:max_durability");
                    if (key != null) {
                        if (pdc.has(key, PersistentDataType.DOUBLE)) {
                            maxDurability = pdc.get(key, PersistentDataType.DOUBLE);
                        } else if (pdc.has(key, PersistentDataType.INTEGER)) {
                            maxDurability = pdc.get(key, PersistentDataType.INTEGER).doubleValue();
                        }
                    }
                }
            }

            if (maxDurability <= 0) {
                plugin.getLogger().warning("Could not determine max durability for " + type.getId() + ":" + id + ", using 100.0");
                maxDurability = 100.0;
            }

            // Set durability to max in NBT
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey durabilityKey = NamespacedKey.fromString("mmoitems:durability");
            NamespacedKey maxDurabilityKey = NamespacedKey.fromString("mmoitems:max_durability");
            NamespacedKey altCurrentKey = NamespacedKey.fromString("mmoitems:current_durability");

            if (durabilityKey != null) {
                pdc.set(durabilityKey, PersistentDataType.DOUBLE, maxDurability);
            }
            if (maxDurabilityKey != null) {
                pdc.set(maxDurabilityKey, PersistentDataType.DOUBLE, maxDurability);
            }
            if (altCurrentKey != null) {
                pdc.set(altCurrentKey, PersistentDataType.DOUBLE, maxDurability);
            }

            item.setItemMeta(meta);

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] Set MMOItems durability to max: " + maxDurability);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error in setMaxDurability: " + e.getMessage());
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
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
