package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.FertilizerItems;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class PlantCareListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final NamespacedKey SATIVA_WHEAT_KEY;
    private final NamespacedKey INDICA_WHEAT_KEY;
    private final Random random = new Random();
    private final NamespacedKey FERTILIZED_KEY;
    private final FertilizerItems fertilizerItems;
    private final NamespacedKey DRY_KEY;

    public PlantCareListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.SATIVA_WHEAT_KEY = new NamespacedKey(plugin, "sativa_wheat");
        this.INDICA_WHEAT_KEY = new NamespacedKey(plugin, "indica_wheat");
        this.FERTILIZED_KEY = new NamespacedKey(plugin, "fertilized");
        this.fertilizerItems = plugin.getFertilizerItems();
        this.DRY_KEY = new NamespacedKey(plugin, "dry");

    }

    @EventHandler
    public void onRightClickWheat(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.WHEAT) return;

        Player player = event.getPlayer();
        StrainType strainType = getStrainType(block);
        if (strainType == null) return;

        Ageable ageable = (Ageable) block.getBlockData();
        int age = ageable.getAge();
        int maxAge = ageable.getMaximumAge();

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean hasShears = mainHand != null && mainHand.getType() == Material.SHEARS;
        boolean hasBoneMeal = mainHand != null && mainHand.getType() == Material.BONE_MEAL;
        boolean hasFertilizer = mainHand != null && fertilizerItems.isFertilizer(mainHand);
        boolean dry = isDry(block) && age != maxAge;

        if (dry) {
            // Полив ведром воды
            if (mainHand != null && mainHand.getType() == Material.WATER_BUCKET) {
                setDry(block, false);
                mainHand.setAmount(mainHand.getAmount() - 1);
                player.getInventory().addItem(new ItemStack(Material.BUCKET));
                player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);
                player.sendMessage("§aВы полили растение.");
                event.setCancelled(true);
                return;
            }
            hasFertilizer = false;
            hasBoneMeal = false;
            hasShears = false;
        }

        if (hasFertilizer) {
            if (age != 0) {
                player.sendMessage("§cУдобрять можно только только что посаженные кусты.");
                return;
            }
            if (isFertilized(block)) {
                player.sendMessage("§cЭтот куст уже удобрен.");
                return;
            }

            setFertilized(block, true);
            mainHand.setAmount(mainHand.getAmount() - 1);
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FARMER, 1.0f, 1.0f);
            player.sendMessage("§aВы удобрили куст. Теперь он растёт лучше!");
            return;
        }
        if (hasBoneMeal && age < maxAge) {
            int newAge = Math.min(maxAge, age + random.nextInt(3) + 1);
            ageable.setAge(newAge);
            block.setBlockData(ageable);
            mainHand.setAmount(mainHand.getAmount() - 1);
            event.setCancelled(true);
            return;
        }
        if (hasShears && age == maxAge) {
            if (mainHand.getItemMeta() instanceof Damageable damageable) {
                int currentDamage = damageable.getDamage();
                int maxDurability = mainHand.getType().getMaxDurability();
                int newDamage = currentDamage + 3;
                if (newDamage > maxDurability) {
                    mainHand.setAmount(0);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    damageable.setDamage(newDamage);
                    mainHand.setItemMeta((ItemMeta) damageable);
                }
            }

            int amount = random.nextInt(2) + 2;
            boolean fertilized = isFertilized(block);
            if (fertilized) {
                amount += random.nextInt(2) + 1;
                setFertilized(block, false);
            }
            ItemStack buds = (strainType == StrainType.SATIVA) ? sativaItems.createBud() : indicaItems.createBud();
            buds.setAmount(amount);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), buds);
            ageable.setAge(0);
            block.setBlockData(ageable);
            setBlockPDC(block, strainType == StrainType.SATIVA ? SATIVA_WHEAT_KEY : INDICA_WHEAT_KEY, true);
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
            player.sendMessage("§aВы собрали " + amount + " соцветий, и растение начало расти заново.");
            return;
        }
        event.setCancelled(true);
        String strainName = strainType == StrainType.SATIVA ? "Сативы" : "Индики";
        int growthPercent = (age * 100) / maxAge;
        boolean fertilized = isFertilized(block);
        player.sendMessage("§e=== Информация о кусте ===");
        if (age == maxAge) {
            player.sendMessage("§7Статус: §aВыросло");
        } else {
            player.sendMessage("§7Статус: " + (dry ? "§cЗасохло" : "§aВ норме"));
        }
        player.sendMessage("§7Сорт: " + strainName);
        if (growthPercent == 100) {
            player.sendMessage("§7Стадия роста: §a" + growthPercent + "%");
        } else {
            player.sendMessage("§7Стадия роста: " + growthPercent + "%");
        }
        player.sendMessage("§7Удобрение: " + (fertilized ? "§aУдобрено" : "§cНе Удобрено"));
        if (!hasShears && !hasBoneMeal && !hasFertilizer && !dry) {
            player.sendMessage("§8Возьмите ножницы, чтобы собрать соцветия.");
        } else if (dry) {
            player.sendMessage("§8Полейте растение ведром воды.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventAutoHarvest(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.WHEAT) return;
        if (getStrainType(block) == null) return;

        event.setCancelled(true);
    }

    private boolean isDry(Block block) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, "dry");
        return chunkPDC.getOrDefault(blockKey, PersistentDataType.BOOLEAN, false);
    }

    private void setDry(Block block, boolean value) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, "dry");
        if (value) chunkPDC.set(blockKey, PersistentDataType.BOOLEAN, true);
        else chunkPDC.remove(blockKey);
    }

    private StrainType getStrainType(Block block) {
        if (isStrainWheat(block, SATIVA_WHEAT_KEY)) {
            return StrainType.SATIVA;
        } else if (isStrainWheat(block, INDICA_WHEAT_KEY)) {
            return StrainType.INDICA;
        }
        return null;
    }

    private boolean isStrainWheat(Block block, NamespacedKey strainKey) {
        PersistentDataContainer chunkPDC = block.getChunk().getPersistentDataContainer();
        NamespacedKey blockKey = getBlockKey(block, strainKey.getKey());
        return chunkPDC.getOrDefault(blockKey, PersistentDataType.BOOLEAN, false);
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
    private NamespacedKey getBlockKey(Block block, String prefix) {
        return new NamespacedKey(plugin, prefix + "_x" + block.getX() + "_y" + block.getY() + "_z" + block.getZ());
    }

    private enum StrainType {
        SATIVA, INDICA
    }
}