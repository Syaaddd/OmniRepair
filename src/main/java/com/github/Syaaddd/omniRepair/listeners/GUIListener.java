package com.github.Syaaddd.omniRepair.listeners;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.gui.RepairGUI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles all GUI-related events.
 * Manages the 3 simplified buttons: Repair Hand, Repair All, Close.
 */
public class GUIListener implements Listener {

    private final OmniRepair plugin;

    public GUIListener(OmniRepair plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        RepairGUI gui = plugin.getGuiManager().getGUI(player);
        if (gui == null) {
            return;
        }

        // Cancel all events in this GUI to prevent item manipulation
        event.setCancelled(true);

        int slot = event.getSlot();

        // Check if clicked on Repair Hand button
        if (slot == gui.getRepairHandSlot()) {
            handleRepairHandClick(player, gui);
            return;
        }

        // Check if clicked on Repair All button
        if (slot == gui.getRepairAllSlot()) {
            handleRepairAllClick(player, gui);
            return;
        }

        // Check if clicked on Close button
        if (slot == gui.getCloseSlot()) {
            player.closeInventory();
            playClickSound(player);
            return;
        }
    }

    /**
     * Handle click on the Repair Hand button.
     * Repairs the item in the player's hand instantly.
     */
    private void handleRepairHandClick(Player player, RepairGUI gui) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if item in offhand if main hand is air
        if (itemInHand == null || itemInHand.getType().isAir()) {
            itemInHand = player.getInventory().getItemInOffHand();
        }

