package io.github.potaseval.listeners.block;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.TobaccoItems;
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

public class CigaretteBlockListener implements Listener {

    private final GreatWeeb plugin;
    private final TobaccoItems tobaccoItems;
    private final NamespacedKey BLOCK_KEY;

    public CigaretteBlockListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.tobaccoItems = plugin.getTobaccoItems();
        this.BLOCK_KEY = new NamespacedKey(plugin, "cigarette_block");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!tobaccoItems.isCigaretteBlock(item)) return;

        Block block = event.getBlock();
        NamespacedKey key = getBlockKey(block);
        block.getChunk().getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.LIGHT_GRAY_WOOL) return;

        if (!isCigaretteBlock(block)) return;

        event.setDropItems(false);
        event.setExpToDrop(0);

        ItemStack packs = tobaccoItems.createCigarettePack();
        packs.setAmount(9);
        block.getWorld().dropItemNaturally(block.getLocation(), packs);

        NamespacedKey key = getBlockKey(block);
        block.getChunk().getPersistentDataContainer().remove(key);
    }

    @EventHandler
    public void onDrop(BlockDropItemEvent event) {
        Block block = event.getBlockState().getBlock();
        if (block.getType() == Material.LIGHT_GRAY_WOOL && isCigaretteBlock(block)) {
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
            if (block.getType() == Material.LIGHT_GRAY_WOOL && isCigaretteBlock(block)) {
                toRemove.add(block);
            }
        }
        for (Block block : toRemove) {
            NamespacedKey key = getBlockKey(block);
            block.getChunk().getPersistentDataContainer().remove(key);
            ItemStack packs = tobaccoItems.createCigarettePack();
            packs.setAmount(9);
            block.getWorld().dropItemNaturally(block.getLocation(), packs);
            block.setType(Material.AIR);
        }
        blocks.removeAll(toRemove);
    }
    private boolean isCigaretteBlock(Block block) {
        NamespacedKey key = getBlockKey(block);
        return block.getChunk().getPersistentDataContainer().getOrDefault(key, PersistentDataType.BOOLEAN, false);
    }

    private NamespacedKey getBlockKey(Block block) {
        return new NamespacedKey(plugin, BLOCK_KEY.getKey() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }
}