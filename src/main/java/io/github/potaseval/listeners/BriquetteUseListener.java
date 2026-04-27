package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
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

    public BriquetteUseListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
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
                ItemStack boshka = isSativa ? sativaItems.createBoshka() : indicaItems.createBoshka();
                boshka.setAmount(amountInHand * 8);
                player.getInventory().addItem(boshka).forEach((index, leftover) ->
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover)
                );
                String strainName = isSativa ? "Сативы" : "Индики";
                player.sendMessage("§aВы разломали " + amountInHand + " брикет(ов) " + strainName + " и получили " + (amountInHand * 8) + " бошек!");
                event.setCancelled(true);
                return;
            }
        }

        mainHand.setAmount(mainHand.getAmount() - 1);
        event.setCancelled(true);

        ItemStack boshka = isSativa ? sativaItems.createBoshka() : indicaItems.createBoshka();
        boshka.setAmount(8);
        player.getInventory().addItem(boshka).forEach((index, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );
        String strainName = isSativa ? "Сативы" : "Индики";
        player.sendMessage("§aВы разломали брикет и получили 8 бошек " + strainName + "!");
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