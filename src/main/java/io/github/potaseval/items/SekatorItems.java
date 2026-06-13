package io.github.potaseval.items;

import io.github.potaseval.util.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class SekatorItems {

    private final JavaPlugin plugin;
    private final NamespacedKey ITEM_TYPE_KEY;
    private final NamespacedKey ITEM_ID_KEY;

    public SekatorItems(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
        this.ITEM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
    }

    public boolean isSekator(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "sekator");
    }

    public ItemStack createSekator() {
        ItemStack item = new ItemStack(Material.SHEARS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Секатор")
                .color(TextColor.color(0x888888))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Острый садовый секатор.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "sekator");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 8001);
        item.setItemMeta(meta);
        return item;
    }
    public boolean isWateringCan(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "watering_can");
    }
    public ItemStack createEmptyWateringCan() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Лейка")
                .color(TextColor.color(0x5599FF))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Пустая лейка для полива.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "watering_can");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 8002);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createFilledWateringCan() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Лейка")
                .color(TextColor.color(0x5599FF))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Наполнена водой.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "watering_can");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 8002);
        pdc.set(new NamespacedKey(plugin, "watering_can_water"), PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }
}