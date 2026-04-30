package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.GashItems;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import io.github.potaseval.util.ItemUtils;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class BriquetteUseListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final NamespacedKey BRAND_KEY;
    private final GashItems gashItems;

    public BriquetteUseListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.gashItems = plugin.getGashItems();
        this.BRAND_KEY = new NamespacedKey(plugin, "briquette_brand");
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean isSativa = sativaItems.isBriquette(mainHand);
        boolean isIndica = indicaItems.isBriquette(mainHand);
        boolean isBriquette = isSativa || isIndica;
        boolean isSpiceBriquette = gashItems.isSpiceBriquette(mainHand);
        boolean isGashBriquette = gashItems.isGashBriquette(mainHand);
        if (isSpiceBriquette) {
            isBriquette = true;
        }
        if (isGashBriquette) {
            isBriquette = true;
        }

        if (!isBriquette) return;


        if (player.isSneaking()) {
            boolean hasRenamedPaper = offHand != null && offHand.getType() == Material.PAPER &&
                    offHand.hasItemMeta() && offHand.getItemMeta().hasDisplayName();

            if (hasRenamedPaper) {
                ItemMeta paperMeta = offHand.getItemMeta();
                String brandName = PlainTextComponentSerializer.plainText().serialize(paperMeta.displayName());
                applyBrand(mainHand, brandName);
                offHand.setAmount(offHand.getAmount() - 1);
                player.sendMessage("§aБренд '" + brandName + "' добавлен к брикету!");
                event.setCancelled(true);
                return;
            } else {
                int amountInHand = mainHand.getAmount();
                mainHand.setAmount(0);

                ItemStack result;
                String itemName;
                if (isSativa) {
                    result = sativaItems.createBoshka();
                    itemName = "Сативы";
                } else if (isIndica) {
                    result = indicaItems.createBoshka();
                    itemName = "Индики";
                } else if (isGashBriquette) {
                    result = gashItems.createGash();
                    itemName = "Гашиша";
                } else {
                    result = gashItems.createSpice();
                    itemName = "Спайса";
                }
                result.setAmount(amountInHand * 8);
                ItemUtils.giveOrDrop(player, result);
                player.sendMessage("§aВы разломали " + amountInHand + " брикет(ов) " + itemName + " и получили " + (amountInHand * 8) + " "
                        + (isSativa || isIndica ? "бошек" : (isGashBriquette ? "гашиша" : "спайса")) + " " + itemName + "!");
                return;
            }
        }
        mainHand.setAmount(mainHand.getAmount() - 1);
        event.setCancelled(true);

        ItemStack result;
        String itemName;
        if (isSativa) {
            result = sativaItems.createBoshka();
            itemName = "Сативы";
        } else if (isIndica) {
            result = indicaItems.createBoshka();
            itemName = "Индики";
        } else if (isGashBriquette) {
            result = gashItems.createGash();
            itemName = "Гашиша";
        } else {
            result = gashItems.createSpice();
            itemName = "Спайса";
        }

        result.setAmount(8);
        player.getInventory().addItem(result).forEach((index, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );
        player.sendMessage("§aВы разломали брикет и получили 8 "
                + (isSativa || isIndica ? "бошек" : (isGashBriquette ? "гашиша" : "спайса")) + " " + itemName + "!");
    }

    private void applyBrand(ItemStack briquette, String brandName) {
        ItemMeta meta = briquette.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        pdc.set(BRAND_KEY, PersistentDataType.STRING, brandName);

        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        lore.removeIf(line -> {
            String plain = PlainTextComponentSerializer.plainText().serialize(line);
            return plain.startsWith("Бренд: ");
        });
        lore.add(Component.text("Бренд: " + brandName).color(TextColor.color(0xAAAAAA)));
        meta.lore(lore);

        briquette.setItemMeta(meta);
    }
}