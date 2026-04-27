package io.github.potaseval.listeners;

import io.github.potaseval.managers.DryingRackManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class DryingRackInventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();

        if (!(inv.getHolder() instanceof DryingRackManager.DryingHolder)) return;

        int slot = event.getRawSlot();
        if (slot >= 0 && slot < inv.getSize()) {
            if (slot != 1 && slot != 3 && slot != 4) {
                event.setCancelled(true);
            }
        }
    }
}
