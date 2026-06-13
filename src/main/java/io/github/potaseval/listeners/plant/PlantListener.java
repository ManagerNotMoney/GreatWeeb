package io.github.potaseval.listeners.plant;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import io.github.potaseval.items.TobaccoItems;
import io.github.potaseval.util.PlantDataUtils;
import io.github.potaseval.util.PlantDataUtils.StrainInfo;
import io.github.potaseval.util.PlantDataUtils.StrainType;
import org.bukkit.*;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PlantListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final TobaccoItems tobaccoItems;

    private final Set<Location> activePlants = new HashSet<>();

    public PlantListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.tobaccoItems = plugin.getTobaccoItems();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : new HashSet<>(activePlants)) {
                    World world = loc.getWorld();
                    if (world == null || !world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        continue;
                    }
                    Block block = loc.getBlock();
                    if (block.getType() != Material.WHEAT) {
                        activePlants.remove(loc);
                        PlantDataUtils.removePlantFromPDC(block);
                        continue;
                    }
                    StrainInfo strain = PlantDataUtils.getStrainInfo(block);
                    if (strain == null) {
                        activePlants.remove(loc);
                        PlantDataUtils.removePlantFromPDC(block);
                        continue;
                    }
                    if (!PlantDataUtils.isDry(block) && !PlantDataUtils.isBoxFertilized(block) && Math.random() < 0.15) {
                        if (PlantDataUtils.isMoist(block)) {
                            PlantDataUtils.setMoist(block, false);
                        } else {
                            PlantDataUtils.setDry(block, true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1600L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : new HashSet<>(activePlants)) {
                    World world = loc.getWorld();
                    if (world == null || !world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        continue;
                    }
                    Block block = loc.getBlock();
                    if (block.getType() != Material.WHEAT) continue;

                    StrainInfo strain = PlantDataUtils.getStrainInfo(block);
                    if (strain == null) continue;
                    if (PlantDataUtils.isDry(block)) continue;

                    Ageable ageable = (Ageable) block.getBlockData();
                    if (ageable.getAge() >= ageable.getMaximumAge()) continue;

                    Block above = block.getRelative(0, 1, 0);
                    double chance = 0.0;
                    boolean isUltrachlorophyll = false;

                    if (above.getType() == Material.PEARLESCENT_FROGLIGHT) {
                        chance = 0.2;
                    } else if (above.getType() == Material.PURPLE_STAINED_GLASS
                            && above.getRelative(0, 1, 0).getType() == Material.PEARLESCENT_FROGLIGHT) {
                        chance = 0.3;
                    } else if (above.getType() == Material.GREEN_STAINED_GLASS
                            && above.getRelative(0, 1, 0).getType() == Material.VERDANT_FROGLIGHT) {
                        // Ultrachlorophyll: slows growth by 15% (0.85 multiplier on base chance)
                        chance = 0.2 * 0.85;
                        isUltrachlorophyll = true;
                    }

                    if (chance > 0.0 && Math.random() < chance) {
                        ageable.setAge(ageable.getAge() + 1);
                        block.setBlockData(ageable);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 300L);
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.WHEAT) return;
        if (PlantDataUtils.getStrainInfo(block) != null && PlantDataUtils.isDry(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSeedPlant(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        Block block = event.getBlock();
        if (sativaItems.isSeed(itemInHand)) {
            plantStrain(block, PlantDataUtils.SATIVA_WHEAT_KEY, StrainType.SATIVA);
        } else if (indicaItems.isSeed(itemInHand)) {
            plantStrain(block, PlantDataUtils.INDICA_WHEAT_KEY, StrainType.INDICA);
        } else if (tobaccoItems.isTobaccoSeed(itemInHand)) {
            plantStrain(block, PlantDataUtils.TOBACCO_WHEAT_KEY, StrainType.TOBACCO);
        }
    }

    private void plantStrain(Block block, NamespacedKey strainKey, StrainType type) {
        block.setType(Material.WHEAT);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(0);
        block.setBlockData(ageable);

        PlantDataUtils.setBlockPDC(block, PlantDataUtils.SATIVA_WHEAT_KEY, false);
        PlantDataUtils.setBlockPDC(block, PlantDataUtils.INDICA_WHEAT_KEY, false);
        PlantDataUtils.setBlockPDC(block, PlantDataUtils.TOBACCO_WHEAT_KEY, false);
        PlantDataUtils.setBlockPDC(block, strainKey, true);

        block.getState().update(true, true);
        block.getChunk().setForceLoaded(true);
        block.getChunk().setForceLoaded(false);

        activePlants.add(block.getLocation());
        PlantDataUtils.addPlantToPDC(block, type);
    }

    @EventHandler
    public void onWheatBreak(BlockDropItemEvent event) {
        BlockState blockState = event.getBlockState();
        if (blockState.getType() != Material.WHEAT) return;
        Block block = blockState.getBlock();

        StrainInfo strain = PlantDataUtils.getStrainInfo(block);
        if (strain == null) {
            if (event.getItems().isEmpty()) {
                dropVanillaWheat(block, blockState);
            }
            return;
        }

        if (strain.type == StrainType.TOBACCO) {
            event.getItems().clear();

            Ageable ageable = (Ageable) blockState.getBlockData();
            int age = ageable.getAge();
            int maxAge = ageable.getMaximumAge();
            Random random = new Random();

            ItemStack seeds = tobaccoItems.createSeed();
            if (age == maxAge) {
                int seedAmount = random.nextInt(2) + 1;
                seeds.setAmount(seedAmount);
                int tobaccoAmount = random.nextInt(2) + 1;
                ItemStack tobacco = tobaccoItems.createTobacco();
                tobacco.setAmount(tobaccoAmount);
                block.getWorld().dropItemNaturally(block.getLocation(), tobacco);
            } else {
                seeds.setAmount(1);
            }
            block.getWorld().dropItemNaturally(block.getLocation(), seeds);

            activePlants.remove(block.getLocation());
            PlantDataUtils.removePlantFromPDC(block);
            PlantDataUtils.setBlockPDC(block, strain.key, false);
            PlantDataUtils.setDry(block, false);
            PlantDataUtils.setMoist(block, false);
            PlantDataUtils.setFertilized(block, false);
            PlantDataUtils.setBoxFertilized(block, false);
            block.getState().update(true, true);
            block.getChunk().setForceLoaded(true);
            block.getChunk().setForceLoaded(false);
            return;
        }

        event.getItems().clear();

        Ageable ageable = (Ageable) blockState.getBlockData();
        ItemStack seed = (strain.type == StrainType.SATIVA) ? sativaItems.createSeed() : indicaItems.createSeed();
        block.getWorld().dropItemNaturally(block.getLocation(), seed);

        if (ageable.getAge() == ageable.getMaximumAge()) {
            ItemStack bud = (strain.type == StrainType.SATIVA) ? sativaItems.createBud() : indicaItems.createBud();
            block.getWorld().dropItemNaturally(block.getLocation(), bud);
        }

        activePlants.remove(block.getLocation());
        PlantDataUtils.removePlantFromPDC(block);
        PlantDataUtils.setBlockPDC(block, strain.key, false);
        PlantDataUtils.setDry(block, false);
        PlantDataUtils.setMoist(block, false);
        PlantDataUtils.setFertilized(block, false);
        PlantDataUtils.setBoxFertilized(block, false);
        block.getState().update(true, true);
        block.getChunk().setForceLoaded(true);
        block.getChunk().setForceLoaded(false);
    }

    @EventHandler
    public void onFarmlandTrample(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND && event.getTo() == Material.DIRT) {
            Block above = event.getBlock().getRelative(0, 1, 0);
            if (above.getType() == Material.WHEAT) {
                StrainInfo strain = PlantDataUtils.getStrainInfo(above);
                if (strain != null) {
                    activePlants.remove(above.getLocation());
                    PlantDataUtils.removePlantFromPDC(above);
                    ItemStack seed;
                    if (strain.type == StrainType.SATIVA) {
                        seed = sativaItems.createSeed();
                    } else if (strain.type == StrainType.INDICA) {
                        seed = indicaItems.createSeed();
                    } else {
                        seed = tobaccoItems.createSeed();
                    }
                    above.getWorld().dropItemNaturally(above.getLocation().add(0.5, 0.5, 0.5), seed);
                    PlantDataUtils.setBlockPDC(above, strain.key, false);
                    PlantDataUtils.setDry(above, false);
                    PlantDataUtils.setMoist(above, false);
                    PlantDataUtils.setFertilized(above, false);
                    PlantDataUtils.setBoxFertilized(above, false);
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
        PlantDataUtils.loadPlantsFromPDC(event.getChunk(), activePlants);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        activePlants.removeIf(loc -> loc.getChunk().equals(event.getChunk()));
    }
}