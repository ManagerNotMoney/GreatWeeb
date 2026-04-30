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
    private final Set<Location> activePlants = new HashSet<>();
    public static NamespacedKey DRY_KEY;
    private final NamespacedKey MOIST_KEY;
    private final NamespacedKey FERTILIZED_KEY;
    private static final String PLANT_KEY_PREFIX = "plant_";

    public PlantListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.SATIVA_WHEAT_KEY = new NamespacedKey(plugin, "sativa_wheat");
        this.INDICA_WHEAT_KEY = new NamespacedKey(plugin, "indica_wheat");
        DRY_KEY = new NamespacedKey(plugin, "dry");
        this.MOIST_KEY = new NamespacedKey(plugin, "moist");
        this.FERTILIZED_KEY = new NamespacedKey(plugin, "fertilized");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : new HashSet<>(activePlants)) {
                    Block block = loc.getBlock();
                    if (block.getType() != Material.WHEAT) {
                        activePlants.remove(loc);
                        removePlantFromPDC(block);
                        continue;
                    }
                    StrainInfo strain = getStrainInfo(block);
                    if (strain == null) {
                        activePlants.remove(loc);
                        removePlantFromPDC(block);
                        continue;
                    }
                    if (!isDry(block) && Math.random() < 0.15) {
                        if (isMoist(block)) {
                            setMoist(block, false);
                        } else {
                            setDry(block, true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1600L);
    }

    // === Работа с PDC чанка ===
    private void addPlantToPDC(Block block, StrainType strainType) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey key = getPlantKey(block);
        chunkPDC.set(key, PersistentDataType.STRING, strainType.name());
    }

    private void removePlantFromPDC(Block block) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey key = getPlantKey(block);
        chunkPDC.remove(key);
    }

    private NamespacedKey getPlantKey(Block block) {
        return new NamespacedKey(plugin, PLANT_KEY_PREFIX + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }
    private void loadPlantsFromPDC(Chunk chunk) {
        PersistentDataContainer chunkPDC = chunk.getPersistentDataContainer();
        for (NamespacedKey key : chunkPDC.getKeys()) {
            if (key.getKey().startsWith(PLANT_KEY_PREFIX)) {
                String value = chunkPDC.get(key, PersistentDataType.STRING);
                if (value == null) continue;
                String[] parts = key.getKey().substring(PLANT_KEY_PREFIX.length()).split("_");
                if (parts.length != 3) continue;
                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);

                    Block block = chunk.getWorld().getBlockAt(x, y, z);

                    if (block.getType() != Material.WHEAT) {
                        chunkPDC.remove(key);
                        continue;
                    }
                    StrainType type = StrainType.valueOf(value);
                    StrainInfo info = getStrainInfo(block);
                    if (info == null || info.type != type) {
                        chunkPDC.remove(key);
                        continue;
                    }
                    activePlants.add(block.getLocation());
                } catch (IllegalArgumentException e) {
                    chunkPDC.remove(key);
                }
            }
        }
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
    private boolean isFertilized(Block block) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, "fertilized");
        return chunkPDC.getOrDefault(blockKey, PersistentDataType.BOOLEAN, false);
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

    private void setBlockPDC(Block block, NamespacedKey key, boolean value) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, key.getKey());
        if (value) {
            chunkPDC.set(blockKey, PersistentDataType.BOOLEAN, true);
        } else {
            chunkPDC.remove(blockKey);
        }
    }

    private NamespacedKey getBlockKey(Block block, String prefix) {
        return new NamespacedKey(plugin, prefix + "_x" + block.getX() + "_y" + block.getY() + "_z" + block.getZ());
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
            plantStrain(block, SATIVA_WHEAT_KEY, StrainType.SATIVA);
        } else if (indicaItems.isSeed(itemInHand)) {
            plantStrain(block, INDICA_WHEAT_KEY, StrainType.INDICA);
        }
    }

    private void plantStrain(Block block, NamespacedKey strainKey, StrainType type) {
        block.setType(Material.WHEAT);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(0);
        block.setBlockData(ageable);
        setBlockPDC(block, strainKey, true);
        block.getState().update(true, true);
        block.getChunk().setForceLoaded(true);
        block.getChunk().setForceLoaded(false);

        activePlants.add(block.getLocation());
        addPlantToPDC(block, type);
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
        activePlants.remove(block.getLocation());
        removePlantFromPDC(block);
        setBlockPDC(block, strain.key, false);
        block.getState().update(true, true);
        block.getChunk().setForceLoaded(true);
        block.getChunk().setForceLoaded(false);

        event.getItems().clear();

        Ageable ageable = (Ageable) blockState.getBlockData();

        ItemStack seed = (strain.type == StrainType.SATIVA)
                ? sativaItems.createSeed()
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
                    activePlants.remove(above.getLocation());
                    removePlantFromPDC(above);
                    ItemStack seed = (strain.type == StrainType.SATIVA)
                            ? sativaItems.createSeed()
                            : indicaItems.createSeed();
                    above.getWorld().dropItemNaturally(above.getLocation().add(0.5, 0.5, 0.5), seed);
                    setBlockPDC(above, strain.key, false);

                    setDry(above, false);
                    setMoist(above, false);
                    setFertilized(above, false);
                }
            }
        }
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

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        loadPlantsFromPDC(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        activePlants.removeIf(loc -> loc.getChunk().equals(event.getChunk()));
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