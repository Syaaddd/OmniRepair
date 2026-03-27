package com.github.Syaaddd.omniRepair.integration;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles integration with AdvancedEnchantments plugin.
 * Provides soft-dependency support - safely degrades if AdvancedEnchantments is not installed.
 * Uses reflection for API compatibility across different AdvancedEnchantments versions.
 */
public class CustomEnchantHook {

    private final OmniRepair plugin;
    private boolean enabled = false;

    // AdvancedEnchantments reflection
    private Method getEnchantmentsMethod = null;
    private Method addEnchantmentMethod = null;
    private Method removeEnchantmentMethod = null;
    private Method hasEnchantmentMethod = null;
    private Object advancedEnchantmentsAPI = null;

    // Common NBT key patterns used by AdvancedEnchantments
    private final List<String> possibleNBTKeys = new ArrayList<>();

    public CustomEnchantHook(OmniRepair plugin) {
        this.plugin = plugin;
        initializePossibleNBTKeys();

        // Check if AdvancedEnchantments is available
        if (plugin.getServer().getPluginManager().getPlugin("AdvancedEnchantments") != null) {
            this.enabled = true;
            plugin.getLogger().info("✓ AdvancedEnchantments integration enabled");
            initializeReflection();
        } else {
            plugin.getLogger().info("✗ AdvancedEnchantments not found - custom enchant support disabled");
        }
    }

    /**
     * Initialize list of possible NBT keys used by AdvancedEnchantments.
     */
    private void initializePossibleNBTKeys() {
        // Common NBT keys used by AdvancedEnchantments and similar plugins
        possibleNBTKeys.add("advancedenchantments");
        possibleNBTKeys.add("advanced_enchantments");
        possibleNBTKeys.add("ae_enchantments");
        possibleNBTKeys.add("ae");
        possibleNBTKeys.add("custom_enchants");
        possibleNBTKeys.add("custom_enchantments");
        possibleNBTKeys.add("enchants");
        possibleNBTKeys.add("enchantment");
    }

