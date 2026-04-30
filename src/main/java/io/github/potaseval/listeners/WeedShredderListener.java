package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.GashItems;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import io.github.potaseval.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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

    // ЧОРНЫЙ ПАПИН ТАНК
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean isSativaBud = sativaItems.isBud(mainHand);
        boolean isIndicaBud = indicaItems.isBud(mainHand);
        boolean isSativaBoshka = sativaItems.isBoshka(mainHand);
        boolean isIndicaBoshka = indicaItems.isBoshka(mainHand);

        if (!isSativaBud && !isIndicaBud && !isSativaBoshka && !isIndicaBoshka) return;
        if (offHand == null || offHand.getType() != Material.SHEARS) return;

        boolean broken = ItemUtils.damageItem(offHand, 5, player);
        if (broken) {
            event.setCancelled(true);
            return;
        }

        int multiplier = (isSativaBud || isIndicaBud) ? 3 : 2;
        int amount = mainHand.getAmount();

        mainHand.setAmount(0);

        int totalShredded = amount * multiplier;
        ItemStack shredded = gashItems.createShreddedWeed();
        while (totalShredded > 0) {
            int stackSize = Math.min(totalShredded, 64);
            ItemStack stack = shredded.clone();
            stack.setAmount(stackSize);
            ItemUtils.giveOrDrop(player, stack);
            totalShredded -= stackSize;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
        player.sendMessage("§aВы измельчили " + amount + " предметов, получив " + amount * multiplier + " измельчённой марихуаны.");
        event.setCancelled(true);
    }
}
// извини я наверное поторопился
// извини меня братан ты не добился