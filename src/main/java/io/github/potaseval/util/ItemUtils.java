package io.github.potaseval.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public final class ItemUtils {

    private ItemUtils() {}

    public static boolean isCustomItemType(ItemStack item, NamespacedKey typeKey, String expectedType) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        return expectedType.equals(type);
    }
    public static boolean damageItem(ItemStack item, int damage, Player player) {
        if (!(item.getItemMeta() instanceof Damageable damageable)) return false;

        int maxDurability = item.getType().getMaxDurability();
        int currentDamage = damageable.getDamage();
        int newDamage = currentDamage + damage;

        if (newDamage > maxDurability) {
            item.setAmount(0);
            if (player != null) {
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
            return true;
        } else {
            damageable.setDamage(newDamage);
            item.setItemMeta((ItemMeta) damageable);
            return false;
        }
    }
    public static void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }
}