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

public class MedicalItems {

    private final JavaPlugin plugin;
    private final NamespacedKey ITEM_TYPE_KEY;
    private final NamespacedKey ITEM_ID_KEY;

    public MedicalItems(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
        this.ITEM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
    }

    public boolean isMedicalBoshka(ItemStack item) {
        return checkType(item, "medical_boshka");
    }

    public boolean isMedicalJoint(ItemStack item) {
        return checkType(item, "medical_joint");
    }

    private boolean checkType(ItemStack item, String type) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String stored = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return type.equals(stored);
    }

    public ItemStack createMedicalBoshka() {
        ItemStack item = new ItemStack(Material.GREEN_DYE); // можно другой материал
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Медицинская бошка")
                .color(TextColor.color(0x99FF99))  // светло-зелёный
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Очищенная и безопасная.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "medical_boshka");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 7001);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createMedicalJoint() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Медицинский косяк")
                .color(TextColor.color(0x99FF99))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Слабый, но лечебный.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "medical_joint");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 7002);
        item.setItemMeta(meta);
        return item;
    }
}
