package io.github.potaseval.listeners.block;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.GashItems;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PackBlockListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final GashItems gashItems;
    private final NamespacedKey PACK_TYPE_KEY;

    public PackBlockListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.gashItems = plugin.getGashItems();
        this.PACK_TYPE_KEY = new NamespacedKey(plugin, "pack_block_type");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String type = getPackBlockType(item);
        if (type == null) return;

        Block block = event.getBlock();
        NamespacedKey key = getBlockKey(block);
        block.getChunk().getPersistentDataContainer().set(key, PersistentDataType.STRING, type);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.DRIED_KELP_BLOCK) return;

        String type = getPackBlockTypeFromBlock(block);
        if (type == null) return;

        event.setDropItems(false);
        event.setExpToDrop(0);

        ItemStack briquettes = getBriquettesForType(type);
        briquettes.setAmount(9);
        block.getWorld().dropItemNaturally(block.getLocation(), briquettes);

        NamespacedKey key = getBlockKey(block);
        block.getChunk().getPersistentDataContainer().remove(key);
    }

    @EventHandler
    public void onDrop(BlockDropItemEvent event) {
        Block block = event.getBlockState().getBlock();
        if (block.getType() == Material.DRIED_KELP_BLOCK && getPackBlockTypeFromBlock(block) != null) {
            event.getItems().clear();
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blocks) {
        List<Block> toRemove = new ArrayList<>();
        for (Block block : blocks) {
            if (block.getType() == Material.DRIED_KELP_BLOCK && getPackBlockTypeFromBlock(block) != null) {
                toRemove.add(block);
            }
        }
        for (Block block : toRemove) {
            String type = getPackBlockTypeFromBlock(block);
            NamespacedKey key = getBlockKey(block);
            block.getChunk().getPersistentDataContainer().remove(key);
            ItemStack briquettes = getBriquettesForType(type);
            briquettes.setAmount(9);
            block.getWorld().dropItemNaturally(block.getLocation(), briquettes);
            block.setType(Material.AIR);
        }
        blocks.removeAll(toRemove);
    }

    private String getPackBlockType(ItemStack item) {
        if (sativaItems.isPack(item)) return "sativa";
        if (indicaItems.isPack(item)) return "indica";
        if (gashItems.isGashPack(item)) return "gash";
        if (gashItems.isSpicePack(item)) return "spice";
        return null;
    }

    private String getPackBlockTypeFromBlock(Block block) {
        NamespacedKey key = getBlockKey(block);
        return block.getChunk().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private ItemStack getBriquettesForType(String type) {
        return switch (type) {
            case "sativa" -> sativaItems.createBriquette();
            case "indica" -> indicaItems.createBriquette();
            case "gash" -> gashItems.createGashBriquette();
            case "spice" -> gashItems.createSpiceBriquette();
            default -> null;
        };
    }

    private NamespacedKey getBlockKey(Block block) {
        return new NamespacedKey(plugin, PACK_TYPE_KEY.getKey() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }
}
