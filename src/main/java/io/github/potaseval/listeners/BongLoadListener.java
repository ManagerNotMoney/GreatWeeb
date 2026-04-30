package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.*;
import io.github.potaseval.util.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BongLoadListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final GashItems gashItems;
    private final BongItems bongItems;
    private final MedicalItems medicalItems;

    public BongLoadListener(GreatWeeb plugin, MedicalItems medicalItems) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.gashItems = plugin.getGashItems();
        this.bongItems = plugin.getBongItems();
        this.medicalItems = medicalItems;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (!bongItems.isBong(mainHand)) return;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.FLINT_AND_STEEL) {
            return;
        }

        if (mainHand.getAmount() != 1) {
            player.sendMessage("§cБонг должен быть один в руке!");
            event.setCancelled(true);
            return;
        }

        ItemMeta meta = mainHand.getItemMeta();
        String contentId = meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "bong_content"),
                PersistentDataType.STRING
        );

        if (player.isSneaking()) {
            handleLoad(player, mainHand, offHand);
        } else {
            if (contentId != null) {
                handleUnload(player, mainHand, offHand);
            }
        }

        event.setCancelled(true);
    }

    private void handleLoad(Player player, ItemStack bong, ItemStack offHand) {
        ItemMeta meta = bong.getItemMeta();
        String existing = meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "bong_content"),
                PersistentDataType.STRING
        );
        if (existing != null) {
            player.sendMessage("§cБонг уже заправлен. Достаньте содержимое перед заправкой.");
            return;
        }

        String contentDisplay = null;
        String contentId = null;

        if (!offHand.isEmpty()) {
            if (sativaItems.isBoshka(offHand)) {
                contentDisplay = "Сатива";
                contentId = "sativa";
            } else if (indicaItems.isBoshka(offHand)) {
                contentDisplay = "Индика";
                contentId = "indica";
            } else if (gashItems.isGash(offHand)) {
                contentDisplay = "Гашиш";
                contentId = "gash";
            } else if (gashItems.isSpice(offHand)) {
                contentDisplay = "Спайс";
                contentId = "spice";
            } else if (medicalItems.isMedicalBoshka(offHand)) {
                contentDisplay = "Медицинская";
                contentId = "medical";
            }
        }

        if (contentId == null) {
            player.sendMessage("§cВозьмите бошку,Гашиш или Спайс во вторую руку.");
            return;
        }

        offHand.setAmount(offHand.getAmount() - 1);

        meta = bong.getItemMeta();
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        lore.removeIf(line -> {
            String plain = PlainTextComponentSerializer.plainText().serialize(line);
            return plain.startsWith("Содержит:");
        });

        lore.add(Component.text("Содержит: " + contentDisplay)
                .color(TextColor.color(0x00FF00))
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "bong_content"),
                PersistentDataType.STRING,
                contentId
        );
        bong.setItemMeta(meta);
        player.sendMessage("§aВы заправили бонг " + contentDisplay + ".");
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 0.8f, 1.2f);
    }

    private void handleUnload(Player player, ItemStack bong, ItemStack offHand) {
        ItemMeta meta = bong.getItemMeta();
        String contentId = meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "bong_content"),
                PersistentDataType.STRING
        );

        ItemStack toGive = null;
        switch (contentId) {
            case "sativa":
                toGive = sativaItems.createBoshka();
                break;
            case "indica":
                toGive = indicaItems.createBoshka();
                break;
            case "gash":
                toGive = gashItems.createGash();
                break;
            case "spice":
                toGive = gashItems.createSpice();
                break;
            case "medical":
                toGive = medicalItems.createMedicalBoshka();
                break;
        }

        if (toGive == null) return;

        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "bong_content"));
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(line -> {
            String plain = PlainTextComponentSerializer.plainText().serialize(line);
            return plain.startsWith("Содержит:");
        });
        meta.lore(lore);
        bong.setItemMeta(meta);

        if (!offHand.isEmpty()) {
            ItemUtils.giveOrDrop(player, toGive);
        } else {
            player.getInventory().setItemInOffHand(toGive);
        }

        player.sendMessage("§aВы достали " +
                PlainTextComponentSerializer.plainText().serialize(toGive.getItemMeta().displayName()) +
                " из бонга.");
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_EMPTY, 0.8f, 1.0f);
    }
}
