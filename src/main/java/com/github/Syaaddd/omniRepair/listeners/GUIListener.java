package com.github.Syaaddd.omniRepair.listeners;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.gui.RepairGUI;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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
        boolean isOffHand = false;

        // Check if item in offhand if main hand is air
        if (itemInHand == null || itemInHand.getType().isAir()) {
            itemInHand = player.getInventory().getItemInOffHand();
            isOffHand = true;
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

        // Check if item can be repaired (damaged, not blacklisted, not soulbound)
        boolean canRepair = plugin.getItemUtils().canRepair(itemInHand);

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] canRepair result: " + canRepair);
        }

        if (!canRepair) {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
            playErrorSound(player);
            return;
        }

        // Calculate repair cost using handler (consistent with repair logic)
        double cost;
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isEnabled()
                && plugin.getMmoItemsHook().isMMOItem(itemInHand)) {
            cost = plugin.getMmoItemsRepair().getRepairCost(itemInHand);
        } else {
            cost = plugin.getVanillaRepair().getRepairCost(itemInHand);
        }

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

        // Success! Set back to the hand the item came from
        if (isOffHand) {
            player.getInventory().setItemInOffHand(repairedItem);
        } else {
            player.getInventory().setItemInMainHand(repairedItem);
        }

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
     * Play success effects (sound, particles, action bar).
     */
    private void playSuccessEffects(Player player, double cost) {
        // Play sound
        if (plugin.getConfig().getBoolean("effects.sound.enabled", true)) {
            String soundName = plugin.getConfig().getString("effects.sound.type", "BLOCK_ANVIL_USE");
            try {
                Sound sound = resolveSound(soundName);
                if (sound != null) {
                    float volume = (float) plugin.getConfig().getDouble("effects.sound.volume", 1.0);
                    float pitch = (float) plugin.getConfig().getDouble("effects.sound.pitch", 1.0);
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } else {
                    plugin.getLogger().warning("Invalid sound type: " + soundName);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error playing sound " + soundName + ": " + e.getMessage());
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
     * Resolve a Sound by name, supporting both modern namespaced keys and legacy enum names.
     */
    private Sound resolveSound(String name) {
        if (name == null || name.isEmpty()) return null;

        // Try modern registry lookup (e.g. "minecraft:block.anvil.use")
        String keyStr = name.contains(":") ? name : "minecraft:" + name.toLowerCase().replace('_', '.');
        NamespacedKey key = NamespacedKey.fromString(keyStr);
        if (key != null) {
            Sound sound = Registry.SOUND_EVENT.get(key);
            if (sound != null) return sound;
        }

        // Fallback to legacy enum lookup (e.g. "BLOCK_ANVIL_USE")
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
