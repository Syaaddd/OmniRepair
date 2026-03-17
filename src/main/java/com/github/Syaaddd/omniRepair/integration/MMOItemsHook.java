package com.github.Syaaddd.omniRepair.integration;

import com.github.Syaaddd.omniRepair.OmniRepair;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;

/**
 * Handles integration with MMOItems plugin.
 * Provides soft-dependency support - safely degrades if MMOItems is not installed.
 * Uses reflection for API compatibility across different MMOItems versions.
 */
public class MMOItemsHook {

    private final OmniRepair plugin;
    private boolean enabled = false;

    // Reflection methods for MMOItems API compatibility
    private Method getDurabilityMethod = null;
    private Method getMaxDurabilityMethod = null;
    private Method setDurabilityMethod = null;
    private Method applyMMOItemMethod = null;

    // NBT Keys for MMOItems durability
    private NamespacedKey durabilityKey = null;
    private NamespacedKey maxDurabilityKey = null;

    public MMOItemsHook(OmniRepair plugin) {
        this.plugin = plugin;

        // Check if MMOItems is available
        if (plugin.getServer().getPluginManager().getPlugin("MMOItems") != null) {
            this.enabled = plugin.getConfig().getBoolean("mmoitems.enabled", true);
            if (enabled) {
                plugin.getLogger().info("✓ MMOItems integration enabled");
                initializeReflection();
                initializeNBTKeys();
            } else {
                plugin.getLogger().info("✗ MMOItems integration disabled in config");
            }
        } else {
            plugin.getLogger().info("✗ MMOItems not found - using vanilla repair only");
        }
    }

    /**
     * Initialize reflection methods for MMOItems API.
     */
    private void initializeReflection() {
        try {
            // Try to find the durability methods in MMOItems class
            Class<?> mmoItemsClass = MMOItems.class;

            // Look for methods that work with ItemStack
            for (Method method : mmoItemsClass.getDeclaredMethods()) {
                if (method.getName().equals("getDurability") &&
                        method.getParameterCount() == 1 &&
                        method.getParameterTypes()[0] == ItemStack.class) {
                    getDurabilityMethod = method;
                    getDurabilityMethod.setAccessible(true);
                }
                if (method.getName().equals("getMaxDurability") &&
                        method.getParameterCount() == 1 &&
                        method.getParameterTypes()[0] == ItemStack.class) {
                    getMaxDurabilityMethod = method;
                    getMaxDurabilityMethod.setAccessible(true);
                }
                if (method.getName().equals("setDurability") &&
                        method.getParameterCount() == 2 &&
                        method.getParameterTypes()[0] == ItemStack.class &&
                        (method.getParameterTypes()[1] == double.class || method.getParameterTypes()[1] == Double.class ||
                                method.getParameterTypes()[1] == int.class || method.getParameterTypes()[1] == Integer.class)) {
                    setDurabilityMethod = method;
                    setDurabilityMethod.setAccessible(true);
                }
            }

            // Also try to find applyMMOItem method for repair
            for (Method method : mmoItemsClass.getDeclaredMethods()) {
                if (method.getName().equals("applyMMOItem") || method.getName().equals("getMMOItem")) {
                    applyMMOItemMethod = method;
                    applyMMOItemMethod.setAccessible(true);
                }
            }

            if (getDurabilityMethod != null) {
                plugin.getLogger().info("  ✓ Found getDurability method via reflection");
            }
            if (getMaxDurabilityMethod != null) {
                plugin.getLogger().info("  ✓ Found getMaxDurability method via reflection");
            }
            if (setDurabilityMethod != null) {
                plugin.getLogger().info("  ✓ Found setDurability method via reflection");
            } else {
                plugin.getLogger().info("  ⚠ setDurability method not found - will use NBT fallback");
            }

        } catch (Exception e) {
            plugin.getLogger().warning("  ⚠ Could not initialize MMOItems reflection: " + e.getMessage());
        }
    }

