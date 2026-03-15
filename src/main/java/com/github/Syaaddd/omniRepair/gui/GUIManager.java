package com.github.Syaaddd.omniRepair.gui;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all active Repair GUIs.
 * Tracks which player has which GUI open.
 */
public class GUIManager {

    private final OmniRepair plugin;
    private final Map<UUID, RepairGUI> activeGUIs;

    public GUIManager(OmniRepair plugin) {
        this.plugin = plugin;
        this.activeGUIs = new HashMap<>();
    }

    /**
     * Open a repair GUI for a player.
     */
    public void openGUI(Player player) {
        // Close any existing GUI for this player
        closeGUI(player);

        // Create and open new GUI
        RepairGUI gui = new RepairGUI(plugin, player);
        activeGUIs.put(player.getUniqueId(), gui);
        gui.open();

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("Opened repair GUI for " + player.getName());
        }
    }

    /**
     * Close a player's GUI.
     */
    public void closeGUI(Player player) {
        RepairGUI gui = activeGUIs.remove(player.getUniqueId());
        if (gui != null) {
            player.closeInventory();
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().info("Closed repair GUI for " + player.getName());
            }
        }
    }

    /**
     * Get the GUI for a player.
     */
    public RepairGUI getGUI(Player player) {
        return activeGUIs.get(player.getUniqueId());
    }

    /**
     * Check if a player has an active GUI.
     */
    public boolean hasGUI(Player player) {
        return activeGUIs.containsKey(player.getUniqueId());
    }

    /**
     * Remove a player's GUI (called when inventory closes).
     */
    public void removeGUI(Player player) {
        activeGUIs.remove(player.getUniqueId());
    }

    /**
     * Get the number of active GUIs.
     */
    public int getActiveCount() {
        return activeGUIs.size();
    }

    /**
     * Close all active GUIs (for server shutdown or reload).
     */
    public void closeAll() {
        for (Map.Entry<UUID, RepairGUI> entry : activeGUIs.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.closeInventory();
            }
        }
        activeGUIs.clear();
    }
}
