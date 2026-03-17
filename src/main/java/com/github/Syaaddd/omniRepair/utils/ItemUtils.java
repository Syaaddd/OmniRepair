package com.github.Syaaddd.omniRepair.utils;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Utility class for item-related operations.
 * Provides methods for checking durability, blacklist, and item properties.
 */
public class ItemUtils {

    private final OmniRepair plugin;

    public ItemUtils(OmniRepair plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if an item is damaged (vanilla or MMOItems).
     */
    public boolean isDamaged(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] isDamaged: item is null or air");
            }
            return false;
        }

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] isDamaged: Checking item " + item.getType().name());
        }

        // Check MMOItems durability FIRST (higher priority for RPG items)
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()) {
            // Check if it's an MMOItem first
            if (plugin.getMmoItemsHook().isMMOItem(item)) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] isDamaged: Item is MMOItem, checking MMOItems hook");
                }
                
                boolean isDamaged = plugin.getMmoItemsHook().isDamaged(item);
                
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    String mmoId = net.Indyuce.mmoitems.MMOItems.getID(item);
                    double current = plugin.getMmoItemsHook().getDurability(item);
                    double max = plugin.getMmoItemsHook().getMaxDurability(item);
                    plugin.getLogger().info("[DEBUG] MMOItem Check - ID: " + mmoId + 
                        ", Current: " + current + ", Max: " + max + ", IsDamaged: " + isDamaged);
                }
                
                return isDamaged;
            }
        }

        // Check vanilla durability for non-MMOItems
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] isDamaged: Checking vanilla damage for " + item.getType().name());
        }
        
        boolean vanillaDamaged = hasVanillaDamage(item);
        
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Vanilla item damage check: " + vanillaDamaged + " for " + item.getType().name());
        }
        
        return vanillaDamaged;
    }

    /**
     * Check if an item has vanilla durability damage.
     */
    public boolean hasVanillaDamage(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        // Check if item type can have durability
        if (!isDurabilityItem(item.getType())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check if meta is Damageable
        if (!(meta instanceof Damageable)) {
            return false;
        }

        Damageable damageable = (Damageable) meta;

        // Check if item has damage
        if (!damageable.hasDamage()) {
            return false;
        }

        int damage = damageable.getDamage();
        int maxDurability = getMaxVanillaDurability(item.getType());

        // Item is damaged if damage > 0 and damage <= maxDurability
        // In Minecraft, damage increases as item is used (0 = new, maxDurability = broken)
        boolean isDamaged = damage > 0 && damage <= maxDurability;

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] hasVanillaDamage: " + isDamaged +
                " (damage=" + damage + ", max=" + maxDurability + ") for " + item.getType().name());
        }

        return isDamaged;
    }

    /**
     * Get the current durability of an item (vanilla or MMOItems).
     * @return Current durability, or -1 if not applicable
     */
    public double getCurrentDurability(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return -1;
        }

        // Try MMOItems first
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()) {
            double mmoDurability = plugin.getMmoItemsHook().getDurability(item);
            if (mmoDurability >= 0) {
                return mmoDurability;
            }
        }

        // Fallback to vanilla
        if (isDurabilityItem(item.getType())) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                int maxDurability = getMaxVanillaDurability(item.getType());
                int damage = damageable.hasDamage() ? damageable.getDamage() : 0;
                return maxDurability - damage;
            }
            return getMaxVanillaDurability(item.getType());
        }

        return -1;
    }

    /**
     * Get the maximum durability of an item.
     */
    public double getMaxDurability(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return -1;
        }

        // Try MMOItems first
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()) {
            double mmoMaxDurability = plugin.getMmoItemsHook().getMaxDurability(item);
            if (mmoMaxDurability >= 0) {
                return mmoMaxDurability;
            }
        }

        // Fallback to vanilla
        return getMaxVanillaDurability(item.getType());
    }

    /**
     * Get the maximum vanilla durability for a material.
     */
    public int getMaxVanillaDurability(Material material) {
        return switch (material) {
            case WOODEN_SWORD, WOODEN_PICKAXE, WOODEN_AXE, WOODEN_SHOVEL, WOODEN_HOE -> 60;
            case GOLDEN_SWORD, GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_HOE -> 33;
            case STONE_SWORD, STONE_PICKAXE, STONE_AXE, STONE_SHOVEL, STONE_HOE -> 132;
            case IRON_SWORD, IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE -> 251;
            case DIAMOND_SWORD, DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SHOVEL, DIAMOND_HOE -> 1562;
            case NETHERITE_SWORD, NETHERITE_PICKAXE, NETHERITE_AXE, NETHERITE_SHOVEL, NETHERITE_HOE -> 2032;

            case BOW, FISHING_ROD -> 385;
            case CROSSBOW, TRIDENT -> 251;
            case SHIELD -> 337;

            case LEATHER_HELMET -> 56;
            case LEATHER_CHESTPLATE -> 81;
            case LEATHER_LEGGINGS -> 76;
            case LEATHER_BOOTS -> 66;

            case CHAINMAIL_HELMET -> 166;
            case CHAINMAIL_CHESTPLATE -> 241;
            case CHAINMAIL_LEGGINGS -> 226;
            case CHAINMAIL_BOOTS -> 199;

            case IRON_HELMET -> 166;
            case IRON_CHESTPLATE -> 241;
            case IRON_LEGGINGS -> 226;
            case IRON_BOOTS -> 199;

            case GOLDEN_HELMET -> 78;
            case GOLDEN_CHESTPLATE -> 113;
            case GOLDEN_LEGGINGS -> 106;
            case GOLDEN_BOOTS -> 92;

            case DIAMOND_HELMET -> 364;
            case DIAMOND_CHESTPLATE -> 529;
            case DIAMOND_LEGGINGS -> 496;
            case DIAMOND_BOOTS -> 430;

            case NETHERITE_HELMET -> 408;
            case NETHERITE_CHESTPLATE -> 593;
            case NETHERITE_LEGGINGS -> 556;
            case NETHERITE_BOOTS -> 482;

            case TURTLE_HELMET -> 276;
            case ELYTRA -> 432;
            case SHEARS -> 239;
            case FLINT_AND_STEEL -> 65;
            case CARROT_ON_A_STICK -> 26;
            case WARPED_FUNGUS_ON_A_STICK -> 100;

            default -> 0;
        };
    }

    /**
     * Check if a material can have durability.
     */
    public boolean isDurabilityItem(Material material) {
        return getMaxVanillaDurability(material) > 0;
    }

    /**
     * Get the durability percentage of an item.
     * @return Percentage (0-100), or -1 if not applicable
     */
    public double getDurabilityPercent(ItemStack item) {
        double current = getCurrentDurability(item);
        double max = getMaxDurability(item);

        if (current < 0 || max <= 0) {
            return -1;
        }

        return (current / max) * 100.0;
    }

    /**
     * Get the damage percentage (how much durability is lost).
     * @return Damage percentage (0-100), or -1 if not applicable
     */
    public double getDamagePercent(ItemStack item) {
        double durabilityPercent = getDurabilityPercent(item);
        if (durabilityPercent < 0) {
            return -1;
        }
        return 100.0 - durabilityPercent;
    }

    /**
     * Check if an item is blacklisted from repair.
     */
    public boolean isBlacklisted(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ConfigurationSection blacklist = plugin.getConfig().getConfigurationSection("blacklist");
        if (blacklist == null) {
            return false;
        }

        // Check material blacklist
        List<String> materialBlacklist = blacklist.getStringList("materials");
        if (materialBlacklist.contains(item.getType().name())) {
            return true;
        }

        // Check lore blacklist
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                List<String> loreBlacklist = blacklist.getStringList("lore-contains");
                for (String line : lore) {
                    for (String blacklisted : loreBlacklist) {
                        if (line.toLowerCase().contains(blacklisted.toLowerCase())) {
                            return true;
                        }
                    }
                }
            }
        }

        // Check name blacklist
        if (meta != null && meta.hasDisplayName()) {
            String name = meta.getDisplayName();
            List<String> nameBlacklist = blacklist.getStringList("name-contains");
            for (String blacklisted : nameBlacklist) {
                if (name.toLowerCase().contains(blacklisted.toLowerCase())) {
                    return true;
                }
            }
        }

        // Check MMOItems blacklist
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()) {
            String mmoId = net.Indyuce.mmoitems.MMOItems.getID(item);
            if (mmoId != null && plugin.getMmoItemsHook().isBlacklisted(mmoId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if an item is soulbound.
     */
    public boolean isSoulbound(ItemStack item) {
        if (!plugin.getConfig().getBoolean("safety.respect-soulbound", true)) {
            return false;
        }

        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check lore for soulbound indicators
        if (meta.hasLore()) {
            List<String> soulboundLore = plugin.getConfig().getStringList("safety.soulbound-lore");
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    for (String soulbound : soulboundLore) {
                        if (line.toLowerCase().contains(soulbound.toLowerCase())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if an item can be repaired (not blacklisted, not soulbound, damaged).
     */
    public boolean canRepair(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        if (isBlacklisted(item)) {
            return false;
        }

        if (isSoulbound(item)) {
            return false;
        }

        return isDamaged(item);
    }
}
