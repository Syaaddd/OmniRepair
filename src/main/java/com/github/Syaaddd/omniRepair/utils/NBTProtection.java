package com.github.Syaaddd.omniRepair.utils;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Utility class for NBT protection and item cloning.
 * Ensures all NBT data is preserved during repair operations.
 */
public class NBTProtection {

    private final OmniRepair plugin;

    public NBTProtection(OmniRepair plugin) {
        this.plugin = plugin;
    }

    /**
     * Create a safe clone of an ItemStack before modification.
     * This preserves all NBT data including enchantments, custom model data, etc.
     * 
     * @param item The item to clone
     * @return A safe clone of the item, or null if input is null/air
     */
    public ItemStack cloneSafely(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        try {
            // Use Bukkit's clone method which preserves all NBT
            return item.clone();
        } catch (Exception e) {
            plugin.getLogger().warning("Error cloning item: " + e.getMessage());
            // Return a basic copy if clone fails
            return new ItemStack(item.getType(), item.getAmount());
        }
    }

    /**
     * Verify that NBT data is preserved after an operation.
     * Checks type, amount, enchantments, display name, lore, custom model data,
     * PDC keys, and custom enchantments.
     */
    public boolean verifyNBT(ItemStack original, ItemStack modified) {
        if (original == null || modified == null) {
            return false;
        }

        try {
            // Check basic properties
            if (original.getType() != modified.getType()) {
                plugin.getLogger().warning("NBT verification failed: Item type changed");
                return false;
            }

            if (original.getAmount() != modified.getAmount()) {
                plugin.getLogger().warning("NBT verification failed: Item amount changed");
                return false;
            }

            // Check ItemMeta presence
            if (original.hasItemMeta() != modified.hasItemMeta()) {
                plugin.getLogger().warning("NBT verification failed: ItemMeta presence changed");
                return false;
            }

            if (!original.hasItemMeta()) {
                return true; // No meta to verify
            }

            ItemMeta originalMeta = original.getItemMeta();
            ItemMeta modifiedMeta = modified.getItemMeta();
            if (originalMeta == null || modifiedMeta == null) {
                return false;
            }

            // Verify enchantments are preserved
            if (original.getEnchantments().size() != modified.getEnchantments().size()) {
                plugin.getLogger().warning("NBT verification failed: Enchantment count changed");
                return false;
            }

            for (var entry : original.getEnchantments().entrySet()) {
                int modifiedLevel = modified.getEnchantmentLevel(entry.getKey());
                if (modifiedLevel != entry.getValue()) {
                    plugin.getLogger().warning("NBT verification failed: Enchantment level changed");
                    return false;
                }
            }

            // Verify display name is preserved
            if (originalMeta.hasDisplayName() != modifiedMeta.hasDisplayName()) {
                plugin.getLogger().warning("NBT verification failed: Display name presence changed");
                return false;
            }
            if (originalMeta.hasDisplayName() && !originalMeta.getDisplayName().equals(modifiedMeta.getDisplayName())) {
                plugin.getLogger().warning("NBT verification failed: Display name changed");
                return false;
            }

            // Verify lore is preserved
            List<String> originalLore = originalMeta.getLore();
            List<String> modifiedLore = modifiedMeta.getLore();
            if ((originalLore == null || originalLore.isEmpty()) != (modifiedLore == null || modifiedLore.isEmpty())) {
                plugin.getLogger().warning("NBT verification failed: Lore presence changed");
                return false;
            }
            if (originalLore != null && modifiedLore != null && !originalLore.equals(modifiedLore)) {
                // Only warn for lore changes (some handlers legitimately update durability lore)
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] NBT verification: Lore content changed (may be intentional)");
                }
            }

            // Verify custom model data is preserved
            if (originalMeta.hasCustomModelData() != modifiedMeta.hasCustomModelData()) {
                plugin.getLogger().warning("NBT verification failed: Custom model data presence changed");
                return false;
            }
            if (originalMeta.hasCustomModelData() && originalMeta.getCustomModelData() != modifiedMeta.getCustomModelData()) {
                plugin.getLogger().warning("NBT verification failed: Custom model data changed");
                return false;
            }

            // Verify PersistentDataContainer keys are preserved
            PersistentDataContainer originalPDC = originalMeta.getPersistentDataContainer();
            PersistentDataContainer modifiedPDC = modifiedMeta.getPersistentDataContainer();

            for (NamespacedKey key : originalPDC.getKeys()) {
                boolean hasKey = modifiedPDC.has(key, PersistentDataType.STRING)
                    || modifiedPDC.has(key, PersistentDataType.INTEGER)
                    || modifiedPDC.has(key, PersistentDataType.DOUBLE)
                    || modifiedPDC.has(key, PersistentDataType.LONG)
                    || modifiedPDC.has(key, PersistentDataType.BYTE_ARRAY);
                if (!hasKey) {
                    plugin.getLogger().warning("NBT verification failed: PDC key '" + key + "' missing from modified item");
                    return false;
                }
            }

            // Verify custom enchantments are preserved (AdvancedEnchantments, etc.)
            if (plugin.getCustomEnchantHook() != null && plugin.getCustomEnchantHook().isEnabled()) {
                boolean originalHasCustom = plugin.getCustomEnchantHook().hasCustomEnchantments(original);
                boolean modifiedHasCustom = plugin.getCustomEnchantHook().hasCustomEnchantments(modified);

                if (originalHasCustom && !modifiedHasCustom) {
                    plugin.getLogger().warning("NBT verification failed: Custom enchantments were lost!");
                    return false;
                }

                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Custom enchant verification: Original=" + originalHasCustom + ", Modified=" + modifiedHasCustom);
                }
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error verifying NBT: " + e.getMessage());
            return false;
        }
    }

    /**
     * Apply durability from one item to another while preserving all other NBT.
     * This is used as a fallback if MMOItems API fails.
     * 
     * @param target The item to apply durability to
     * @param source The item with the desired durability
     * @return The target item with updated durability
     */
    public ItemStack applyDurabilityOnly(ItemStack target, ItemStack source) {
        if (target == null || source == null) {
            return target;
        }

        try {
            ItemStack result = cloneSafely(target);

            if (result.hasItemMeta()) {
                ItemMeta meta = result.getItemMeta();
                ItemMeta sourceMeta = source.getItemMeta();

                if (meta instanceof Damageable && sourceMeta instanceof Damageable) {
                    Damageable damageableMeta = (Damageable) meta;
                    Damageable sourceDamageable = (Damageable) sourceMeta;
                    
                    if (sourceDamageable.hasDamage()) {
                        damageableMeta.setDamage(sourceDamageable.getDamage());
                    } else {
                        damageableMeta.setDamage(0);
                    }

                    result.setItemMeta(meta);
                }
            }

            return result;
        } catch (Exception e) {
            plugin.getLogger().warning("Error applying durability: " + e.getMessage());
            return target;
        }
    }

    /**
     * Log NBT verification result for debugging.
     */
    public void logVerification(ItemStack original, ItemStack modified, String operation) {
        if (!plugin.getConfig().getBoolean("settings.debug", false)) {
            return;
        }

        boolean verified = verifyNBT(original, modified);
        if (verified) {
            plugin.getLogger().info("NBT verification passed for " + operation);
        } else {
            plugin.getLogger().warning("NBT verification FAILED for " + operation);
        }
    }
}
