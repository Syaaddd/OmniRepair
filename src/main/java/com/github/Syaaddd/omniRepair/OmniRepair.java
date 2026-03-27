package com.github.Syaaddd.omniRepair;

import com.github.Syaaddd.omniRepair.commands.RepairCommand;
import com.github.Syaaddd.omniRepair.economy.EconomyHandler;
import com.github.Syaaddd.omniRepair.gui.GUIManager;
import com.github.Syaaddd.omniRepair.integration.MMOItemsHook;
import com.github.Syaaddd.omniRepair.integration.VaultHook;
import com.github.Syaaddd.omniRepair.integration.CustomEnchantHook;
import com.github.Syaaddd.omniRepair.listeners.GUIListener;
import com.github.Syaaddd.omniRepair.listeners.RepairListener;
import com.github.Syaaddd.omniRepair.repair.MMOItemsRepair;
import com.github.Syaaddd.omniRepair.repair.VanillaRepair;
import com.github.Syaaddd.omniRepair.utils.ItemUtils;
import com.github.Syaaddd.omniRepair.utils.LoreUpdater;
import com.github.Syaaddd.omniRepair.utils.NBTProtection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * OmniRepair - Repair Vanilla & RPG Items Safely. MMOItems Supported.
 * 
 * @author Syaaddd
 * @version 1.0.0-SNAPSHOT
 */
public final class OmniRepair extends JavaPlugin {

    // Integration hooks
    private MMOItemsHook mmoItemsHook;
    private VaultHook vaultHook;
    private CustomEnchantHook customEnchantHook;

    // Utility classes
    private ItemUtils itemUtils;
    private NBTProtection nbtProtection;
    private LoreUpdater loreUpdater;

    // Repair handlers
    private VanillaRepair vanillaRepair;
    private MMOItemsRepair mmoItemsRepair;

    // Economy
    private EconomyHandler economyHandler;

    // GUI
    private GUIManager guiManager;

    // Listeners
    private GUIListener guiListener;
    private RepairListener repairListener;

    // Commands
    private RepairCommand repairCommand;

    // Messages configuration
    private FileConfiguration messagesConfig;
    private File messagesConfigFile;

