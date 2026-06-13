package io.github.potaseval.listeners.plant;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.SekatorItems;
import io.github.potaseval.util.PlantDataUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class WateringCanListener implements Listener {

    private final GreatWeeb plugin;
    private final SekatorItems sekatorItems;
    private final NamespacedKey WATER_KEY;

    public WateringCanListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sekatorItems = plugin.getSekatorItems();
        this.WATER_KEY = new NamespacedKey(plugin, "watering_can_water");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!sekatorItems.isWateringCan(item)) return;

        Block targetBlock = player.getTargetBlockExact(5, FluidCollisionMode.SOURCE_ONLY);
        if (targetBlock == null) return;

        ItemMeta meta = item.getItemMeta();
        boolean hasWater = meta.getPersistentDataContainer().getOrDefault(WATER_KEY, PersistentDataType.BOOLEAN, false);

        if (targetBlock.getType() == Material.WATER) {
            if (hasWater) {
                player.sendMessage("§7Лейка уже наполнена.");
            } else {
                ItemStack filled = sekatorItems.createFilledWateringCan();
                filled.setAmount(item.getAmount());
                player.getInventory().setItemInMainHand(filled);
                player.playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL, 1.0f, 1.0f);
                player.sendMessage("§aВы наполнили лейку водой.");
            }
            event.setCancelled(true);
            return;
        }

        if (targetBlock.getType() == Material.WHEAT && !player.isSneaking()) {
            if (!hasWater) {
                player.sendMessage("§cЛейка пуста. Наполните её из воды.");
                return;
            }
            if (PlantDataUtils.getStrainType(targetBlock) == null) return;

            int radius = 1;
            int count = 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = targetBlock.getRelative(dx, 0, dz);
                    if (b.getType() == Material.WHEAT && PlantDataUtils.getStrainType(b) != null) {
                        if (PlantDataUtils.isDry(b)) {
                            PlantDataUtils.setDry(b, false);
                            count++;
                        }
                    }
                }
            }
            if (count > 0) {
                ItemStack empty = sekatorItems.createEmptyWateringCan();
                empty.setAmount(item.getAmount());
                player.getInventory().setItemInMainHand(empty);
                player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, 0.8f, 1.0f);
                player.sendMessage("§aВы полили " + count + " кустов вокруг.");
            } else {
                player.sendMessage("§7Все кусты вокруг и так увлажнены.");
            }
            event.setCancelled(true);
        }
    }
}