        // Debug logging
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Repair Hand clicked by " + player.getName());
            plugin.getLogger().info("[DEBUG] Item in hand: " + (itemInHand != null ? itemInHand.getType().name() : "null"));
            if (itemInHand != null) {
                plugin.getLogger().info("[DEBUG] Item display name: " + (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName() ? itemInHand.getItemMeta().getDisplayName() : "none"));
            }
        }

        // Check if player has an item
        if (itemInHand == null || itemInHand.getType().isAir()) {
            sendMessage(player, plugin.getMessages().getString("repair.not-damaged"));
            playErrorSound(player);
            return;
        }

        // Check if item is damaged
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Checking if item is damaged...");
        }
        
        // For MMOItems, check if it's a valid MMOItem
        // For Vanilla items, use ItemUtils.isDamaged()
        boolean isDamaged;
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled() 
                && plugin.getMmoItemsHook().isMMOItem(itemInHand)) {
            // MMOItems item - if it's valid MMOItem, allow repair
            isDamaged = hasDurabilityStat(itemInHand);
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] MMOItem validity check: " + isDamaged);
            }
        } else {
            // Vanilla item - use normal damage check from ItemUtils
            isDamaged = plugin.getItemUtils().isDamaged(itemInHand);
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] isDamaged result (vanilla): " + isDamaged);
            }
        }
        
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Final isDamaged result: " + isDamaged);
            plugin.getLogger().info("[DEBUG] Checking if item can be repaired...");
        }

        if (!isDamaged) {
            sendMessage(player, plugin.getMessages().getString("repair.not-damaged"));
            playErrorSound(player);
            return;
        }

        // For MMOItems, skip canRepair check since we already validated it
        // For vanilla items, check blacklist and soulbound
        boolean canRepair;
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled() 
                && plugin.getMmoItemsHook().isMMOItem(itemInHand)) {
            // MMOItems - already validated, check only blacklist
            canRepair = !isBlacklisted(itemInHand);
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] MMOItem blacklist check: " + !canRepair);
            }
        } else {
            // Vanilla - full check
            canRepair = plugin.getItemUtils().canRepair(itemInHand);
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] canRepair result: " + canRepair);
            }
        }

        if (!canRepair) {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
            playErrorSound(player);
            return;
        }

        // Calculate repair cost
        double cost = calculateRepairCost(itemInHand);

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Repair cost: " + cost);
        }

        // Check if player can afford
        boolean canAfford = plugin.getEconomyHandler().canAfford(player, cost);
        
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Can afford: " + canAfford + " (cost: " + cost + ")");
            plugin.getLogger().info("[DEBUG] Economy enabled: " + plugin.getEconomyHandler().isUsingEconomy());
            plugin.getLogger().info("[DEBUG] Player balance: " + plugin.getVaultHook().getBalanceFormatted(player));
        }
        
        if (!canAfford) {
            sendMessage(player, plugin.getMessages().getString("repair.insufficient-funds")
                    .replace("{needed}", plugin.getVaultHook().format(cost))
                    .replace("{balance}", plugin.getVaultHook().getBalanceFormatted(player)));
            playErrorSound(player);
            return;
        }

        // Perform repair based on item type
        ItemStack repairedItem;

        try {
            if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()
                    && plugin.getMmoItemsHook().isMMOItem(itemInHand)) {

                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Using MMOItems repair");
                }

                repairedItem = plugin.getMmoItemsRepair().repair(itemInHand, player);
            } else {

                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("[DEBUG] Using Vanilla repair");
                }

                repairedItem = plugin.getVanillaRepair().repair(itemInHand, player);
            }
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().severe("[DEBUG] Repair exception: " + e.getMessage());
                e.printStackTrace();
            }
            sendMessage(player, "&cAn error occurred while repairing. Check console.");
            playErrorSound(player);
            return;
        }

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Repaired item result: " + (repairedItem != null ? "success" : "null"));
        }

        if (repairedItem == null) {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
            playErrorSound(player);
            return;
        }

        // Withdraw payment
        boolean withdrawn = plugin.getEconomyHandler().withdraw(player, cost);
        
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] Withdraw result: " + withdrawn);
            plugin.getLogger().info("[DEBUG] Player balance after: " + plugin.getVaultHook().getBalanceFormatted(player));
        }
        
        if (!withdrawn) {
            sendMessage(player, plugin.getMessages().getString("repair.insufficient-funds"));
            playErrorSound(player);
            return;
        }

        // Success!
        player.getInventory().setItemInMainHand(repairedItem);

        // Play success effects
        playSuccessEffects(player, cost);

        sendMessage(player, plugin.getMessages().getString("repair.success")
                .replace("{cost}", plugin.getEconomyHandler().getCostString(cost)));
    }

    /**
     * Handle click on the Repair All button.
     * Repairs all damaged items in player's inventory.
     */
    private void handleRepairAllClick(Player player, RepairGUI gui) {
        if (!player.hasPermission("omnirepair.bulk")) {
            sendMessage(player, plugin.getMessages().getString("general.no-permission"));
            playErrorSound(player);
            return;
        }

        if (!plugin.getConfig().getBoolean("settings.bulk-repair", true)) {
            sendMessage(player, "&cBulk repair is disabled on this server.");
            playErrorSound(player);
            return;
        }

        // Close the GUI first
        player.closeInventory();

        // Perform bulk repair
        plugin.getRepairListener().performBulkRepair(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        RepairGUI gui = plugin.getGuiManager().getGUI(player);
        if (gui != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        RepairGUI gui = plugin.getGuiManager().getGUI(player);
        if (gui != null) {
            plugin.getGuiManager().removeGUI(player);
        }
    }

    /**
     * Calculate the repair cost for an item.
     */
    private double calculateRepairCost(ItemStack item) {
        double damagePercent = plugin.getItemUtils().getDamagePercent(item);
        if (damagePercent < 0) {
            return 0;
        }

        double baseCost = damagePercent * plugin.getConfig().getDouble("settings.cost-per-percent", 10.0);

        // Apply MMOItems multiplier
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(item)) {
            baseCost *= plugin.getConfig().getDouble("mmoitems.custom-cost-multiplier", 1.5);
        }

        // Apply min/max
        baseCost = Math.max(baseCost, plugin.getConfig().getDouble("settings.min-cost", 5.0));
        baseCost = Math.min(baseCost, plugin.getConfig().getDouble("settings.max-cost", 5000.0));

        return baseCost;
    }

    /**
     * Play success effects (sound, particles, action bar).
     */
    private void playSuccessEffects(Player player, double cost) {
        // Play sound
        if (plugin.getConfig().getBoolean("effects.sound.enabled", true)) {
            String soundName = plugin.getConfig().getString("effects.sound.type", "BLOCK_ANVIL_USE");
            try {
                Sound sound = Sound.valueOf(soundName);
                float volume = (float) plugin.getConfig().getDouble("effects.sound.volume", 1.0);
                float pitch = (float) plugin.getConfig().getDouble("effects.sound.pitch", 1.0);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound type: " + soundName);
            }
        }

        // Spawn particles
        if (plugin.getConfig().getBoolean("effects.particles.enabled", true)) {
            spawnParticles(player);
        }

        // Send action bar message
        if (plugin.getConfig().getBoolean("effects.action-bar.enabled", true)) {
            String message = plugin.getMessages().getString("action-bar.repair-success")
                    .replace("{cost}", plugin.getEconomyHandler().getCostString(cost));
            player.sendActionBar(net.kyori.adventure.text.Component.text(colorize(message)));
        }
    }

    /**
     * Spawn repair success particles.
     */
    private void spawnParticles(Player player) {
        String particleName = plugin.getConfig().getString("effects.particles.type", "VILLAGER_HAPPY");
        int count = plugin.getConfig().getInt("effects.particles.count", 10);
        double offsetX = plugin.getConfig().getDouble("effects.particles.offset-x", 0.5);
        double offsetY = plugin.getConfig().getDouble("effects.particles.offset-y", 0.5);
        double offsetZ = plugin.getConfig().getDouble("effects.particles.offset-z", 0.5);
        double speed = plugin.getConfig().getDouble("effects.particles.speed", 0.5);

        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleName);
            player.getWorld().spawnParticle(
                    particle,
                    player.getLocation().add(0, 1, 0),
                    count,
                    offsetX, offsetY, offsetZ,
                    speed
            );
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type: " + particleName);
        }
    }

    /**
     * Play click sound.
     */
    private void playClickSound(Player player) {
        try {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Play error sound.
     */
    private void playErrorSound(Player player) {
        try {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Send a message to a player.
     */
    private void sendMessage(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        String prefix = plugin.getMessages().getString("prefix", "&8[&6OmniRepair&8] ");
        player.sendMessage(plugin.getLoreUpdater().colorize(prefix + message));
    }

    /**
     * Colorize a string.
     */
    private String colorize(String text) {
        return plugin.getLoreUpdater().colorize(text);
    }

    /**
     * Check if an MMOItem has durability stat.
     */
    private boolean hasDurabilityStat(ItemStack item) {
        try {
            net.Indyuce.mmoitems.api.Type type = net.Indyuce.mmoitems.MMOItems.getType(item);
            String id = net.Indyuce.mmoitems.MMOItems.getID(item);

            if (type == null || id == null) {
                return false;
            }

            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] hasDurabilityStat: Checking " + type.getId() + ":" + id);
            }

            // Get the template item from MMOItems
            ItemStack template = net.Indyuce.mmoitems.MMOItems.plugin.getItem(type, id);
            
            if (template == null) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().warning("[DEBUG] hasDurabilityStat: Template is null!");
                }
                return false;
            }

            // Compare NBT - if the item has different NBT than template, it might be damaged
            // This works because damaged items have modified durability NBT
            ItemMeta itemMeta = item.getItemMeta();
            ItemMeta templateMeta = template.getItemMeta();
            
            if (itemMeta == null || templateMeta == null) {
                return false;
            }

            // Check PersistentDataContainer for any durability-related data
            org.bukkit.persistence.PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
            
            // If item has any PDC data that template doesn't have, it might be modified
            // Or we can just assume any MMOItem with custom NBT can be "repaired" by getting fresh template
            boolean hasCustomNBT = !itemPDC.isEmpty();
            
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] hasDurabilityStat: Has custom NBT: " + hasCustomNBT);
                plugin.getLogger().info("[DEBUG] hasDurabilityStat: Item PDC keys: " + itemPDC.getKeys());
            }

            // Simple approach: if it's a valid MMOItem, allow repair
            // The repair will give them a fresh template with full durability
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("[DEBUG] hasDurabilityStat: Valid MMOItem, allowing repair");
            }
            return true;

        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[DEBUG] hasDurabilityStat error: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Check if an MMOItem is blacklisted.
     */
    private boolean isBlacklisted(ItemStack item) {
        try {
            String id = net.Indyuce.mmoitems.MMOItems.getID(item);
            if (id == null) {
                return false;
            }

            // Check MMOItems ID blacklist from config
            java.util.List<String> blacklist = plugin.getConfig().getStringList("blacklist.mmoitems-ids");
            for (String blacklistedId : blacklist) {
                if (blacklistedId.equalsIgnoreCase(id)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
