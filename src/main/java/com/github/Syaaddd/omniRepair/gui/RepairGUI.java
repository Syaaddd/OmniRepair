package com.github.Syaaddd.omniRepair.gui;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified Repair GUI with only 3 buttons:
 * - Repair Hand (repair item in hand)
 * - Repair All Inventory (repair all damaged items)
 * - Close GUI
 */
public class RepairGUI implements InventoryHolder {

    private final OmniRepair plugin;
    private final Player player;
    private final Inventory inventory;

    // Button slots
    private final int repairHandSlot;
    private final int repairAllSlot;
    private final int closeSlot;

    public RepairGUI(OmniRepair plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        // Get slot configuration
        repairHandSlot = plugin.getConfig().getInt("gui.slots.repair-hand", 10);
        repairAllSlot = plugin.getConfig().getInt("gui.slots.repair-all", 12);
        closeSlot = plugin.getConfig().getInt("gui.slots.close", 4);

        // Create inventory - smaller size for simplified GUI
        int size = plugin.getConfig().getInt("gui.size", 27);
        String title = plugin.getLoreUpdater().colorize(
                plugin.getConfig().getString("gui.title", "&8&l🔨 RPG Mender"));
        inventory = Bukkit.createInventory(this, size, net.kyori.adventure.text.Component.text(title));

        // Initialize GUI
        initializeGUI();
    }

    /**
     * Initialize the GUI with buttons.
     */
    private void initializeGUI() {
        // Fill background
        fillBackground();

        // Place buttons
        placeButtons();
    }

    /**
     * Fill the background with glass panes.
     */
    private void fillBackground() {
        Material backgroundMat = Material.getMaterial(
                plugin.getConfig().getString("gui.background", "BLACK_STAINED_GLASS_PANE"));
        if (backgroundMat == null) {
            backgroundMat = Material.BLACK_STAINED_GLASS_PANE;
        }

        ItemStack background = new ItemStack(backgroundMat);
        ItemMeta meta = background.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(plugin.getConfig().getString("gui.background-name", " ")));
            background.setItemMeta(meta);
        }

        // Fill all slots except button slots
        int[] buttonSlots = {repairHandSlot, repairAllSlot, closeSlot};

        for (int i = 0; i < inventory.getSize(); i++) {
            boolean isButtonSlot = false;
            for (int slot : buttonSlots) {
                if (i == slot) {
                    isButtonSlot = true;
                    break;
                }
            }

            if (!isButtonSlot) {
                inventory.setItem(i, background);
            }
        }
    }

    /**
     * Place the action buttons.
     */
    private void placeButtons() {
        // Repair Hand button
        ItemStack repairHandButton = createButton(
                plugin.getConfig().getString("gui.buttons.repair-hand", "ANVIL"),
                plugin.getConfig().getString("gui.button-names.repair-hand", "&a&lRepair Hand"),
                plugin.getConfig().getStringList("gui.button-names.repair-hand-lore")
        );
        inventory.setItem(repairHandSlot, repairHandButton);

        // Repair All button
        ItemStack repairAllButton = createButton(
                plugin.getConfig().getString("gui.buttons.repair-all", "HOPPER"),
                plugin.getConfig().getString("gui.button-names.repair-all", "&e&lRepair All Inventory"),
                plugin.getConfig().getStringList("gui.button-names.repair-all-lore")
        );
        inventory.setItem(repairAllSlot, repairAllButton);

        // Close button
        ItemStack closeButton = createButton(
                plugin.getConfig().getString("gui.buttons.close", "BARRIER"),
                plugin.getConfig().getString("gui.button-names.close", "&c&lClose"),
                plugin.getConfig().getStringList("gui.button-names.close-lore")
        );
        inventory.setItem(closeSlot, closeButton);
    }

    /**
     * Create a button item with name and lore.
     */
    private ItemStack createButton(String materialName, String displayName, List<String> lore) {
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            material = Material.ANVIL;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(displayName));

            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(colorize(line));
                }
                meta.setLore(coloredLore);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Get the repair hand button slot.
     */
    public int getRepairHandSlot() {
        return repairHandSlot;
    }

    /**
     * Get the repair all button slot.
     */
    public int getRepairAllSlot() {
        return repairAllSlot;
    }

    /**
     * Get the close button slot.
     */
    public int getCloseSlot() {
        return closeSlot;
    }

    /**
     * Colorize a string.
     */
    private String colorize(String text) {
        return plugin.getLoreUpdater().colorize(text);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Open the GUI for the player.
     */
    public void open() {
        player.openInventory(inventory);
    }
}
