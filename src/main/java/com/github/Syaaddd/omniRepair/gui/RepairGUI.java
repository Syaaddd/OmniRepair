package com.github.Syaaddd.omniRepair.gui;

import com.github.Syaaddd.omniRepair.OmniRepair;
import com.github.Syaaddd.omniRepair.economy.EconomyHandler;
import com.github.Syaaddd.omniRepair.utils.LoreUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Repair GUI for a single player.
 * Handles item preview, cost display, and repair actions.
 */
public class RepairGUI implements InventoryHolder {

    private final OmniRepair plugin;
    private final Player player;
    private final Inventory inventory;
    
    private ItemStack inputItem = null;
    private ItemStack previewItem = null;
    private double currentCost = 0;
    
    private final int inputSlot;
    private final int previewSlot;
    private final int costSlot;
    private final int repairButtonSlot;
    private final int repairAllButtonSlot;
    private final int closeButtonSlot;
    
    private final LegacyComponentSerializer serializer;

    public RepairGUI(OmniRepair plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();

        // Get slot configuration
        inputSlot = plugin.getConfig().getInt("gui.slots.input", 10);
        previewSlot = plugin.getConfig().getInt("gui.slots.preview", 16);
        costSlot = plugin.getConfig().getInt("gui.slots.cost", 28);
        repairButtonSlot = plugin.getConfig().getInt("gui.slots.repair-button", 31);
        repairAllButtonSlot = plugin.getConfig().getInt("gui.slots.repair-all-button", 29);
        closeButtonSlot = plugin.getConfig().getInt("gui.slots.close-button", 33);

        // Create inventory
        int size = plugin.getConfig().getInt("gui.size", 54);
        String title = colorize(plugin.getConfig().getString("gui.title", "&8&l🔨 RPG Mender"));
        inventory = Bukkit.createInventory(this, size, Component.text(title));

        // Initialize GUI
        initializeGUI();
    }

    /**
     * Initialize the GUI with background and buttons.
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

        // Fill all slots except the important ones
        int[] protectedSlots = {inputSlot, previewSlot, costSlot, repairButtonSlot, repairAllButtonSlot, closeButtonSlot};
        
        for (int i = 0; i < inventory.getSize(); i++) {
            boolean protectedSlot = false;
            for (int slot : protectedSlots) {
                if (i == slot) {
                    protectedSlot = true;
                    break;
                }
            }
            
            if (!protectedSlot) {
                inventory.setItem(i, background);
            }
        }
    }

    /**
     * Place the action buttons.
     */
    private void placeButtons() {
        // Repair button
        ItemStack repairButton = createButton(
                plugin.getConfig().getString("gui.buttons.repair", "ANVIL"),
                plugin.getConfig().getString("gui.button-names.repair", "&a&lREPAIR ITEM"),
                plugin.getConfig().getStringList("gui.button-names.repair-lore")
        );
        inventory.setItem(repairButtonSlot, repairButton);

        // Repair All button
        ItemStack repairAllButton = createButton(
                plugin.getConfig().getString("gui.buttons.repair-all", "HOPPER"),
                plugin.getConfig().getString("gui.button-names.repair-all", "&e&lRepair All Inventory"),
                plugin.getConfig().getStringList("gui.button-names.repair-all-lore")
        );
        inventory.setItem(repairAllButtonSlot, repairAllButton);

        // Close button
        ItemStack closeButton = createButton(
                plugin.getConfig().getString("gui.buttons.close", "BARRIER"),
                plugin.getConfig().getString("gui.button-names.close", "&c&lClose"),
                plugin.getConfig().getStringList("gui.button-names.close-lore")
        );
        inventory.setItem(closeButtonSlot, closeButton);
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
     * Set the input item and update preview.
     */
    public void setInputItem(ItemStack item) {
        this.inputItem = item;
        
        if (item == null || item.getType().isAir()) {
            clearPreview();
            return;
        }

        // Check if item can be repaired
        if (!plugin.getItemUtils().canRepair(inputItem)) {
            clearPreview();
            return;
        }

        // Calculate repair cost and create preview
        updatePreview();
    }

    /**
     * Update the preview slot with repaired item and cost.
     */
    private void updatePreview() {
        if (inputItem == null || inputItem.getType().isAir()) {
            clearPreview();
            return;
        }

        // Calculate cost
        currentCost = calculateCost(inputItem);
        
        // Create preview item (clone of input with full durability)
        previewItem = inputItem.clone();
        
        // Set to full durability
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(inputItem)) {
            // MMOItems - just show as repaired in preview
            double maxDurability = plugin.getMmoItemsHook().getMaxDurability(inputItem);
            previewItem = plugin.getLoreUpdater().addPreviewLore(previewItem, maxDurability, maxDurability, currentCost);
        } else {
            // Vanilla - set damage to 0
            ItemMeta meta = previewItem.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(0);
                previewItem.setItemMeta(meta);
            }
            // Add preview lore
            double maxDurability = plugin.getItemUtils().getMaxDurability(inputItem);
            previewItem = plugin.getLoreUpdater().addPreviewLore(previewItem, maxDurability, maxDurability, currentCost);
        }