    /**
     * Initialize reflection methods for AdvancedEnchantments API.
     */
    private void initializeReflection() {
        try {
            // Get the AdvancedEnchantments plugin instance
            org.bukkit.plugin.Plugin aePlugin = plugin.getServer().getPluginManager().getPlugin("AdvancedEnchantments");
            if (aePlugin == null) {
                enabled = false;
                return;
            }

            // Try to get the API instance
            // Common API patterns:
            // 1. AdvancedEnchantments.getAPI()
            // 2. AdvancedEnchantmentsAPI.getInstance()
            // 3. PluginMainClass.getEnchantmentManager()

            Class<?> apiClass = null;

            // Try common API class names
            String[] possibleAPIClasses = {
                "com.bgsoftware.advancedenchantments.api.AdvancedEnchantmentsAPI",
                "com.bgsoftware.advancedenchantments.api.EnchantmentAPI",
                "com.advancedenchantments.api.AdvancedEnchantmentsAPI",
                "org.phoenixframework.advancedenchantments.api.AdvancedEnchantmentsAPI"
            };

            for (String className : possibleAPIClasses) {
                try {
                    apiClass = Class.forName(className);
                    plugin.getLogger().info("  ✓ Found API class: " + className);
                    break;
                } catch (ClassNotFoundException e) {
                    // Try next class
                }
            }

            if (apiClass == null) {
                // Fallback: Try to get API from plugin class directly
                Class<?> pluginClass = aePlugin.getClass();
                plugin.getLogger().info("  ℹ Trying to use plugin class directly: " + pluginClass.getName());

                // Look for methods that might return enchantment data
                for (Method method : pluginClass.getDeclaredMethods()) {
                    String methodName = method.getName().toLowerCase();
                    if (methodName.contains("enchant") || methodName.contains("api")) {
                        try {
                            method.setAccessible(true);
                            plugin.getLogger().info("  ℹ Found potential method: " + method.getName());
                        } catch (NoClassDefFoundError e) {
                            // Skip methods that reference missing classes (e.g., Vault Economy)
                        }
                    }
                }

                // For now, we'll use NBT-based approach as fallback
                plugin.getLogger().info("  ℹ Using NBT-based custom enchant detection");
                return;
            }

            // Try to get API instance
            try {
                Method getInstanceMethod = apiClass.getMethod("getInstance");
                advancedEnchantmentsAPI = getInstanceMethod.invoke(null);
                plugin.getLogger().info("  ✓ Got API instance");
            } catch (Exception e) {
                // Try getting API from plugin
                try {
                    Method getAPIMethod = aePlugin.getClass().getMethod("getAPI");
                    advancedEnchantmentsAPI = getAPIMethod.invoke(aePlugin);
                    plugin.getLogger().info("  ✓ Got API from plugin");
                } catch (Exception e2) {
                    plugin.getLogger().warning("  ⚠ Could not get API instance: " + e2.getMessage());
                }
            }

            // Look for enchantment-related methods
            if (advancedEnchantmentsAPI != null) {
                Class<?> apiInstanceClass = advancedEnchantmentsAPI.getClass();

                for (Method method : apiInstanceClass.getDeclaredMethods()) {
                    String methodName = method.getName().toLowerCase();

                    try {
                        if (methodName.contains("getenchantments") || methodName.contains("getenchants")) {
                            getEnchantmentsMethod = method;
                            getEnchantmentsMethod.setAccessible(true);
                            plugin.getLogger().info("  ✓ Found getEnchantments method: " + method.getName());
                        }

                        if (methodName.contains("addenchantment") || methodName.contains("addenchant")) {
                            addEnchantmentMethod = method;
                            addEnchantmentMethod.setAccessible(true);
                            plugin.getLogger().info("  ✓ Found addEnchantment method: " + method.getName());
                        }

                        if (methodName.contains("removeenchantment") || methodName.contains("removeenchant")) {
                            removeEnchantmentMethod = method;
                            removeEnchantmentMethod.setAccessible(true);
                            plugin.getLogger().info("  ✓ Found removeEnchantment method: " + method.getName());
                        }

                        if (methodName.contains("hasenchantment") || methodName.contains("hasenchant")) {
                            hasEnchantmentMethod = method;
                            hasEnchantmentMethod.setAccessible(true);
                            plugin.getLogger().info("  ✓ Found hasEnchantment method: " + method.getName());
                        }
                    } catch (NoClassDefFoundError e) {
                        // Skip methods that reference missing classes
                    }
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("  ⚠ Could not initialize AdvancedEnchantments reflection: " + e.getMessage());
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if AdvancedEnchantments integration is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get all custom enchantments from an item.
     * Returns a map of enchantment ID to level.
     *
     * @param item The item to get enchantments from
     * @return Map of enchantment ID to level, or empty map if none found
     */
    public Map<String, Integer> getCustomEnchantments(ItemStack item) {
        if (!enabled || item == null || item.getType().isAir()) {
            return java.util.Collections.emptyMap();
        }

        // Try API method first
        if (getEnchantmentsMethod != null && advancedEnchantmentsAPI != null) {
            try {
                Object result = getEnchantmentsMethod.invoke(advancedEnchantmentsAPI, item);
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> enchants = (Map<String, Integer>) result;
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().info("[DEBUG] Got custom enchantments via API: " + enchants);
                    }
                    return enchants;
                }
            } catch (Exception e) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().warning("[DEBUG] Error getting custom enchantments via API: " + e.getMessage());
                }
            }
        }

        // Fallback: Read from NBT
        return getCustomEnchantmentsFromNBT(item);
    }

    /**
     * Get custom enchantments from NBT data.
     * This is a fallback method when API is not available.
     */
    private Map<String, Integer> getCustomEnchantmentsFromNBT(ItemStack item) {
        Map<String, Integer> enchantments = new java.util.HashMap<>();

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return enchantments;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();

            // Try each possible NBT key
            for (String keyName : possibleNBTKeys) {
                NamespacedKey key = new NamespacedKey(plugin, keyName);

                // Try as string (JSON format)
                if (container.has(key, PersistentDataType.STRING)) {
                    String data = container.get(key, PersistentDataType.STRING);
                    if (plugin.getConfig().getBoolean("settings.debug", false)) {
                        plugin.getLogger().info("[DEBUG] Found custom enchant data in key '" + keyName + "': " + data);
                    }
                    // Parse the JSON data (simplified parsing)
                    parseEnchantData(data, enchantments);
                    break;
                }

                // Try as integer (single enchant level)
                if (container.has(key, PersistentDataType.INTEGER)) {
                    Integer level = container.get(key, PersistentDataType.INTEGER);
                    if (level != null && level > 0) {
                        enchantments.put(keyName, level);
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Found custom enchant '" + keyName + "' level " + level);
                        }
                        break;
                    }
                }
            }

