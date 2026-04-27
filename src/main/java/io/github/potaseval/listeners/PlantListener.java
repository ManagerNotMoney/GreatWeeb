package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class PlantListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final NamespacedKey SATIVA_WHEAT_KEY;
    private final NamespacedKey INDICA_WHEAT_KEY;
    private final NamespacedKey FERTILIZED_KEY;
    private final Set<Location> activePlants = new HashSet<>();
    public static NamespacedKey DRY_KEY;
    private final NamespacedKey MOIST_KEY;

    public PlantListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.SATIVA_WHEAT_KEY = new NamespacedKey(plugin, "sativa_wheat");
        this.INDICA_WHEAT_KEY = new NamespacedKey(plugin, "indica_wheat");
        this.FERTILIZED_KEY = new NamespacedKey(plugin, "fertilized");
        DRY_KEY = new NamespacedKey(plugin, "dry");
        this.MOIST_KEY = new NamespacedKey(plugin, "moist");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : new HashSet<>(activePlants)) {
                    Block block = loc.getBlock();
                    if (block.getType() != Material.WHEAT) {
                        activePlants.remove(loc);
                        continue;
                    }
                    StrainInfo strain = getStrainInfo(block);
                    if (strain == null) {
                        activePlants.remove(loc);
                        continue;
                    }
                    if (!isDry(block) && Math.random() < 0.15) {
                        if (isMoist(block)) {
                            setMoist(block, false);   // УВ(Л)АЖЕНИЕ ПРОПАДАЕТ
                        } else {
                            setDry(block, true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1600L);
    }
    private boolean isDry(Block block) {
        return block.getChunk().getPersistentDataContainer()
                .getOrDefault(getBlockKey(block, "dry"), PersistentDataType.BOOLEAN, false);
    }
    private void setDry(Block block, boolean value) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey key = getBlockKey(block, "dry");
        if (value) chunkPDC.set(key, PersistentDataType.BOOLEAN, true);
        else chunkPDC.remove(key);
    }
    private void addPlantLocation(Block block) {
        activePlants.add(block.getLocation());
    }
    private void removePlantLocation(Block block) {
        activePlants.remove(block.getLocation());
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.WHEAT) return;
        if (getStrainInfo(block) != null && isDry(block)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onSeedPlant(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        Block block = event.getBlock();

        if (sativaItems.isSeed(itemInHand)) {
            plantStrain(block, SATIVA_WHEAT_KEY);
            addPlantLocation(block);
        } else if (indicaItems.isSeed(itemInHand)) {
            plantStrain(block, INDICA_WHEAT_KEY);
            addPlantLocation(block);
        }
    }

    private void plantStrain(Block block, NamespacedKey strainKey) {
        block.setType(Material.WHEAT);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(0);
        block.setBlockData(ageable);
        setBlockPDC(block, strainKey, true);
        block.getState().update(true, true);
        block.getChunk().setForceLoaded(true);
        block.getChunk().setForceLoaded(false);
    }

    @EventHandler
    public void onWheatBreak(BlockDropItemEvent event) {
        BlockState blockState = event.getBlockState();
        if (blockState.getType() != Material.WHEAT) return;
        Block block = blockState.getBlock();

        StrainInfo strain = getStrainInfo(block);
        if (strain == null) {

            if (event.getItems().isEmpty()) {
                dropVanillaWheat(block, blockState);
            }
            return;
        }
        setDry(block, false);
        setMoist(block, false);
        removePlantLocation(block);
        setBlockPDC(block, strain.key, false);
        setFertilized(block, false);
        block.getState().update(true, true);
        block.getChunk().setForceLoaded(true);
        block.getChunk().setForceLoaded(false);

        event.getItems().clear();

        Ageable ageable = (Ageable) blockState.getBlockData();

        ItemStack seed = (strain.type == StrainType.SATIVA)
                ? sativaItems.createSeed()   // после рефакторинга createSeed()
                : indicaItems.createSeed();
        block.getWorld().dropItemNaturally(block.getLocation(), seed);

        if (ageable.getAge() == ageable.getMaximumAge()) {
            ItemStack bud = (strain.type == StrainType.SATIVA)
                    ? sativaItems.createBud()
                    : indicaItems.createBud();
            block.getWorld().dropItemNaturally(block.getLocation(), bud);
        }
    }

    @EventHandler
    public void onFarmlandTrample(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND && event.getTo() == Material.DIRT) {
            Block above = event.getBlock().getRelative(0, 1, 0);
            if (above.getType() == Material.WHEAT) {
                StrainInfo strain = getStrainInfo(above);
                if (strain != null) {
                    removePlantLocation(above);
                    ItemStack seed = (strain.type == StrainType.SATIVA)
                            ? sativaItems.createSeed()
                            : indicaItems.createSeed();
                    above.getWorld().dropItemNaturally(above.getLocation().add(0.5, 0.5, 0.5), seed);
                    setBlockPDC(above, strain.key, false);
                }
            }
        }
    }

    private void setFertilized(Block block, boolean value) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, "fertilized");
        if (value) {
            chunkPDC.set(blockKey, PersistentDataType.BOOLEAN, true);
        } else {
            chunkPDC.remove(blockKey);
        }
    }

    private boolean isFertilized(Block block) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, "fertilized");
        return chunkPDC.getOrDefault(blockKey, PersistentDataType.BOOLEAN, false);
    }

    private void dropVanillaWheat(Block block, BlockState blockState) {
        Ageable ageable = (Ageable) blockState.getBlockData();
        if (ageable.getAge() == ageable.getMaximumAge()) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT));
            int seedCount = new java.util.Random().nextInt(4);
            if (seedCount > 0) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT_SEEDS, seedCount));
            }
        } else {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT_SEEDS));
        }
    }

    private void setBlockPDC(Block block, NamespacedKey key, boolean value) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, key.getKey());
        if (value) {
            chunkPDC.set(blockKey, PersistentDataType.BOOLEAN, true);
        } else {
            chunkPDC.remove(blockKey);
        }
    }
    private boolean isMoist(Block block) {
        return block.getChunk().getPersistentDataContainer()
                .getOrDefault(getBlockKey(block, "moist"), PersistentDataType.BOOLEAN, false);
    }

    private void setMoist(Block block, boolean value) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey key = getBlockKey(block, "moist");
        if (value) chunkPDC.set(key, PersistentDataType.BOOLEAN, true);
        else chunkPDC.remove(key);
    }
    private boolean isStrainWheat(Block block, NamespacedKey strainKey) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, strainKey.getKey());
        return chunkPDC.getOrDefault(blockKey, PersistentDataType.BOOLEAN, false);
    }

    private StrainInfo getStrainInfo(Block block) {
        if (isStrainWheat(block, SATIVA_WHEAT_KEY)) {
            return new StrainInfo(StrainType.SATIVA, SATIVA_WHEAT_KEY);
        } else if (isStrainWheat(block, INDICA_WHEAT_KEY)) {
            return new StrainInfo(StrainType.INDICA, INDICA_WHEAT_KEY);
        }
        return null;
    }
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getType() == Material.WHEAT && getStrainInfo(block) != null) {
                        activePlants.add(block.getLocation());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        activePlants.removeIf(loc -> loc.getChunk().equals(event.getChunk()));
    }

    private NamespacedKey getBlockKey(Block block, String prefix) {
        return new NamespacedKey(plugin, prefix + "_x" + block.getX() + "_y" + block.getY() + "_z" + block.getZ());
    }

    private enum StrainType { SATIVA, INDICA }

    private static class StrainInfo {
        final StrainType type;
        final NamespacedKey key;

        StrainInfo(StrainType type, NamespacedKey key) {
            this.type = type;
            this.key = key;
        }
    }
}