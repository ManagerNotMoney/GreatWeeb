package io.github.potaseval.items;

import io.github.potaseval.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;

public class FertilizerItems {

    private final JavaPlugin plugin;
    private final NamespacedKey ITEM_TYPE_KEY;
    private final NamespacedKey ITEM_ID_KEY;

    public FertilizerItems(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
        this.ITEM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
    }

    public boolean isFertilizer(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "fertilizer");
    }

    public ItemStack createFertilizer() {
        ItemStack item = new ItemStack(Material.BONE_MEAL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Удобрение")
                .color(TextColor.color(0x8B4513))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Удобряет кусты мариуханы.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "fertilizer");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 6001);
        item.setItemMeta(meta);
        return item;
    }
}
