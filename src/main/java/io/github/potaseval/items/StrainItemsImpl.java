package io.github.potaseval.items;

import io.github.potaseval.RecipeManager;
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

public class StrainItemsImpl implements RecipeManager.StrainItems {

    private final JavaPlugin plugin;
    private final String strainName;
    private final String displayNameRu;
    private final TextColor displayColor;
    private final int baseItemId;

    private final Material seedMaterial;
    private final Material budMaterial;
    private static final Material BOSHKA_MATERIAL = Material.GREEN_DYE;
    private static final Material JOINT_MATERIAL = Material.STICK;
    private static final Material BRIQUETTE_MATERIAL = Material.DRIED_KELP;
    private static final Material PACK_MATERIAL = Material.DRIED_KELP_BLOCK;

    private final NamespacedKey ITEM_TYPE_KEY;
    private final NamespacedKey ITEM_ID_KEY;

    public StrainItemsImpl(JavaPlugin plugin, String strainName, String displayNameRu,
                           TextColor displayColor, int baseItemId,
                           Material seedMaterial, Material budMaterial) {
        this.plugin = plugin;
        this.strainName = strainName;
        this.displayNameRu = displayNameRu;
        this.displayColor = displayColor;
        this.baseItemId = baseItemId;
        this.seedMaterial = seedMaterial;
        this.budMaterial = budMaterial;
        this.ITEM_TYPE_KEY = new NamespacedKey(plugin, "custom_item_type");
        this.ITEM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
    }

    public boolean isSeed(ItemStack item) {
        return checkItemType(item, strainName + "_seed");
    }

    public boolean isBud(ItemStack item) {
        return checkItemType(item, strainName + "_bud");
    }

    public boolean isBoshka(ItemStack item) {
        return checkItemType(item, strainName + "_boshka");
    }

    public boolean isJoint(ItemStack item) {
        return checkItemType(item, strainName + "_joint");
    }

    public boolean isBriquette(ItemStack item) {
        return checkItemType(item, strainName + "_briquette");
    }

    public boolean isPack(ItemStack item) {
        return checkItemType(item, strainName + "_pack");
    }

    private boolean checkItemType(ItemStack item, String expectedType) {
        return ItemUtils.isCustomItemType(item, ITEM_TYPE_KEY, expectedType);
    }

    @Override
    public ItemStack createSeed() {
        return createItem(seedMaterial,
                "Семена " + displayNameRu,
                "Приятно пахнущие семена " + displayNameRu.toLowerCase() + ".",
                strainName + "_seed",
                baseItemId);
    }

    @Override
    public ItemStack createBud() {
        return createItem(budMaterial,
                "Соцветие " + displayNameRu,
                (strainName.equals("sativa") ?
                        "Ароматное соцветие, полное силы." :
                        "Тяжёлое, смолистое соцветие."),
                strainName + "_bud",
                baseItemId + 1);
    }

    @Override
    public ItemStack createBoshka() {
        return createItem(BOSHKA_MATERIAL,
                "Бошка " + displayNameRu,
                "Высушенное соцветие " + displayNameRu.toLowerCase() + ".",
                strainName + "_boshka",
                baseItemId + 2);
    }

    @Override
    public ItemStack createJoint() {
        return createItem(JOINT_MATERIAL,
                "Косяк " + displayNameRu,
                "Скрученный косяк " + displayNameRu.toLowerCase() + ".",
                strainName + "_joint",
                baseItemId + 3);
    }

    @Override
    public ItemStack createBriquette() {
        return createItem(BRIQUETTE_MATERIAL,
                "Брикет " + displayNameRu,
                "Спрессованные бошки " + displayNameRu.toLowerCase() + ".",
                strainName + "_briquette",
                baseItemId + 4);
    }

    @Override
    public ItemStack createPack() {
        return createItem(PACK_MATERIAL,
                "Пак " + displayNameRu,
                "Упакованные брикеты " + displayNameRu.toLowerCase() + ".",
                strainName + "_pack",
                baseItemId + 5);
    }

    private ItemStack createItem(Material material, String displayName, String loreLine,
                                 String typeKey, int id) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName)
                .color(displayColor)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.asList(
                Component.text(loreLine, TextColor.color(0xAAAAAA))
        ));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, typeKey);
        pdc.set(ITEM_ID_KEY, PersistentDataType.INTEGER, id);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String getName() {
        return strainName;
    }
}