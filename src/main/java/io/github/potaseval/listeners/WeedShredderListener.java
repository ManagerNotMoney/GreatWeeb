package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.GashItems;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class WeedShredderListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final GashItems gashItems;

    public WeedShredderListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.gashItems = plugin.getGashItems();
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean isSativaBoshka = sativaItems.isBoshka(mainHand);
        boolean isIndicaBoshka = indicaItems.isBoshka(mainHand);
        if (!isSativaBoshka && !isIndicaBoshka) return;

        if (offHand == null || offHand.getType() != Material.SHEARS) return;

        ItemMeta meta = offHand.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;

        int currentDamage = damageable.getDamage();
        int maxDurability = offHand.getType().getMaxDurability();
        int requiredDamage = 5;

        if (currentDamage + requiredDamage > maxDurability) {
            player.sendMessage("§cНожницы слишком изношены для измельчения.");
            return;
        }

        int amount = mainHand.getAmount();
        damageable.setDamage(currentDamage + requiredDamage);
        offHand.setItemMeta((ItemMeta) damageable);

        mainHand.setAmount(0);

        int totalShredded = amount * 2;
        ItemStack shredded = gashItems.createShreddedWeed();

        while (totalShredded > 0) {
            int stackSize = Math.min(totalShredded, 64);
            ItemStack stack = shredded.clone();
            stack.setAmount(stackSize);
            player.getInventory().addItem(stack).forEach((index, leftover) ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover)
            );
            totalShredded -= stackSize;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
        player.sendMessage("§aВы измельчили " + amount + " бошек.");
        event.setCancelled(true);
    }
}