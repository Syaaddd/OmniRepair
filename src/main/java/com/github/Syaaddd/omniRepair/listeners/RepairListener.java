package com.github.Syaaddd.omniRepair.listeners;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.repair.MMOItemsRepair;
import com.github.Syaaddd.omniRepair.repair.VanillaRepair;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles repair events and bulk repair operations.
 */
public class RepairListener implements Listener {

    private final OmniRepair plugin;

    public RepairListener(OmniRepair plugin) {
        this.plugin = plugin;
    }

    /**
     * Perform bulk repair on all damaged items in a player's inventory.
     */
    public void performBulkRepair(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> itemsToRepair = new ArrayList<>();
        List<ItemStack> repairedItems = new ArrayList<>();
        
        // Collect all damaged items
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir() && plugin.getItemUtils().canRepair(item)) {
                itemsToRepair.add(item);
            }
        }

        // Check max bulk repair limit
        int maxBulkRepair = plugin.getConfig().getInt("settings.max-bulk-repair", 360);
        if (itemsToRepair.size() > maxBulkRepair) {
            sendMessage(player, "&cToo many items to repair! Maximum: " + maxBulkRepair);
            return;
        }

        if (itemsToRepair.isEmpty()) {
            sendMessage(player, plugin.getMessages().getString("repair.no-items-to-repair"));
            return;
        }

        // Calculate total cost
        double totalCost = 0;
        for (ItemStack item : itemsToRepair) {
            if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(item)) {
                totalCost += plugin.getMmoItemsRepair().getRepairCost(item);
            } else {
                totalCost += plugin.getVanillaRepair().getRepairCost(item);
            }
        }

        // Check if player can afford
        if (!plugin.getEconomyHandler().canAfford(player, totalCost)) {
            sendMessage(player, plugin.getMessages().getString("repair.insufficient-funds")
                    .replace("{needed}", plugin.getVaultHook().format(totalCost))
                    .replace("{balance}", plugin.getVaultHook().getBalanceFormatted(player)));
            return;
        }

        // Perform repairs
        int repairedCount = 0;
        for (ItemStack item : itemsToRepair) {
            ItemStack repairedItem = null;
            
            if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(item)) {
                repairedItem = plugin.getMmoItemsRepair().repair(item, player);
            } else {
                repairedItem = plugin.getVanillaRepair().repair(item, player);
            }

            if (repairedItem != null) {
                // Replace the item in inventory
                int slot = getInventorySlot(inventory, item);
                if (slot >= 0) {
                    inventory.setItem(slot, repairedItem);
                    repairedItems.add(repairedItem);
                    repairedCount++;
                }
            }
        }

        // Withdraw payment
        if (repairedCount > 0) {
            plugin.getEconomyHandler().withdraw(player, totalCost);
            
            // Play success sound
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
                        .replace("{cost}", plugin.getEconomyHandler().getCostString(totalCost));
                player.sendActionBar(net.kyori.adventure.text.Component.text(colorize(message)));
            }

            sendMessage(player, plugin.getMessages().getString("repair.success-bulk")
                    .replace("{amount}", String.valueOf(repairedCount))
                    .replace("{cost}", plugin.getEconomyHandler().getCostString(totalCost)));
        } else {
            sendMessage(player, "&cFailed to repair any items!");
        }
    }

    /**
     * Repair a held item instantly.
     */
    public boolean repairHeldItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            sendMessage(player, plugin.getMessages().getString("repair.not-damaged"));
            return false;
        }

        if (!plugin.getItemUtils().canRepair(item)) {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
            return false;
        }

        // Calculate cost
        double cost;
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(item)) {
            cost = plugin.getMmoItemsRepair().getRepairCost(item);
        } else {
            cost = plugin.getVanillaRepair().getRepairCost(item);
        }

        // Check if player can afford
        if (!plugin.getEconomyHandler().canAfford(player, cost)) {
            sendMessage(player, plugin.getMessages().getString("repair.insufficient-funds")
                    .replace("{needed}", plugin.getVaultHook().format(cost))
                    .replace("{balance}", plugin.getVaultHook().getBalanceFormatted(player)));
            return false;
        }

        // Perform repair
        ItemStack repairedItem = null;
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(item)) {
            repairedItem = plugin.getMmoItemsRepair().repair(item, player);
        } else {
            repairedItem = plugin.getVanillaRepair().repair(item, player);
        }

        if (repairedItem != null) {
            // Withdraw payment
            plugin.getEconomyHandler().withdraw(player, cost);
            
            // Set the repaired item
            player.getInventory().setItemInMainHand(repairedItem);

            // Play success sound
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

            sendMessage(player, plugin.getMessages().getString("repair.success")
                    .replace("{cost}", plugin.getEconomyHandler().getCostString(cost)));

            return true;
        }

        return false;
    }

    /**
     * Get the inventory slot of an item.
     */
    private int getInventorySlot(PlayerInventory inventory, ItemStack target) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.isSimilar(target)) {
                return i;
            }
        }
        return -1;
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
}
