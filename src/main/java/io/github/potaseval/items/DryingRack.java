package io.github.potaseval.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class DryingRack {

    private final JavaPlugin plugin;
    private final NamespacedKey ITEM_TYPE_KEY;

    public DryingRack(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
    }

    public boolean isDryingRack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer()
                .get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "drying_rack".equals(type);
    }

    public ItemStack createDryingRack() {
        ItemStack item = new ItemStack(Material.BARREL);   // <--- БОЧКА ВМЕСТО ВЕРСТАКА
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Сушка")
                .color(TextColor.color(0x8B5A2B))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Медленно сушит соцветия без топлива.", TextColor.color(0xAAAAAA)),
                Component.text("Поставьте и откройте для загрузки.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "drying_rack");
        item.setItemMeta(meta);
        return item;
    }
}