            // Also check all keys in PDC for enchant-related data
            for (NamespacedKey key : container.getKeys()) {
                String keyStr = key.getKey().toLowerCase();
                String namespace = key.getNamespace().toLowerCase();

                if (keyStr.contains("enchant") || namespace.contains("advancedenchant") || namespace.contains("ae")) {
                    // Try to get the value
                    if (container.has(key, PersistentDataType.STRING)) {
                        String data = container.get(key, PersistentDataType.STRING);
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Found enchant key '" + key + "': " + data);
                        }
                        parseEnchantData(data, enchantments);
                    }
                }
            }

        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] Error reading custom enchantments from NBT: " + e.getMessage());
            }
        }

        return enchantments;
    }

    /**
     * Parse enchantment data from string format.
     */
    private void parseEnchantData(String data, Map<String, Integer> enchantments) {
        if (data == null || data.isEmpty()) {
            return;
        }

        // Try to parse common formats:
        // Format 1: "ENCHANT_NAME:LEVEL" or "ENCHANT_NAME:LEVEL,ENCHANT_NAME:LEVEL"
        // Format 2: JSON like {"enchant1":1,"enchant2":2}

        // Simple comma-separated format
        if (data.contains(":")) {
            String[] parts = data.split(",");
            for (String part : parts) {
                String[] kv = part.trim().split(":");
                if (kv.length == 2) {
                    try {
                        String enchantName = kv[0].trim().toUpperCase();
                        int level = Integer.parseInt(kv[1].trim());
                        enchantments.put(enchantName, level);
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
        }

        // JSON-like format (simplified parsing)
        if (data.contains("{") && data.contains("}")) {
            data = data.replace("{", "").replace("}", "").replace("\"", "");
            String[] parts = data.split(",");
            for (String part : parts) {
                String[] kv = part.trim().split(":");
                if (kv.length == 2) {
                    try {
                        String enchantName = kv[0].trim().toUpperCase();
                        int level = Integer.parseInt(kv[1].trim());
                        enchantments.put(enchantName, level);
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
        }
    }

    /**
     * Apply custom enchantments to an item.
     *
     * @param item The item to apply enchantments to
     * @param enchantments Map of enchantment ID to level
     * @return true if successful, false otherwise
     */
    public boolean applyCustomEnchantments(ItemStack item, Map<String, Integer> enchantments) {
        if (!enabled || item == null || item.getType().isAir() || enchantments == null || enchantments.isEmpty()) {
            return false;
        }

        // Try API method first
        if (addEnchantmentMethod != null && advancedEnchantmentsAPI != null) {
            try {
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    addEnchantmentMethod.invoke(advancedEnchantmentsAPI, item, entry.getKey(), entry.getValue());
                }
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Applied custom enchantments via API: " + enchantments);
                }
                return true;
            } catch (Exception e) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().warning("[DEBUG] Error applying custom enchantments via API: " + e.getMessage());
                }
            }
        }

        // Fallback: Write to NBT
        return applyCustomEnchantmentsViaNBT(item, enchantments);
    }

    /**
     * Apply custom enchantments via NBT.
     * This is a fallback method when API is not available.
     */
    private boolean applyCustomEnchantmentsViaNBT(ItemStack item, Map<String, Integer> enchantments) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return false;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();

            // Store as string format
            StringBuilder data = new StringBuilder();
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                if (data.length() > 0) {
                    data.append(",");
                }
                data.append(entry.getKey()).append(":").append(entry.getValue());
            }

            NamespacedKey key = new NamespacedKey(plugin, "advancedenchantments");
            container.set(key, PersistentDataType.STRING, data.toString());

            item.setItemMeta(meta);

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] Applied custom enchantments via NBT: " + data);
            }

            return true;
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] Error applying custom enchantments via NBT: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Check if an item has any custom enchantments.
     *
     * @param item The item to check
     * @return true if item has custom enchantments, false otherwise
     */
    public boolean hasCustomEnchantments(ItemStack item) {
        if (!enabled || item == null || item.getType().isAir()) {
            return false;
        }

        return !getCustomEnchantments(item).isEmpty();
    }

    /**
     * Copy custom enchantments from one item to another.
     * Copies all PDC data from AdvancedEnchantments and similar plugins.
     *
     * @param source The source item to copy from
     * @param target The target item to copy to
     * @return true if successful, false otherwise
     */
    public boolean copyCustomEnchantments(ItemStack source, ItemStack target) {
        if (!enabled || source == null || target == null || source.getType().isAir() || target.getType().isAir()) {
            return false;
        }

        try {
            ItemMeta sourceMeta = source.getItemMeta();
            ItemMeta targetMeta = target.getItemMeta();

            if (sourceMeta == null || targetMeta == null) {
                return false;
            }

            PersistentDataContainer sourceContainer = sourceMeta.getPersistentDataContainer();
            PersistentDataContainer targetContainer = targetMeta.getPersistentDataContainer();

            boolean hasCopied = false;

            // Copy all PDC keys from source to target
            // This preserves all custom enchant data from AdvancedEnchantments and similar plugins
            for (NamespacedKey key : sourceContainer.getKeys()) {
                String keyStr = key.getKey().toLowerCase();
                String namespace = key.getNamespace().toLowerCase();

                // Copy keys related to enchantments
                if (keyStr.contains("enchant") || namespace.contains("advancedenchant") || namespace.contains("ae")) {
                    // Copy the value based on its type
                    if (sourceContainer.has(key, PersistentDataType.STRING)) {
                        String value = sourceContainer.get(key, PersistentDataType.STRING);
                        targetContainer.set(key, PersistentDataType.STRING, value);
                        hasCopied = true;
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Copied PDC key (STRING): " + key + " = " + value);
                        }
                    } else if (sourceContainer.has(key, PersistentDataType.INTEGER)) {
                        Integer value = sourceContainer.get(key, PersistentDataType.INTEGER);
                        targetContainer.set(key, PersistentDataType.INTEGER, value);
                        hasCopied = true;
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Copied PDC key (INTEGER): " + key + " = " + value);
                        }
                    } else if (sourceContainer.has(key, PersistentDataType.DOUBLE)) {
                        Double value = sourceContainer.get(key, PersistentDataType.DOUBLE);
                        targetContainer.set(key, PersistentDataType.DOUBLE, value);
                        hasCopied = true;
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Copied PDC key (DOUBLE): " + key + " = " + value);
                        }
                    } else if (sourceContainer.has(key, PersistentDataType.LIST)) {
                        // For list types, we need to use raw compound
                        // This handles complex enchant data structures
                        targetContainer.set(key, PersistentDataType.LIST, sourceContainer.get(key, PersistentDataType.LIST));
                        hasCopied = true;
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Copied PDC key (LIST): " + key);
                        }
                    } else if (sourceContainer.has(key, PersistentDataType.TAG_CONTAINER)) {
                        // For compound tag types
                        targetContainer.set(key, PersistentDataType.TAG_CONTAINER, sourceContainer.get(key, PersistentDataType.TAG_CONTAINER));
                        hasCopied = true;
                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                            plugin.getLogger().info("[DEBUG] Copied PDC key (TAG_CONTAINER): " + key);
                        }
                    } else {
                        // Try to get as byte array (fallback)
                        byte[] value = sourceContainer.get(key, PersistentDataType.BYTE_ARRAY);
                        if (value != null) {
                            targetContainer.set(key, PersistentDataType.BYTE_ARRAY, value);
                            hasCopied = true;
                            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                plugin.getLogger().info("[DEBUG] Copied PDC key (BYTE_ARRAY): " + key);
                            }
                        }
                    }
                }
            }

            target.setItemMeta(targetMeta);

            if (hasCopied && plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] Custom enchantments copied successfully via PDC");
            }

            return hasCopied;

        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] Error copying custom enchantments: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Get a debug string showing all custom enchantments on an item.
     */
    public String getDebugString(ItemStack item) {
        if (!enabled || item == null || item.getType().isAir()) {
            return "AdvancedEnchantments not enabled or invalid item";
        }

        Map<String, Integer> enchantments = getCustomEnchantments(item);
        if (enchantments.isEmpty()) {
            return "No custom enchantments found";
        }

        StringBuilder sb = new StringBuilder("Custom enchantments: ");
        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            if (sb.length() > 22) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }
}
