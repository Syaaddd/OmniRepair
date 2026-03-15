package com.github.Syaaddd.omniRepair.utils;

import com.github.Syaaddd.omniRepair.OmniRepair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for updating item lore, especially durability displays.
 * Supports both vanilla and MMOItems lore synchronization.
 */
public class LoreUpdater {

    private final OmniRepair plugin;
    private final LegacyComponentSerializer serializer;

    public LoreUpdater(OmniRepair plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    /**
     * Update the durability lore on an item.
     * 
     * @param item The item to update
     * @param currentDurability Current durability value
     * @param maxDurability Maximum durability value
     * @return The updated item
     */
    public ItemStack updateDurabilityLore(ItemStack item, double currentDurability, double maxDurability) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        if (!plugin.getConfig().getBoolean("mmoitems.sync-lore", true)) {
            return item;
        }

        try {
            ItemStack result = item.clone();
            ItemMeta meta = result.getItemMeta();

            if (meta == null) {
                return result;
            }

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            if (lore == null) {
                lore = new ArrayList<>();
            }

            // Get lore configuration
            String format = plugin.getConfig().getString("mmoitems.lore-format.line", 
                    "&7Durability: &a{current} &7/ &a{max} &7(&e{percent}%&7)");
            String position = plugin.getConfig().getString("mmoitems.lore-format.position", "ADD_BOTTOM");

            // Format the durability line
            int percent = (int) ((currentDurability / maxDurability) * 100);
            String formattedLine = format
                    .replace("{current}", String.valueOf((int) currentDurability))
                    .replace("{max}", String.valueOf((int) maxDurability))
                    .replace("{percent}", String.valueOf(percent));

            // Convert legacy color codes to Component and back for proper formatting
            Component component = serializer.deserialize(formattedLine);
            String coloredLine = serializer.serialize(component);

            // Handle position
            if ("REPLACE_EXISTING".equalsIgnoreCase(position)) {
                // Remove existing durability lines
                List<String> patterns = plugin.getConfig().getStringList("mmoitems.lore-patterns");
                lore.removeIf(line -> {
                    for (String pattern : patterns) {
                        if (line.toLowerCase().matches(pattern.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                });
                lore.add(coloredLine);
            } else if ("ADD_TOP".equalsIgnoreCase(position)) {
                lore.add(0, coloredLine);
            } else {
                // Default: ADD_BOTTOM
                lore.add(coloredLine);
            }

            meta.setLore(lore);
            result.setItemMeta(meta);
            return result;
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating durability lore: " + e.getMessage());
            return item;
        }
    }

    /**
     * Add custom lore lines to an item for GUI preview.
     * 
     * @param item The item
     * @param currentDurability Current durability
     * @param maxDurability Maximum durability
     * @param cost Repair cost
     * @return The item with preview lore
     */
    public ItemStack addPreviewLore(ItemStack item, double currentDurability, double maxDurability, double cost) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        try {
            ItemStack result = item.clone();
            ItemMeta meta = result.getItemMeta();

            if (meta == null) {
                return result;
            }

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            if (lore == null) {
                lore = new ArrayList<>();
            }

            // Get preview lore configuration
            List<String> previewLore = plugin.getConfig().getStringList("gui.preview-lore");
            
            int percent = (int) ((currentDurability / maxDurability) * 100);
            int damagePercent = 100 - percent;

            for (String line : previewLore) {
                String formatted = line
                        .replace("{current}", String.valueOf((int) currentDurability))
                        .replace("{max}", String.valueOf((int) maxDurability))
                        .replace("{percent}", String.valueOf(percent))
                        .replace("{damagePercent}", String.valueOf(damagePercent))
                        .replace("{cost}", plugin.getVaultHook().format(cost));

                Component component = serializer.deserialize(formatted);
                String coloredLine = serializer.serialize(component);
                lore.add(coloredLine);
            }

            meta.setLore(lore);
            result.setItemMeta(meta);
            return result;
        } catch (Exception e) {
            plugin.getLogger().warning("Error adding preview lore: " + e.getMessage());
            return item;
        }
    }

    /**
     * Remove preview lore from an item (used when taking item from GUI).
     * 
     * @param item The item
     * @param originalLore The original lore (before preview was added)
     * @return The item with preview lore removed
     */
    public ItemStack removePreviewLore(ItemStack item, List<String> originalLore) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        try {
            ItemStack result = item.clone();
            ItemMeta meta = result.getItemMeta();

            if (meta == null || !meta.hasLore()) {
                return result;
            }

            List<String> lore = meta.getLore();
            if (lore == null) {
                return result;
            }

            // Remove preview lore lines (lines added at the end)
            int originalSize = originalLore != null ? originalLore.size() : 0;
            
            while (lore.size() > originalSize) {
                lore.remove(lore.size() - 1);
            }

            if (lore.isEmpty()) {
                meta.setLore(null);
            } else {
                meta.setLore(lore);
            }

            result.setItemMeta(meta);
            return result;
        } catch (Exception e) {
            plugin.getLogger().warning("Error removing preview lore: " + e.getMessage());
            return item;
        }
    }

    /**
     * Colorize a string using legacy color codes.
     */
    public String colorize(String text) {
        if (text == null) {
            return "";
        }
        Component component = serializer.deserialize(text);
        return serializer.serialize(component);
    }

    /**
     * Get formatted durability string.
     */
    public String formatDurability(double current, double max) {
        int percent = (int) ((current / max) * 100);
        String color = percent > 50 ? "&a" : percent > 25 ? "&e" : "&c";
        return color + (int) current + " &7/ &a" + (int) max + " &7(" + color + percent + "%&7)";
    }
}
