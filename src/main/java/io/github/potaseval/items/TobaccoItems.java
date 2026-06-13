package io.github.potaseval.items;

import io.github.potaseval.util.ItemUtils;
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
import java.util.Collections;

public class TobaccoItems {

    private final JavaPlugin plugin;
    private final NamespacedKey ITEM_TYPE_KEY;
    private final NamespacedKey ITEM_ID_KEY;

    // Старые константы ID
    private static final int TOBACCO_SEED_ID = 9001;
    private static final int TOBACCO_ID = 9002;
    private static final int BOX_FERTILIZER_ID = 9003;
    private static final int CIGARETTE_PACK_ID = 9004;
    private static final int CIGARETTE_BLOCK_ID = 9005;

    // Новые константы для сигареты и фильтра
    private static final int FILTER_ID = 10001;
    private static final int CIGARETTE_ID = 10002;

    // Новые предметы
    private static final int DRIED_TOBACCO_ID = 9006;
    private static final int SHREDDED_TOBACCO_ID = 9007;

    public TobaccoItems(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
        this.ITEM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
    }
    public boolean isDriedTobacco(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "dried_tobacco");
    }

    public ItemStack createDriedTobacco() {
        ItemStack item = new ItemStack(Material.TALL_DRY_GRASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Высушенный табак")
                .color(TextColor.color(0xAAAA00))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Готов к измельчению.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "dried_tobacco");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, DRIED_TOBACCO_ID);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isShreddedTobacco(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "shredded_tobacco");
    }

    public ItemStack createShreddedTobacco() {
        ItemStack item = new ItemStack(Material.LEAF_LITTER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Измельчённый табак")
                .color(TextColor.color(0x55FF55))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Измельчённый высушенный табак.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "shredded_tobacco");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, SHREDDED_TOBACCO_ID);
        item.setItemMeta(meta);
        return item;
    }
    public boolean isTobaccoSeed(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "tobacco_seed");
    }

    public boolean isTobacco(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "tobacco");
    }

    public ItemStack createSeed() {
        ItemStack item = new ItemStack(Material.MELON_SEEDS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Семена табака")
                .color(TextColor.color(0xAAAA00))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Вырастет в куст табака.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "tobacco_seed");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, TOBACCO_SEED_ID);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createTobacco() {
        ItemStack item = new ItemStack(Material.WHEAT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Табак")
                .color(TextColor.color(0x55FF55))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("собранный лист табака.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "tobacco");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, TOBACCO_ID);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isBoxFertilizer(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "box_fertilizer");
    }

    public ItemStack createBoxFertilizer() {
        ItemStack item = new ItemStack(Material.BONE_MEAL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Боксовое удобрение")
                .color(TextColor.color(0xFFD700))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text("Мощный стимулятор роста.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "box_fertilizer");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, BOX_FERTILIZER_ID);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isCigarettePack(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "cigarette_pack");
    }

    public ItemStack createCigarettePack() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Пачка сигарет")
                .color(TextColor.color(0xCCCCCC))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Стандартная пачка сигарет.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "cigarette_pack");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, CIGARETTE_PACK_ID);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isCigaretteBlock(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "cigarette_block");
    }

    public ItemStack createCigaretteBlock() {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Блок сигарет")
                .color(TextColor.color(0xCCCCCC))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Спрессованные пачки сигарет.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "cigarette_block");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, CIGARETTE_BLOCK_ID);
        item.setItemMeta(meta);
        return item;
    }

    // ----------------------------------
    //  Методы для сигареты (из CigaretteItems)
    // ----------------------------------

    public boolean isCigarette(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "cigarette");
    }

    public ItemStack createCigarette() {
        ItemStack item = new ItemStack(Material.BREEZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Сигарета")
                .color(TextColor.color(0xFFCC80))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Пахнет табаком.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "cigarette");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, CIGARETTE_ID);
        item.setItemMeta(meta);
        return item;
    }

    // ----------------------------------
    //  Методы для фильтра (из FilterItems)
    // ----------------------------------

    public boolean isFilter(ItemStack item) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, "filter");
    }

    public ItemStack createFilter() {
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Фильтр")
                .color(TextColor.color(0xFFFFFF))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Collections.singletonList(
                Component.text("Для изготовления сигарет.", TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "filter");
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, FILTER_ID);
        item.setItemMeta(meta);
        return item;
    }
}