        // Update preview slot
        inventory.setItem(previewSlot, previewItem);

        // Update cost display
        updateCostDisplay();
    }

    /**
     * Update the cost display slot.
     */
    private void updateCostDisplay() {
        Material costMat = Material.getMaterial(
                plugin.getConfig().getString("gui.buttons.cost-display", "GOLD_BLOCK"));
        if (costMat == null) {
            costMat = Material.GOLD_BLOCK;
        }

        ItemStack costItem = new ItemStack(costMat);
        ItemMeta meta = costItem.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(colorize(plugin.getConfig().getString("gui.cost-display-name", "&6&lRepair Cost")));
            
            List<String> lore = new ArrayList<>();
            String baseCost = plugin.getVaultHook().format(currentCost);
            double multiplier = 1.0;
            if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(inputItem)) {
                multiplier = plugin.getConfig().getDouble("mmoitems.custom-cost-multiplier", 1.5);
            }
            String totalCost = plugin.getVaultHook().format(currentCost);
            
            lore.add(colorize("&7Base Cost: &e" + baseCost));
            if (multiplier > 1.0) {
                lore.add(colorize("&7Multiplier: &ex" + multiplier));
            }
            lore.add(colorize("&7Total: &a" + totalCost));
            
            meta.setLore(lore);
            costItem.setItemMeta(meta);
        }

        inventory.setItem(costSlot, costItem);
    }

    /**
     * Clear the preview slot.
     */
    private void clearPreview() {
        previewItem = null;
        currentCost = 0;
        inventory.setItem(previewSlot, null);
        inventory.setItem(costSlot, null);
    }

    /**
     * Calculate the repair cost for an item.
     */
    private double calculateCost(ItemStack item) {
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
     * Execute the repair operation.
     * @return true if successful, false otherwise
     */
    public boolean executeRepair() {
        if (inputItem == null || previewItem == null) {
            return false;
        }

        if (!plugin.getItemUtils().canRepair(inputItem)) {
            return false;
        }

        // Check if player can afford
        if (!plugin.getEconomyHandler().canAfford(player, currentCost)) {
            return false;
        }

        // Perform repair based on item type
        ItemStack repairedItem;
        
        if (plugin.getMmoItemsHook() != null && plugin.getMmoItemsHook().isMMOItem(inputItem)) {
            repairedItem = plugin.getMmoItemsRepair().repair(inputItem, player);
        } else {
            repairedItem = plugin.getVanillaRepair().repair(inputItem, player);
        }

        if (repairedItem == null) {
            return false;
        }

        // Withdraw payment
        if (!plugin.getEconomyHandler().withdraw(player, currentCost)) {
            return false;
        }

        // Place repaired item in preview slot (player can take it)
        inventory.setItem(previewSlot, repairedItem);
        inputItem = null;
        inventory.setItem(inputSlot, null);
        inventory.setItem(costSlot, null);

        return true;
    }

    /**
     * Get the input item.
     */
    public ItemStack getInputItem() {
        return inputItem;
    }

    /**
     * Get the preview item.
     */
    public ItemStack getPreviewItem() {
        return previewItem;
    }

    /**
     * Get the current repair cost.
     */
    public double getCurrentCost() {
        return currentCost;
    }

    /**
     * Get the repair button slot.
     */
    public int getRepairButtonSlot() {
        return repairButtonSlot;
    }

    /**
     * Get the repair all button slot.
     */
    public int getRepairAllButtonSlot() {
        return repairAllButtonSlot;
    }

    /**
     * Get the close button slot.
     */
    public int getCloseButtonSlot() {
        return closeButtonSlot;
    }

    /**
     * Get the input slot.
     */
    public int getInputSlot() {
        return inputSlot;
    }

    /**
     * Get the preview slot.
     */
    public int getPreviewSlot() {
        return previewSlot;
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
