package com.github.Syaaddd.omniRepair.listeners;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.gui.RepairGUI;
import com.github.Syaaddd.omniRepair.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
 * Manages item placement, preview updates, and repair actions.
 */
public class GUIListener implements Listener {

    private final OmniRepair plugin;
    private final LegacyComponentSerializer serializer;

    public GUIListener(OmniRepair plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
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
        ItemStack clickedItem = event.getCurrentItem();

        // Check if clicked on repair button
        if (slot == gui.getRepairButtonSlot()) {
            handleRepairClick(player, gui);
            return;
        }

        // Check if clicked on repair all button
        if (slot == gui.getRepairAllButtonSlot()) {
            handleRepairAllClick(player, gui);
            return;
        }

        // Check if clicked on close button
        if (slot == gui.getCloseButtonSlot()) {
            player.closeInventory();
            return;
        }

        // Check if clicked on input slot
        if (slot == gui.getInputSlot()) {
            handleInputSlotClick(event, player, gui);
            return;
        }

        // Check if clicked on preview slot (prevent taking item before repair)
        if (slot == gui.getPreviewSlot()) {
            handlePreviewSlotClick(player, gui);
            return;
        }
    }

    /**
     * Handle click on the repair button.
     */
    private void handleRepairClick(Player player, RepairGUI gui) {
        ItemStack inputItem = gui.getInputItem();
        
        if (inputItem == null || inputItem.getType().isAir()) {
            sendMessage(player, plugin.getMessages().getString("repair.not-damaged"));
            return;
        }

        if (!plugin.getItemUtils().canRepair(inputItem)) {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
            return;
        }

        double cost = gui.getCurrentCost();
        
        // Check if player can afford
        if (!plugin.getEconomyHandler().canAfford(player, cost)) {
            sendMessage(player, plugin.getMessages().getString("repair.insufficient-funds")
                    .replace("{needed}", plugin.getVaultHook().format(cost))
                    .replace("{balance}", plugin.getVaultHook().getBalanceFormatted(player)));
            return;
        }

        // Execute repair
        boolean success = gui.executeRepair();
        
        if (success) {
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
                player.sendActionBar(Component.text(colorize(message)));
            }

            sendMessage(player, plugin.getMessages().getString("repair.success")
                    .replace("{cost}", plugin.getEconomyHandler().getCostString(cost)));
        } else {
            sendMessage(player, plugin.getMessages().getString("repair.blacklisted"));
        }
    }

    /**
     * Handle click on the repair all button.
     */
    private void handleRepairAllClick(Player player, RepairGUI gui) {
        if (!player.hasPermission("omnirepair.bulk")) {
            sendMessage(player, plugin.getMessages().getString("general.no-permission"));
            return;
        }

        if (!plugin.getConfig().getBoolean("settings.bulk-repair", true)) {
            sendMessage(player, "&cBulk repair is disabled on this server.");
            return;
        }

        // Close the GUI first
        player.closeInventory();

        // Perform bulk repair
        plugin.getRepairListener().performBulkRepair(player);
    }

    /**
     * Handle click on the input slot.
     */
    private void handleInputSlotClick(InventoryClickEvent event, Player player, RepairGUI gui) {
        ItemStack cursor = event.getCursor();
        ItemStack newInput = cursor != null ? cursor.clone() : null;

        // Update GUI with new input item
        gui.setInputItem(newInput);
        
        // Update the input slot display
        if (cursor != null && !cursor.getType().isAir()) {
            event.getInventory().setItem(gui.getInputSlot(), cursor.clone());
        } else {
            event.getInventory().setItem(gui.getInputSlot(), null);
        }
    }

    /**
     * Handle click on the preview slot.
     */
    private void handlePreviewSlotClick(Player player, RepairGUI gui) {
        // Only allow taking the item after repair
        ItemStack previewItem = gui.getPreviewItem();
        ItemStack inputItem = gui.getInputItem();
        
        if (inputItem != null && !inputItem.getType().isAir()) {
            // Item not repaired yet
            sendMessage(player, plugin.getMessages().getString("gui.preview-error"));
        } else if (previewItem != null && !previewItem.getType().isAir()) {
            // Item is repaired, allow taking
            // The item will be taken naturally since we don't cancel in this case
        }
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
            // Return input item to player if still in GUI
            ItemStack inputItem = gui.getInputItem();
            if (inputItem != null && !inputItem.getType().isAir()) {
                // Give the item back to player
                player.getInventory().addItem(inputItem);
            }

            plugin.getGuiManager().removeGUI(player);
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
     * Send a message to a player.
     */
    private void sendMessage(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String prefix = plugin.getMessages().getString("prefix", "&8[&6OmniRepair&8] ");
        player.sendMessage(Component.text(colorize(prefix + message)));
    }

    /**
     * Colorize a string.
     */
    private String colorize(String text) {
        if (text == null) {
            return "";
        }
        Component component = serializer.deserialize(text);
        return serializer.serialize(component);
    }
}
