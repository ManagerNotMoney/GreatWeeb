package io.github.potaseval.listeners;

import io.github.potaseval.*;
import io.github.potaseval.items.*;
import io.github.potaseval.managers.DelayedEffectManager;
import io.github.potaseval.managers.SmokeEffectManager;
import io.github.potaseval.util.ItemUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BongSmokeListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final GashItems gashItems;
    private final BongItems bongItems;
    private final OverdoseListener overdoseListener;
    private final SmokeEffectManager smokeEffectManager;
    private final DelayedEffectManager delayedEffectManager;
    private final MedicalItems medicalItems;

    public BongSmokeListener(GreatWeeb plugin, OverdoseListener overdoseListener,
                             SmokeEffectManager smokeEffectManager, DelayedEffectManager delayedEffectManager, MedicalItems medicalItems) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.gashItems = plugin.getGashItems();
        this.bongItems = plugin.getBongItems();
        this.overdoseListener = overdoseListener;
        this.smokeEffectManager = smokeEffectManager;
        this.delayedEffectManager = delayedEffectManager;
        this.medicalItems = medicalItems;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBongSmoke(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!bongItems.isBong(mainHand)) return;// проверочка на то бонг ли это (я просто тупой забывая че ето))
        if (!plugin.canSmoke(player)) {
            long remaining = plugin.getSmokeCooldownRemaining(player);
            player.sendMessage("§cПодождите ещё " + remaining + " сек.");
            event.setCancelled(true);
            return;
        }
        if (mainHand.getAmount() != 1) {
            player.sendMessage("§cБонг должен быть один в руке!");
            event.setCancelled(true);
            return;
        }

        if (offHand == null || offHand.getType() != Material.FLINT_AND_STEEL) {
            player.sendMessage("§cВозьмите зажигалку во вторую руку.");
            event.setCancelled(true);
            return;
        }

        ItemMeta meta = mainHand.getItemMeta();
        String contentId = meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "bong_content"),
                PersistentDataType.STRING
        );

        if (contentId == null) {
            player.sendMessage("§cБонг пуст. Заправьте его бошкой или гашишем.");
            event.setCancelled(true);
            return;
        }

        ItemUtils.damageItem(offHand, 1, player);

        String strainName = null;
        switch (contentId) {
            case "sativa":
                strainName = "Сативы";
                overdoseListener.registerSmoke(player, "sativa");
                delayedEffectManager.schedule(player, "sativa", smokeEffectManager, 10 * 20);
                break;
            case "indica":
                strainName = "Индики";
                overdoseListener.registerSmoke(player, "indica");
                delayedEffectManager.schedule(player, "indica", smokeEffectManager, 10 * 20);
                break;
            case "gash":
                strainName = "Гашиша";
                overdoseListener.registerSmoke(player, "gash");
                delayedEffectManager.schedule(player, "gash", smokeEffectManager, 10 * 20);
                break;
            case "spice":
                strainName = "Спайса";
                overdoseListener.registerSmoke(player, "spice");
                delayedEffectManager.schedule(player, "spice", smokeEffectManager, 10 * 20);
                break;
            case "medical":
                strainName = "медицинской травкой";
                delayedEffectManager.schedule(player, "medical", smokeEffectManager, 10 * 20);
                break;
        }

        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "bong_content"));
        if (meta.hasLore()) {
            var lore = meta.lore();
            if (lore != null) {
                lore.removeIf(line -> {
                    String plain = PlainTextComponentSerializer.plainText().serialize(line);
                    return plain.startsWith("Содержит:");
                });
                meta.lore(lore);
            }
        }
        mainHand.setItemMeta(meta);

        Location loc = player.getLocation().add(0, 1.5, 0);
        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 20, 0.5, 0.8, 0.5, 0.05);
        player.getWorld().spawnParticle(Particle.SMOKE, loc, 30, 0.4, 0.6, 0.4, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.7f, 0.4f);
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_EMPTY, 0.8f, 1.0f);

        player.sendMessage("§aВы сделали глубокую затяжку из бонга с " + strainName + "!");
        plugin.setSmokeCooldown(player);
        event.setCancelled(true);

        // Запашок для окружающих
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distanceSquared(player.getLocation()) <= 60 * 60) {
                nearby.sendMessage("§7Вы чувствуете запах травки...");
            }
        }
    }
}