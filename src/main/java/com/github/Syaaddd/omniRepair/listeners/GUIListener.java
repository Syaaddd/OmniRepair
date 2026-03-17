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

        // Check if player has an item
        if (itemInHand == null || itemInHand.getType().isAir()) {
            sendMessage(player, plugin.getMessages().getString("repair.not-damaged"));
            playErrorSound(player);
            return;
        }

        // Check if item is damaged
        if (!plugin.getItemUtils().isDamaged(itemInHand)) {
            sendMessage(player, plugin.getMessages().getString("repair.not-damaged"));
            playErrorSound(player);
            return;
        }

        // Check if item can be repaired (not blacklisted, not soulbound)
        if (!plugin.getItemUtils().canRepair(itemInHand)) {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
            playErrorSound(player);
            return;
        }

        // Calculate repair cost
        double cost = calculateRepairCost(itemInHand);

        // Check if player can afford
        if (!plugin.getEconomyHandler().canAfford(player, cost)) {
            sendMessage(player, plugin.getMessages().getString("repair.insufficient-funds")
                    .replace("{needed}", plugin.getVaultHook().format(cost))
                    .replace("{balance}", plugin.getVaultHook().getBalanceFormatted(player)));
            playErrorSound(player);
            return;
        }

        // Perform repair based on item type
        ItemStack repairedItem;

        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled() 
                && plugin.getMmoItemsHook().isMMOItem(itemInHand)) {
            repairedItem = plugin.getMmoItemsRepair().repair(itemInHand, player);
        } else {
            repairedItem = plugin.getVanillaRepair().repair(itemInHand, player);
        }

        if (repairedItem == null) {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
            playErrorSound(player);
            return;
        }

        // Withdraw payment
        if (!plugin.getEconomyHandler().withdraw(player, cost)) {
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
}
