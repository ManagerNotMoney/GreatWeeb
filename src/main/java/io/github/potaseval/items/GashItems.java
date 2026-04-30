package io.github.potaseval.items;

import io.github.potaseval.RecipeManager;
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

public class GashItems implements RecipeManager.StrainItems {

    private final JavaPlugin plugin;
    private final NamespacedKey ITEM_TYPE_KEY;
    private final NamespacedKey ITEM_ID_KEY;
    private static final int SPICE_BRIQUETTE_ID = 4008;
    private static final int SPICE_PACK_ID = 4009;
    private static final int GASH_BRIQUETTE_ID = 4010;
    private static final int GASH_PACK_ID = 4011;

    public GashItems(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
        this.ITEM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
    }
    public boolean isGashBriquette(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "gash_briquette".equals(type);
    }

    public boolean isGashPack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "gash_pack".equals(type);
    }

    public ItemStack createGashBriquette() {
        ItemStack item = new ItemStack(Material.DRIED_KELP);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Брикет гашиша")
                .color(TextColor.color(0x8B4513))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Спрессованный гашиш.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "gash_briquette");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, GASH_BRIQUETTE_ID);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createGashPack() {
        ItemStack item = new ItemStack(Material.DRIED_KELP_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Пак гашиша")
                .color(TextColor.color(0x8B4513))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Упакованные брикеты гашиша.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "gash_pack");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, GASH_PACK_ID);
        item.setItemMeta(meta);
        return item;
    }
    public boolean isSpiceBriquette(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "spice_briquette".equals(type);
    }
    public boolean isSpicePack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        return "spice_pack".equals(type);
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
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "gash");
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
    public ItemStack createSpiceBriquette() {
        ItemStack item = new ItemStack(Material.DRIED_KELP);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Брикет спайса")
                .color(TextColor.color(0xFF5555))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Спрессованный спайс.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "spice_briquette");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, SPICE_BRIQUETTE_ID);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createSpicePack() {
        ItemStack item = new ItemStack(Material.DRIED_KELP_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Пак спайса")
                .color(TextColor.color(0xFF5555))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Упакованные брикеты спайса.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "spice_pack");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, SPICE_PACK_ID);
        item.setItemMeta(meta);
        return item;
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
                .color(TextColor.color(0x8B4513))
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
                .color(TextColor.color(0x99FF99))
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
        ItemStack item = new ItemStack(Material.RESIN_BRICK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Гашишное масло")
                .color(TextColor.color(0xDAA520))
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
        return null;
    }

    @Override
    public ItemStack createBud() {
        return null;
    }

    @Override
    public ItemStack createBoshka() {
        return null;
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
        return null;
    }
}