    @Override
    public void onEnable() {
        // Log startup message
        getLogger().info("╔════════════════════════════════════════════════════════════════╗");
        getLogger().info("#                    OmniRepair                                    #");
        getLogger().info("#     \"Repair Vanilla & RPG Items Safely. MMOItems Supported.\"   #");
        getLogger().info("╚════════════════════════════════════════════════════════════════╝");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: Syaaddd");
        getLogger().info("");

        // Save default configs
        saveDefaultConfig();
        saveMessagesConfig();

        // Initialize integration hooks
        initializeHooks();

        // Initialize utility classes
        initializeUtilities();

        // Initialize repair handlers
        initializeRepairHandlers();

        // Initialize economy
        initializeEconomy();

        // Initialize GUI
        initializeGUI();

        // Initialize listeners
        initializeListeners();

        // Initialize commands
        initializeCommands();

        // Log enabled message
        getLogger().info("");
        getLogger().info("✓ OmniRepair enabled successfully!");
        getLogger().info("  - Economy: " + (economyHandler.isUsingEconomy() ? "Enabled" : "Disabled"));
        getLogger().info("  - MMOItems: " + (mmoItemsHook.isEnabled() ? "Enabled" : "Disabled"));
        getLogger().info("  - AdvancedEnchantments: " + (customEnchantHook.isEnabled() ? "Enabled" : "Disabled"));
        getLogger().info("  - Vault: " + (vaultHook.isEnabled() ? "Enabled" : "Disabled"));
        getLogger().info("  - Use /repair to open the GUI");
        getLogger().info("╚════════════════════════════════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        // Close all open GUIs
        if (guiManager != null) {
            guiManager.closeAll();
        }

        // Log shutdown message
        getLogger().info("OmniRepair disabled.");
    }

    /**
     * Initialize integration hooks.
     */
    private void initializeHooks() {
        mmoItemsHook = new MMOItemsHook(this);
        vaultHook = new VaultHook(this);
        customEnchantHook = new CustomEnchantHook(this);
    }

    /**
     * Initialize utility classes.
     */
    private void initializeUtilities() {
        itemUtils = new ItemUtils(this);
        nbtProtection = new NBTProtection(this);
        loreUpdater = new LoreUpdater(this);
    }

    /**
     * Initialize repair handlers.
     */
    private void initializeRepairHandlers() {
        vanillaRepair = new VanillaRepair(this);
        mmoItemsRepair = new MMOItemsRepair(this);
    }

    /**
     * Initialize economy handler.
     */
    private void initializeEconomy() {
        economyHandler = new EconomyHandler(this);
    }

    /**
     * Initialize GUI manager.
     */
    private void initializeGUI() {
        guiManager = new GUIManager(this);
    }

    /**
     * Initialize listeners.
     */
    private void initializeListeners() {
        guiListener = new GUIListener(this);
        repairListener = new RepairListener(this);

        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(repairListener, this);

        getLogger().info("✓ Listeners registered");
    }

    /**
     * Initialize commands.
     */
    private void initializeCommands() {
        repairCommand = new RepairCommand(this);

        getCommand("repair").setExecutor(repairCommand);
        getCommand("repair").setTabCompleter(repairCommand);

        getLogger().info("✓ Commands registered");
    }

    /**
     * Save the default messages configuration.
     */
    private void saveMessagesConfig() {
        messagesConfigFile = new File(getDataFolder(), "messages.yml");
        
        if (!messagesConfigFile.exists()) {
            try (InputStream in = getResource("messages.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, messagesConfigFile.toPath());
                    getLogger().info("✓ Created default messages.yml");
                }
            } catch (IOException e) {
                getLogger().severe("Could not create messages.yml: " + e.getMessage());
            }
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
        
        // Update defaults if needed
        try (InputStream in = getResource("messages.yml")) {
            if (in != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                messagesConfig.setDefaults(defaultConfig);
            }
        } catch (IOException e) {
            getLogger().warning("Could not update messages.yml defaults: " + e.getMessage());
        }
    }

    /**
     * Load messages configuration from file.
     */
    public void loadMessages() {
        if (messagesConfigFile != null && messagesConfigFile.exists()) {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
            
            // Update defaults
            try (InputStream in = getResource("messages.yml")) {
                if (in != null) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                    messagesConfig.setDefaults(defaultConfig);
                }
            } catch (IOException e) {
                getLogger().warning("Could not update messages.yml defaults: " + e.getMessage());
            }
            
            getLogger().info("✓ Messages configuration reloaded");
        }
    }

    // ==================== GETTERS ====================

    /**
     * Get the MMOItems integration hook.
     */
    public MMOItemsHook getMmoItemsHook() {
        return mmoItemsHook;
    }

    /**
     * Get the Vault integration hook.
     */
    public VaultHook getVaultHook() {
        return vaultHook;
    }

    /**
     * Get the AdvancedEnchantments integration hook.
     */
    public CustomEnchantHook getCustomEnchantHook() {
        return customEnchantHook;
    }

    /**
     * Get the item utilities.
     */
    public ItemUtils getItemUtils() {
        return itemUtils;
    }

    /**
     * Get the NBT protection utility.
     */
    public NBTProtection getNBTProtection() {
        return nbtProtection;
    }

    /**
     * Get the lore updater utility.
     */
    public LoreUpdater getLoreUpdater() {
        return loreUpdater;
    }

    /**
     * Get the vanilla repair handler.
     */
    public VanillaRepair getVanillaRepair() {
        return vanillaRepair;
    }

    /**
     * Get the MMOItems repair handler.
     */
    public MMOItemsRepair getMmoItemsRepair() {
        return mmoItemsRepair;
    }

    /**
     * Get the economy handler.
     */
    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    /**
     * Get the GUI manager.
     */
    public GUIManager getGuiManager() {
        return guiManager;
    }

    /**
     * Get the GUI listener.
     */
    public GUIListener getGuiListener() {
        return guiListener;
    }

    /**
     * Get the repair listener.
     */
    public RepairListener getRepairListener() {
        return repairListener;
    }

    /**
     * Get the repair command.
     */
    public RepairCommand getRepairCommand() {
        return repairCommand;
    }

    /**
     * Get the messages configuration.
     */
    public FileConfiguration getMessages() {
        return messagesConfig;
    }

    /**
     * Get a message string and colorize it.
     */
    public String getMessage(String path) {
        String message = messagesConfig.getString(path, "");
        return loreUpdater.colorize(message);
    }

    /**
     * Colorize a string using legacy color codes.
     */
    public String colorize(String text) {
        return loreUpdater.colorize(text);
    }
}
