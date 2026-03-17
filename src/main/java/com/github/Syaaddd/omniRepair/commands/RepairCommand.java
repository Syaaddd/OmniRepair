package com.github.Syaaddd.omniRepair.commands;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Main command handler for /repair command.
 * 
 * Usage:
 * - Players: /repair (opens GUI for themselves)
 * - Console/Admin: /repair <player> (opens GUI for specified player)
 * - Admin: /repair reload, /repair debug, /repair help
 * 
 * Note: Repair is GUI-only. Direct repair commands (hand, all) are removed.
 */
public class RepairCommand implements CommandExecutor, TabCompleter {

    private final OmniRepair plugin;

    public RepairCommand(OmniRepair plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            // No arguments: open GUI for sender (must be player)
            handleGUI(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Check for admin commands
        if (subCommand.equals("reload")) {
            handleReload(sender);
            return true;
        }

        if (subCommand.equals("debug")) {
            handleDebug(sender);
            return true;
        }

        if (subCommand.equals("help")) {
            handleHelp(sender);
            return true;
        }

        // If sender is console or admin with permission, treat first arg as player name
        if (!(sender instanceof Player) || sender.hasPermission("omnirepair.admin")) {
            handleOpenGUIForPlayer(sender, args[0]);
            return true;
        }

        // Player trying to use unknown subcommand
        sendMessage(sender, plugin.getMessages().getString("general.unknown-command"));
        handleHelp(sender);
        return true;
    }

    /**
     * Handle opening the repair GUI for the sender (must be a player).
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
     * Handle opening the repair GUI for a specified player.
     * Can be used by console or admins.
     */
    private void handleOpenGUIForPlayer(CommandSender sender, String targetName) {
        if (targetName == null || targetName.isEmpty()) {
            sendMessage(sender, plugin.getMessages().getString("general.no-player-specified"));
            return;
        }

        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sendMessage(sender, plugin.getMessages().getString("general.player-not-found")
                    .replace("{player}", targetName));
            return;
        }

        if (!target.hasPermission("omnirepair.use")) {
            sendMessage(sender, plugin.getMessages().getString("general.target-no-permission")
                    .replace("{player}", target.getName()));
            return;
        }

        plugin.getGuiManager().openGUI(target);

        // Notify sender
        if (sender instanceof Player) {
            sendMessage(sender, plugin.getMessages().getString("general.gui-opened-other")
                    .replace("{player}", target.getName()));
        } else {
            plugin.getLogger().info("Opened repair GUI for " + target.getName());
        }

        // Notify target player
        if (!target.equals(sender)) {
            target.sendMessage(plugin.getLoreUpdater().colorize(
                    plugin.getMessages().getString("prefix", "&8[&6OmniRepair&8] ") +
                    plugin.getMessages().getString("general.gui-opened-for-you")
            ));
        }
    }

    /**
     * Handle config reload (admin only).
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

        if (sender instanceof Player) {
            sendMessage(sender, plugin.getMessages().getString("general.help-line-gui"));
        }

        if (sender.hasPermission("omnirepair.admin")) {
            sendMessage(sender, plugin.getMessages().getString("general.help-line-target"));
            sendMessage(sender, plugin.getMessages().getString("general.help-line-reload"));
            sendMessage(sender, plugin.getMessages().getString("general.help-line-debug"));
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

            if (sender.hasPermission("omnirepair.admin")) {
                completions.add("reload");
                completions.add("debug");
            }

            // If admin or console, also show player names
            if (!(sender instanceof Player) || sender.hasPermission("omnirepair.admin")) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.hasPermission("omnirepair.use")) {
                        completions.add(player.getName());
                    }
                }
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
        sender.sendMessage(plugin.getLoreUpdater().colorize(prefix + message));
    }
}
