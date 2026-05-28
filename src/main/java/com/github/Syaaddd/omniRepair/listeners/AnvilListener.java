package com.github.Syaaddd.omniRepair.listeners;

import com.github.Syaaddd.omniRepair.OmniRepair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents EXP leak when players put MMOItems into a vanilla anvil.
 * MMOItems cannot be enchanted via anvil; this listener blocks the operation
 * and notifies the player with a configurable message.
 */
public class AnvilListener implements Listener {

    private final OmniRepair plugin;

    public AnvilListener(OmniRepair plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!plugin.getConfig().getBoolean("anvil.block-mmoitems-enchant", true)) {
            return;
        }

        ItemStack left = event.getInventory().getItem(0);
        if (left == null || left.getType().isAir()) {
            return;
        }

        if (!isMMOItem(left)) {
            return;
        }

        event.setResult(null);

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("[DEBUG] AnvilListener: Blocked MMOItem enchant via anvil");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilClick(InventoryClickEvent event) {
        if (!plugin.getConfig().getBoolean("anvil.block-mmoitems-enchant", true)) {
            return;
        }

        if (event.getView().getTopInventory().getType() != InventoryType.ANVIL) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot != 2) {
            return;
        }

        ItemStack left = event.getView().getTopInventory().getItem(0);
        if (left == null || left.getType().isAir()) {
            return;
        }

        if (!isMMOItem(left)) {
            return;
        }

        event.setCancelled(true);

        if (event.getWhoClicked() instanceof Player player) {
            String message = plugin.getMessages().getString("repair.anvil-blocked",
                    "&cItem RPG ini tidak dapat di-enchant melalui anvil!");
            String prefix = plugin.getMessages().getString("prefix", "&8[&6OmniRepair&8] ");
            player.sendMessage(plugin.getLoreUpdater().colorize(prefix + message));
        }
    }

    private boolean isMMOItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        if (plugin.getMmoItemsHook() == null || !plugin.getMmoItemsHook().isEnabled()) {
            return false;
        }
        return plugin.getMmoItemsHook().isMMOItem(item);
    }
}
