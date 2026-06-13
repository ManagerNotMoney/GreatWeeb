package io.github.potaseval.managers;

import io.github.potaseval.items.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DryingRackManager {

    private final JavaPlugin plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final GashItems gashItems;
    private final MedicalItems medicalItems;
    private final Map<Location, DryingData> racks = new HashMap<>();
    private final TobaccoItems tobaccoItems;
    private static final int TICKS_NEEDED = 900;
    private static final long TIMER_INTERVAL = 100L;
    private static final int PROGRESS_PER_STEP = 100;
    private static final String CONFIG_SECTION = "drying-racks";


    public DryingRackManager(JavaPlugin plugin, SativaItems sativaItems, IndicaItems indicaItems,GashItems gashItems,MedicalItems medicalItems, TobaccoItems tobaccoItems) {
        this.plugin = plugin;
        this.sativaItems = sativaItems;
        this.indicaItems = indicaItems;
        this.gashItems = gashItems;
        this.medicalItems = medicalItems;
        this.tobaccoItems = tobaccoItems;

        new BukkitRunnable() {
            @Override
            public void run() {
                tickAll();
            }
        }.runTaskTimer(plugin, 0L, TIMER_INTERVAL);
    }
    public void saveRacks() {
        FileConfiguration config = plugin.getConfig();
        config.set(CONFIG_SECTION, null); // очищаем старую секцию
        ConfigurationSection section = config.createSection(CONFIG_SECTION);

        for (Map.Entry<Location, DryingData> entry : racks.entrySet()) {
            Location loc = entry.getKey();
            DryingData data = entry.getValue();
            String key = loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
            ConfigurationSection rackSection = section.createSection(key);

            rackSection.set("progress", data.progressTicks);

            // Сохраняем предметы из слотов 1, 3, 4
            ConfigurationSection itemsSection = rackSection.createSection("items");
            for (int slot : new int[]{1, 3, 4}) {
                ItemStack item = data.inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    itemsSection.set(String.valueOf(slot), item.serialize());
                }
            }
        }
        plugin.saveConfig();
    }

    public void loadRacks() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection(CONFIG_SECTION);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                String[] parts = key.split("\\|");
                if (parts.length != 4) continue;
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) continue;
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                Location loc = new Location(world, x, y, z);
                Block block = loc.getBlock();
                if (block.getType() != Material.BARREL) continue;
                if (!(block.getState() instanceof TileState state)) continue;
                NamespacedKey rackKey = new NamespacedKey(plugin, "drying_rack");
                if (!state.getPersistentDataContainer().has(rackKey, PersistentDataType.BOOLEAN)) continue;

                DryingData data = new DryingData();
                data.progressTicks = section.getInt(key + ".progress");

                ConfigurationSection items = section.getConfigurationSection(key + ".items");
                if (items != null) {
                    for (String slotStr : items.getKeys(false)) {
                        int slot = Integer.parseInt(slotStr);
                        ItemStack item = ItemStack.deserialize(items.getConfigurationSection(slotStr).getValues(false));
                        data.inventory.setItem(slot, item);
                    }
                }
                racks.put(loc, data);
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось загрузить сушилку " + key + ": " + e.getMessage());
            }
        }
        config.set(CONFIG_SECTION, null);
        plugin.saveConfig();
    }

    public void placeRack(Location loc) {
        racks.put(loc, new DryingData());
    }

    public void breakRack(Location loc, World world) {
        DryingData data = racks.remove(loc);
        if (data == null) return;

        Inventory inv = data.inventory;
        for (int slot : new int[]{1, 3, 4}) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                world.dropItemNaturally(loc, item);
            }
        }
    }

    public Inventory getRackInventory(Location loc) {
        DryingData data = racks.computeIfAbsent(loc, l -> new DryingData());
        return data.inventory;
    }

    private void tickAll() {
        Iterator<Map.Entry<Location, DryingData>> it = racks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Location, DryingData> entry = it.next();
            Location loc = entry.getKey();
            DryingData data = entry.getValue();

            World world = loc.getWorld();

            if (!world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                continue;
            }
            if (loc.getBlock().getType() != Material.BARREL) {
                it.remove();
                continue;
            }

            Inventory inv = data.inventory;
            ItemStack input = inv.getItem(1);
            if (input == null || input.getType() == Material.AIR) {
                data.progressTicks = 0;
                updateProgress(inv, 0);
                continue;
            }

            boolean isBud = sativaItems.isBud(input) || indicaItems.isBud(input);
            if (isBud) {
                data.progressTicks += PROGRESS_PER_STEP;
                if (data.progressTicks >= TICKS_NEEDED) {
                    data.progressTicks -= TICKS_NEEDED;
                    ItemStack bud = input.clone();
                    bud.setAmount(1);
                    input.setAmount(input.getAmount() - 1);
                    if (input.getAmount() <= 0) inv.setItem(1, null);

                    ItemStack boshka = sativaItems.isBud(bud)
                            ? sativaItems.createBoshka()
                            : indicaItems.createBoshka();
                    outputItem(inv, 3, loc, boshka);
                }
                updateProgress(inv, data.progressTicks);
                continue;
            }

            if (sativaItems.isBoshka(input) || indicaItems.isBoshka(input)) {
                data.progressTicks += PROGRESS_PER_STEP;
                if (data.progressTicks >= TICKS_NEEDED) {
                    data.progressTicks -= TICKS_NEEDED;
                    input.setAmount(input.getAmount() - 1);
                    if (input.getAmount() <= 0) inv.setItem(1, null);
                    outputItem(inv, 3, loc, medicalItems.createMedicalBoshka());
                    // Дополнительно выдаём гашишное масло в слот 4
                    outputItem(inv, 4, loc, gashItems.createGashOil());
                }
                updateProgress(inv, data.progressTicks);
                continue;
            }

            if (gashItems.isShreddedWeed(input) && input.getAmount() >= 3) {
                data.progressTicks += PROGRESS_PER_STEP;
                if (data.progressTicks >= TICKS_NEEDED) {
                    data.progressTicks -= TICKS_NEEDED;
                    input.setAmount(input.getAmount() - 3);
                    if (input.getAmount() <= 0) inv.setItem(1, null);
                    outputItem(inv, 3, loc, medicalItems.createMedicalBoshka());
                    // Дополнительно выдаём гашишное масло в слот 4
                    outputItem(inv, 4, loc, gashItems.createGashOil());
                }
                updateProgress(inv, data.progressTicks);
                continue;
            }
            if (tobaccoItems.isTobacco(input)) {
                data.progressTicks += PROGRESS_PER_STEP;
                if (data.progressTicks >= TICKS_NEEDED) {
                    data.progressTicks -= TICKS_NEEDED;
                    input.setAmount(input.getAmount() - 1);
                    if (input.getAmount() <= 0) inv.setItem(1, null);
                    outputItem(inv, 3, loc, tobaccoItems.createDriedTobacco());
                }
                updateProgress(inv, data.progressTicks);
                continue;
            }
            data.progressTicks = 0;
            updateProgress(inv, 0);
        }
    }

    // Вспомогательный метод для выдачи результата в слот 3 или в мир
    private void outputItem(Inventory inv, int slot, Location loc, ItemStack result) {
        ItemStack output = inv.getItem(slot);
        if (output == null || output.getType() == Material.AIR || output.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            inv.setItem(slot, result);
        } else if (output.isSimilar(result) && output.getAmount() < 64) {
            output.setAmount(output.getAmount() + 1);
        } else {
            loc.getWorld().dropItemNaturally(loc, result);
        }
    }

    private static void updateProgress(Inventory inv, int progressTicks) {
        int percent = (progressTicks * 100) / TICKS_NEEDED;
        if (percent > 100) percent = 100;

        ItemStack progressItem = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = progressItem.getItemMeta();
        meta.displayName(Component.text("Сушка")
                .color(TextColor.color(0xFFAA00))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Прогресс: " + percent + "%")
                        .color(TextColor.color(0xAAAAAA))
        ));
        progressItem.setItemMeta(meta);

        inv.setItem(2, progressItem);
    }

    // Внутренний класс для хранения данных одной сушилки
    private static class DryingData {
        Inventory inventory;
        int progressTicks = 0;

        DryingData() {
            DryingHolder holder = new DryingHolder();
            this.inventory = Bukkit.createInventory(holder, 9, "Сушка");
            holder.setInventory(this.inventory);
            decorateInventory();
            updateProgress(inventory, 0);
        }

        private void decorateInventory() {
            ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            inventory.setItem(0, grayPane);
            for (int i = 5; i < 9; i++) {
                inventory.setItem(i, grayPane);
            }
        }
    }
    public static class DryingHolder implements InventoryHolder {
        private Inventory inventory;
        void setInventory(Inventory inventory) { this.inventory = inventory; }
        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }
}
