package io.github.potaseval.items;

import io.github.potaseval.RecipeManager;
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

public class GashItems implements RecipeManager.StrainItems {

    private final JavaPlugin plugin;
    private final NamespacedKey ITEM_TYPE_KEY;
    private final NamespacedKey ITEM_ID_KEY;

    public GashItems(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
        this.ITEM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
    }
    public boolean isCannaBrownie(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String type = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "canna_brownie".equals(type);
    }
    public boolean isTastyCookie(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String type = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "tasty_cookie".equals(type);
    }
    public boolean isGashCake(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String type = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "gash_cake".equals(type);
    }
    public boolean isShreddedWeed(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String type = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "shredded_weed".equals(type);
    }

    public boolean isGash(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String type = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "gash".equals(type);
    }
    public boolean isGashOil(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String type = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "gash_oil".equals(type);
    }
    public boolean isSpice(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String type = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "spice".equals(type);
    }

    public ItemStack createGash() {
        ItemStack item = new ItemStack(Material.BROWN_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Гашиш")
                .color(TextColor.color(0x8B4513))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Cмола конопли.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "gash");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 4001);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createCannaBrownie() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Канна-брауни")
                .color(TextColor.color(0x8B4513))  // коричневый
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Шоколадное пирожное с секретом.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "canna_brownie");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 4006);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createTastyCookie() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Вкусное печенье")
                .color(TextColor.color(0x99FF99))  // светло-зелёный (как medical)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Полезное лакомство с медицинской бошкой.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "tasty_cookie");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 4007);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createGashOil() {
        ItemStack item = new ItemStack(Material.RESIN_BRICK); // или Material.HONEYCOMB
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Гашишное масло")
                .color(TextColor.color(0xDAA520)) // Золотисто-коричневый
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Концентрированное масло из гашиша.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "gash_oil");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 4002);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createSpice() {
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Спайс")
                .color(TextColor.color(0xFF5555))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Синтетический наркотик. Очень опасен.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "spice");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 4005);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createShreddedWeed() {
        ItemStack item = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Измельченная Марихуана")
                .color(TextColor.color(0x00FF00))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Измельченные соцветия конопли.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "shredded_weed");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 4004);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createGashCake() {
        ItemStack item = new ItemStack(Material.PUMPKIN_PIE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Гашишный пирог")
                .color(TextColor.color(0x8B4513))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Сытный пирог с особым ингредиентом.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "gash_cake");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, 4003);
        item.setItemMeta(meta);
        return item;
    }
    @Override
    public String getName() {
        return "gash";
    }

    @Override
    public ItemStack createSeed() {
        return null; // Не используется
    }

    @Override
    public ItemStack createBud() {
        return null;
    }

    @Override
    public ItemStack createBoshka() {
        return null; // Условно
    }

    @Override
    public ItemStack createJoint() {
        return null;
    }

    @Override
    public ItemStack createBriquette() {
        return null;
    }

    @Override
    public ItemStack createPack() {
        return null; // Условно
    }
}
