package com.github.Syaaddd.omniRepair.commands;

import com.github.Syaaddd.omniRepair.OmniRepair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main command handler for /repair command.
 * Supports subcommands: GUI, hand, all, reload, help.
 */
public class RepairCommand implements CommandExecutor, TabCompleter {

    private final OmniRepair plugin;
    private final LegacyComponentSerializer serializer;

    public RepairCommand(OmniRepair plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                             @NotNull String label, @NotNull String[] args) {
        
        // Check if player is using player-only commands
        if (!(sender instanceof Player player)) {
            if (args.length == 0) {
                sendMessage(sender, plugin.getMessages().getString("general.player-only"));
                return true;
            }
        }

        if (args.length == 0) {
            // Open GUI
            handleGUI(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "hand" -> handleHand(sender);
            case "all" -> handleAll(sender);
            case "reload" -> handleReload(sender);
            case "help" -> handleHelp(sender);
            case "debug" -> handleDebug(sender);
            default -> {
                sendMessage(sender, plugin.getMessages().getString("general.unknown-command"));
                handleHelp(sender);
            }
        }

        return true;
    }

    /**
     * Handle opening the repair GUI.
     */
    private void handleGUI(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, plugin.getMessages().getString("general.player-only"));
            return;
        }

        if (!player.hasPermission("omnirepair.use")) {
            sendMessage(sender, plugin.getMessages().getString("general.no-permission"));
            return;
        }

        plugin.getGuiManager().openGUI(player);
    }

    /**
     * Handle repairing held item.
     */
    private void handleHand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, plugin.getMessages().getString("general.player-only"));
            return;
        }

        if (!player.hasPermission("omnirepair.hand")) {
            sendMessage(sender, plugin.getMessages().getString("general.no-permission"));
            return;
        }

        if (!plugin.getConfig().getBoolean("settings.repair-held-item", true)) {
            sendMessage(sender, "&cInstant hand repair is disabled on this server.");
            return;
        }

        boolean success = plugin.getRepairListener().repairHeldItem(player);
        if (!success) {
            // Error message already sent by repairHeldItem
        }
    }

    /**
     * Handle bulk repair.
     */
    private void handleAll(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, plugin.getMessages().getString("general.player-only"));
            return;
        }

        if (!player.hasPermission("omnirepair.bulk")) {
            sendMessage(sender, plugin.getMessages().getString("general.no-permission"));
            return;
        }

        if (!plugin.getConfig().getBoolean("settings.bulk-repair", true)) {
            sendMessage(sender, "&cBulk repair is disabled on this server.");
            return;
        }

        plugin.getRepairListener().performBulkRepair(player);
    }

    /**
     * Handle config reload.
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("omnirepair.admin")) {
            sendMessage(sender, plugin.getMessages().getString("general.no-permission"));
            return;
        }

        try {
            plugin.reloadConfig();
            plugin.loadMessages();
            sendMessage(sender, plugin.getMessages().getString("general.reload-success"));
        } catch (Exception e) {
            sendMessage(sender, plugin.getMessages().getString("general.reload-failed"));
            plugin.getLogger().severe("Failed to reload config: " + e.getMessage());
        }
    }

    /**
     * Handle help command.
     */
    private void handleHelp(CommandSender sender) {
        sendMessage(sender, plugin.getMessages().getString("general.help-header"));
        sendMessage(sender, plugin.getMessages().getString("general.help-line"));
        
        if (sender.hasPermission("omnirepair.hand")) {
            sendMessage(sender, plugin.getMessages().getString("general.help-line-hand"));
        }
        
        if (sender.hasPermission("omnirepair.bulk")) {
            sendMessage(sender, plugin.getMessages().getString("general.help-line-all"));
        }
        
        if (sender.hasPermission("omnirepair.admin")) {
            sendMessage(sender, plugin.getMessages().getString("general.help-line-reload"));
        }
        
        sendMessage(sender, plugin.getMessages().getString("general.help-footer"));
    }

    /**
     * Handle debug toggle (admin only).
     */
    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("omnirepair.admin")) {
            sendMessage(sender, plugin.getMessages().getString("general.no-permission"));
            return;
        }

        boolean currentDebug = plugin.getConfig().getBoolean("settings.debug", false);
        plugin.getConfig().set("settings.debug", !currentDebug);
        plugin.saveConfig();

        if (!currentDebug) {
            sendMessage(sender, plugin.getMessages().getString("admin.debug-enabled"));
        } else {
            sendMessage(sender, plugin.getMessages().getString("admin.debug-disabled"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            // Always show help
            completions.add("help");
            
            if (sender instanceof Player) {
                if (sender.hasPermission("omnirepair.hand")) {
                    completions.add("hand");
                }
                if (sender.hasPermission("omnirepair.bulk")) {
                    completions.add("all");
                }
            }
            
            if (sender.hasPermission("omnirepair.admin")) {
                completions.add("reload");
                completions.add("debug");
            }
            
            String input = args[0].toLowerCase();
            List<String> filtered = new ArrayList<>();
            for (String completion : completions) {
                if (completion.toLowerCase().startsWith(input)) {
                    filtered.add(completion);
                }
            }
            
            return filtered;
        }
        
        return null;
    }

    /**
     * Send a message to a command sender.
     */
    private void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String prefix = plugin.getMessages().getString("prefix", "&8[&6OmniRepair&8] ");
        sender.sendMessage(Component.text(colorize(prefix + message)));
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
