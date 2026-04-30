package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.DryingRack;
import io.github.potaseval.managers.DryingRackManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class DryingRackListener implements Listener {

    private final GreatWeeb plugin;
    private final DryingRack dryingRack;
    private final DryingRackManager manager;
    private final NamespacedKey RACK_KEY;

    public DryingRackListener(GreatWeeb plugin, DryingRack dryingRack, DryingRackManager manager) {
        this.plugin = plugin;
        this.dryingRack = dryingRack;
        this.manager = manager;
        this.RACK_KEY = new NamespacedKey(plugin, "drying_rack");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!dryingRack.isDryingRack(item)) return;

        Block block = event.getBlock();

        if (block.getState() instanceof TileState state) {
            state.getPersistentDataContainer().set(RACK_KEY, PersistentDataType.BOOLEAN, true);
            state.update();
        }
        manager.placeRack(block.getLocation());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BARREL) return;
        if (!(block.getState() instanceof TileState state)) return;
        if (!state.getPersistentDataContainer().has(RACK_KEY, PersistentDataType.BOOLEAN)) return;

        manager.breakRack(block.getLocation(), block.getWorld());
        state.getPersistentDataContainer().remove(RACK_KEY);
        state.update();

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), dryingRack.createDryingRack());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.BARREL) return;
        if (!(clicked.getState() instanceof TileState state)) return;
        if (!state.getPersistentDataContainer().has(RACK_KEY, PersistentDataType.BOOLEAN)) return;

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);

        Player player = event.getPlayer();
        player.openInventory(manager.getRackInventory(clicked.getLocation()));
    }
}