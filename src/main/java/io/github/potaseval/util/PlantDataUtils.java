package io.github.potaseval.util;

import io.github.potaseval.GreatWeeb;
import org.bukkit.Chunk;
// Chunk import already present
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class PlantDataUtils {

    private static JavaPlugin plugin;

    // Ключи для типов пшеницы
    public static NamespacedKey SATIVA_WHEAT_KEY;
    public static NamespacedKey INDICA_WHEAT_KEY;
    public static NamespacedKey TOBACCO_WHEAT_KEY;

    // Ключи состояний
    public static NamespacedKey DRY_KEY;
    public static NamespacedKey MOIST_KEY;
    public static NamespacedKey FERTILIZED_KEY;
    public static NamespacedKey BOX_FERTILIZED_KEY;

    private static final String PLANT_KEY_PREFIX = "plant_";

    private PlantDataUtils() {}

    /** Вызывается один раз при запуске плагина. */
    public static void init(JavaPlugin plugin) {
        PlantDataUtils.plugin = plugin;
        SATIVA_WHEAT_KEY = new NamespacedKey(plugin, "sativa_wheat");
        INDICA_WHEAT_KEY = new NamespacedKey(plugin, "indica_wheat");
        TOBACCO_WHEAT_KEY = new NamespacedKey(plugin, "tobacco_wheat");
        DRY_KEY = new NamespacedKey(plugin, "dry");
        MOIST_KEY = new NamespacedKey(plugin, "moist");
        FERTILIZED_KEY = new NamespacedKey(plugin, "fertilized");
        BOX_FERTILIZED_KEY = new NamespacedKey(plugin, "box_fertilized");
    }

    // ---- Вспомогательный ключ блока ----
    private static NamespacedKey getBlockKey(Block block, String prefix) {
        return new NamespacedKey(plugin, prefix + "_x" + block.getX() + "_y" + block.getY() + "_z" + block.getZ());
    }

    private static PersistentDataContainer getChunkPDC(Block block) {
        Chunk chunk = block.getChunk();
        if (!chunk.isLoaded()) {
            throw new IllegalStateException("Attempted to access PDC of unloaded chunk at " + block.getLocation());
        }
        return chunk.getPersistentDataContainer();
    }

    // ---- Сухость ----
    public static boolean isDry(Block block) {
        return getChunkPDC(block)
                .getOrDefault(getBlockKey(block, "dry"), PersistentDataType.BOOLEAN, false);
    }

    public static void setDry(Block block, boolean value) {
        PersistentDataContainer pdc = getChunkPDC(block);
        NamespacedKey key = getBlockKey(block, "dry");
        if (value) pdc.set(key, PersistentDataType.BOOLEAN, true);
        else pdc.remove(key);
    }

    // ---- Влажность ----
    public static boolean isMoist(Block block) {
        return getChunkPDC(block)
                .getOrDefault(getBlockKey(block, "moist"), PersistentDataType.BOOLEAN, false);
    }

    public static void setMoist(Block block, boolean value) {
        PersistentDataContainer pdc = getChunkPDC(block);
        NamespacedKey key = getBlockKey(block, "moist");
        if (value) pdc.set(key, PersistentDataType.BOOLEAN, true);
        else pdc.remove(key);
    }

    // ---- Удобрение ----
    public static boolean isFertilized(Block block) {
        return getChunkPDC(block)
                .getOrDefault(getBlockKey(block, "fertilized"), PersistentDataType.BOOLEAN, false);
    }

    public static void setFertilized(Block block, boolean value) {
        PersistentDataContainer pdc = getChunkPDC(block);
        NamespacedKey key = getBlockKey(block, "fertilized");
        if (value) pdc.set(key, PersistentDataType.BOOLEAN, true);
        else pdc.remove(key);
    }

    // ---- Боксовое удобрение ----
    public static boolean isBoxFertilized(Block block) {
        return getChunkPDC(block)
                .getOrDefault(getBlockKey(block, "box_fertilized"), PersistentDataType.BOOLEAN, false);
    }

    public static void setBoxFertilized(Block block, boolean value) {
        PersistentDataContainer pdc = getChunkPDC(block);
        NamespacedKey key = getBlockKey(block, "box_fertilized");
        if (value) pdc.set(key, PersistentDataType.BOOLEAN, true);
        else pdc.remove(key);
    }

    // ---- Принадлежность к strain ----
    private static boolean isStrainWheat(Block block, NamespacedKey strainKey) {
        return block.getChunk().getPersistentDataContainer()
                .getOrDefault(getBlockKey(block, strainKey.getKey()), PersistentDataType.BOOLEAN, false);
    }

    /** Общая установка любого кастомного флага блока (используется в PlantListener). */
    public static void setBlockPDC(Block block, NamespacedKey key, boolean value) {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, key.getKey());
        if (value) pdc.set(blockKey, PersistentDataType.BOOLEAN, true);
        else pdc.remove(blockKey);
    }

    // ---- Управление списком растений в PDC (plant_ префикс) ----
    private static NamespacedKey getPlantKey(Block block) {
        return new NamespacedKey(plugin, PLANT_KEY_PREFIX + block.getX() + "_" + block.getY() + "_" + block.getZ());
    }

    public static void addPlantToPDC(Block block, StrainType type) {
        block.getChunk().getPersistentDataContainer().set(getPlantKey(block), PersistentDataType.STRING, type.name());
    }

    public static void removePlantFromPDC(Block block) {
        block.getChunk().getPersistentDataContainer().remove(getPlantKey(block));
    }

    /** Загружает все активные растения в переданное множество. Вызывается при загрузке чанка. */
    public static void loadPlantsFromPDC(Chunk chunk, Set<Location> activePlants) {
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

    // ---- Определение типа сорта ----
    public enum StrainType { SATIVA, INDICA, TOBACCO }

    public static class StrainInfo {
        public final StrainType type;
        public final NamespacedKey key;
        StrainInfo(StrainType type, NamespacedKey key) {
            this.type = type;
            this.key = key;
        }
    }

    /** Возвращает информацию о сорте пшеницы. */
    public static StrainInfo getStrainInfo(Block block) {
        if (isStrainWheat(block, SATIVA_WHEAT_KEY)) return new StrainInfo(StrainType.SATIVA, SATIVA_WHEAT_KEY);
        if (isStrainWheat(block, INDICA_WHEAT_KEY)) return new StrainInfo(StrainType.INDICA, INDICA_WHEAT_KEY);
        if (isStrainWheat(block, TOBACCO_WHEAT_KEY)) return new StrainInfo(StrainType.TOBACCO, TOBACCO_WHEAT_KEY);
        return null;
    }

    /** Более короткий метод, возвращающий только тип (для случаев, где не нужен ключ). */
    public static StrainType getStrainType(Block block) {
        StrainInfo info = getStrainInfo(block);
        return info != null ? info.type : null;
    }
}