    /**
     * Initialize NBT keys for MMOItems durability.
     */
    private void initializeNBTKeys() {
        try {
            // Common MMOItems NBT keys
            durabilityKey = new NamespacedKey("mmoitems", "durability");
            maxDurabilityKey = new NamespacedKey("mmoitems", "max_durability");
        } catch (Exception e) {
            plugin.getLogger().warning("  ⚠ Could not initialize NBT keys: " + e.getMessage());
        }
    }

    /**
     * Check if MMOItems integration is enabled and available.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if an item is an MMOItem.
     */
    public boolean isMMOItem(ItemStack item) {
        if (!enabled || item == null || item.getType() == Material.AIR) {
            return false;
        }

        try {
            String id = MMOItems.getID(item);
            // Check if ID is not null AND not empty (vanilla items return empty string)
            boolean isMMO = id != null && !id.isEmpty();

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] isMMOItem check: " + (isMMO ? "ID=" + id : "Not an MMOItem"));
            }

            return isMMO;
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] isMMOItem error: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Get the current durability of an MMOItem.
     * Uses reflection for API compatibility across versions.
     * Falls back to NBT reading if reflection fails.
     * @return Current durability, or -1 if not an MMOItem or error occurred
     */
    public double getDurability(ItemStack item) {
        if (!enabled || item == null || item.getType() == Material.AIR) {
            return -1;
        }

        try {
            // First try the static method via reflection
            if (getDurabilityMethod != null) {
                Object result = getDurabilityMethod.invoke(null, item);
                if (result instanceof Double) {
                    return (Double) result;
                } else if (result instanceof Integer) {
                    return ((Integer) result).doubleValue();
                }
            }

            // Fallback: Try to read from NBT
            double nbtDurability = readDurabilityFromNBT(item);
            if (nbtDurability >= 0) {
                return nbtDurability;
            }

            return -1;
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("Error getting durability: " + e.getMessage());
            }
            return -1;
        }
    }

    /**
     * Get the maximum durability of an MMOItem.
     * @return Max durability, or -1 if not an MMOItem or error occurred
     */
    public double getMaxDurability(ItemStack item) {
        if (!enabled || item == null || item.getType() == Material.AIR) {
            return -1;
        }

        try {
            // First try the static method via reflection
            if (getMaxDurabilityMethod != null) {
                Object result = getMaxDurabilityMethod.invoke(null, item);
                if (result instanceof Double) {
                    return (Double) result;
                } else if (result instanceof Integer) {
                    return ((Integer) result).doubleValue();
                }
            }

            // Fallback: Try to read from NBT
            double nbtMaxDurability = readMaxDurabilityFromNBT(item);
            if (nbtMaxDurability >= 0) {
                return nbtMaxDurability;
            }

            return -1;
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("Error getting max durability: " + e.getMessage());
            }
            return -1;
        }
    }

    /**
     * Read durability from NBT data.
     */
    private double readDurabilityFromNBT(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return -1;

            PersistentDataContainer container = meta.getPersistentDataContainer();

            // Try common NBT keys used by MMOItems
            if (container.has(durabilityKey, PersistentDataType.DOUBLE)) {
                return container.get(durabilityKey, PersistentDataType.DOUBLE);
            }
            if (container.has(durabilityKey, PersistentDataType.INTEGER)) {
                return container.get(durabilityKey, PersistentDataType.INTEGER).doubleValue();
            }

            // Try alternative key names
            NamespacedKey altKey = new NamespacedKey("mmoitems", "current_durability");
            if (container.has(altKey, PersistentDataType.DOUBLE)) {
                return container.get(altKey, PersistentDataType.DOUBLE);
            }

        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("Error reading durability from NBT: " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * Read max durability from NBT data.
     */
    private double readMaxDurabilityFromNBT(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return -1;

            PersistentDataContainer container = meta.getPersistentDataContainer();

            // Try common NBT keys used by MMOItems
            if (container.has(maxDurabilityKey, PersistentDataType.DOUBLE)) {
                return container.get(maxDurabilityKey, PersistentDataType.DOUBLE);
            }
            if (container.has(maxDurabilityKey, PersistentDataType.INTEGER)) {
                return container.get(maxDurabilityKey, PersistentDataType.INTEGER).doubleValue();
            }

            // Try alternative key names
            NamespacedKey altKey = new NamespacedKey("mmoitems", "max_hp");
            if (container.has(altKey, PersistentDataType.DOUBLE)) {
                return container.get(altKey, PersistentDataType.DOUBLE);
            }

        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("Error reading max durability from NBT: " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * Repair an MMOItem by setting its durability to maximum.
     * Uses reflection if available, falls back to NBT modification.
     * @param item The item to repair
     * @return true if successful, false otherwise
     */
    public boolean repair(ItemStack item) {
        if (!enabled || item == null || item.getType() == Material.AIR) {
            return false;
        }

        try {
            double maxDurability = getMaxDurability(item);
            if (maxDurability < 0) {
                return false;
            }

            // Try using reflection first
            if (setDurabilityMethod != null) {
                setDurabilityMethod.invoke(null, item, maxDurability);
                return true;
            }

            // Fallback: Use NBT modification
            return repairViaNBT(item, maxDurability);

        } catch (Exception e) {
            plugin.getLogger().warning("Error repairing MMOItem: " + e.getMessage());
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Repair item by modifying NBT data directly.
     */
    private boolean repairViaNBT(ItemStack item, double maxDurability) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer container = meta.getPersistentDataContainer();

            // Set durability to max
            container.set(durabilityKey, PersistentDataType.DOUBLE, maxDurability);

            // Also update alternative keys for compatibility
            NamespacedKey altKey = new NamespacedKey("mmoitems", "current_durability");
            container.set(altKey, PersistentDataType.DOUBLE, maxDurability);

            item.setItemMeta(meta);

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("  ✓ Repaired MMOItem via NBT: " + MMOItems.getID(item));
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error repairing MMOItem via NBT: " + e.getMessage());
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Check if an MMOItem is damaged.
     * Uses multiple fallback methods to detect damage.
     */
    public boolean isDamaged(ItemStack item) {
        if (!enabled || item == null || item.getType() == Material.AIR) {
            return false;
        }

        // Log at the very start
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] === Starting isDamaged check for: " + item.getType().name() + " ===");
            plugin.getLogger().info("[DEBUG] MMOItems Hook enabled: " + enabled);
        }

        try {
            // Method 1: Use API methods if available
            if (getDurabilityMethod != null && getMaxDurabilityMethod != null) {
                double current = getDurability(item);
                double max = getMaxDurability(item);

                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Method 1 (API) - Current: " + current + ", Max: " + max);
                }

                if (current >= 0 && max > 0) {
                    boolean damaged = current < max;
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().info("[DEBUG] Method 1 Result: " + damaged);
                    }
                    return damaged;
                }
            } else {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Method 1 skipped - API methods not available");
                }
            }

            // Method 2: Read directly from NBT using PersistentDataContainer
            double nbtCurrent = readDurabilityFromNBT(item);
            double nbtMax = readMaxDurabilityFromNBT(item);

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] Method 2 (NBT PDC) - Current: " + nbtCurrent + ", Max: " + nbtMax);
            }

            if (nbtCurrent >= 0 && nbtMax > 0) {
                boolean damaged = nbtCurrent < nbtMax;
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Method 2 Result: " + damaged);
                }
                return damaged;
            } else {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Method 2 failed - Could not read NBT values");
                }
            }

            // Method 3: Check if item has MMOItems ID and durability stat
            String id = net.Indyuce.mmoitems.MMOItems.getID(item);
            if (id != null && !id.isEmpty()) {
                try {
                    net.Indyuce.mmoitems.api.Type type = net.Indyuce.mmoitems.MMOItems.getType(item);
                    
                    if (type != null) {
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Method 3 - Type: " + type.getId() + ", ID: " + id);
                        }
                        
                        // Check if this type has durability stat using reflection
                        try {
                            Object mmoItem = null;
                            try {
                                // Try newer API
                                Method getMMOItemMethod = MMOItems.plugin.getClass().getMethod("getMMOItem", net.Indyuce.mmoitems.api.Type.class, String.class);
                                if (getMMOItemMethod != null) {
                                    mmoItem = getMMOItemMethod.invoke(MMOItems.plugin, type, id);
                                }
                            } catch (Exception e) {
                                // Try older API
                                try {
                                    Method getItemsMethod = MMOItems.plugin.getClass().getMethod("getItems");
                                    Object itemsManager = getItemsMethod.invoke(MMOItems.plugin);
                                    Method getMMOItemMethod2 = itemsManager.getClass().getMethod("getMMOItem", net.Indyuce.mmoitems.api.Type.class, String.class);
                                    if (getMMOItemMethod2 != null) {
                                        mmoItem = getMMOItemMethod2.invoke(itemsManager, type, id);
                                    }
                                } catch (Exception e2) {
                                    // Ignore
                                }
                            }
                            
                            if (mmoItem != null) {
                                // Check if has durability stat
                                boolean hasDurability = false;
                                try {
                                    Method hasDataMethod = mmoItem.getClass().getMethod("hasData", String.class);
                                    if (hasDataMethod != null) {
                                        Object result = hasDataMethod.invoke(mmoItem, "DURABILITY");
                                        if (result instanceof Boolean) {
                                            hasDurability = (Boolean) result;
                                        }
                                    }
                                } catch (Exception e) {
                                    // Try alternative
                                    try {
                                        Method getStatsMethod = mmoItem.getClass().getMethod("getStats");
                                        Object stats = getStatsMethod.invoke(mmoItem);
                                        if (stats instanceof org.bukkit.configuration.ConfigurationSection) {
                                            org.bukkit.configuration.ConfigurationSection statsSection = (org.bukkit.configuration.ConfigurationSection) stats;
                                            hasDurability = statsSection.contains("DURABILITY");
                                        }
                                    } catch (Exception e2) {
                                        // Ignore
                                    }
                                }
                                
                                if (hasDurability) {
                                    // Has durability stat - item can be damaged
                                    // Since we can't read exact values, assume it needs repair
                                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                        plugin.getLogger().info("[DEBUG] Method 3 Result: Has durability stat, assuming damaged");
                                    }
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                plugin.getLogger().info("[DEBUG] Method 3 - Could not check template");
                            }
                        }
                    }
                } catch (Exception e) {
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().warning("[DEBUG] Method 3 error: " + e.getMessage());
                    }
                }
            }

            // If all methods fail, return false
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] === All methods failed, returning false ===");
            }
            return false;

        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] isDamaged exception: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Get the durability percentage of an MMOItem.
     * @return Percentage (0-100), or -1 if error
     */
    public double getDurabilityPercent(ItemStack item) {
        if (!enabled || item == null || item.getType() == Material.AIR) {
            return -1;
        }

        try {
            double current = getDurability(item);
            double max = getMaxDurability(item);

            if (current < 0 || max <= 0) {
                return -1;
            }

            return (current / max) * 100.0;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get the damage percentage (how much durability is lost).
     * @return Damage percentage (0-100), or -1 if error
     */
    public double getDamagePercent(ItemStack item) {
        double durabilityPercent = getDurabilityPercent(item);
        if (durabilityPercent < 0) {
            return -1;
        }
        return 100.0 - durabilityPercent;
    }

    /**
     * Check if an MMOItem ID is blacklisted.
     */
    public boolean isBlacklisted(String mmoItemId) {
        if (!enabled || mmoItemId == null) {
            return false;
        }

        return plugin.getConfig().getStringList("blacklist.mmoitems-ids")
                .stream()
                .anyMatch(id -> id.equalsIgnoreCase(mmoItemId));
    }
}