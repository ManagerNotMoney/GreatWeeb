package io.github.potaseval.listeners.smoking;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.TobaccoItems;   // только этот импорт
import io.github.potaseval.managers.DelayedEffectManager;
import io.github.potaseval.managers.SmokeEffectManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CigaretteSmokeListener implements Listener {

    private final GreatWeeb plugin;
    private final TobaccoItems tobaccoItems;
    private final DelayedEffectManager delayedEffectManager;
    private final SmokeEffectManager smokeEffectManager;

    public CigaretteSmokeListener(GreatWeeb plugin, DelayedEffectManager delayedEffectManager,
                                  SmokeEffectManager smokeEffectManager) {
        this.plugin = plugin;
        this.tobaccoItems = plugin.getTobaccoItems();
        this.delayedEffectManager = delayedEffectManager;
        this.smokeEffectManager = smokeEffectManager;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (!tobaccoItems.isCigarette(mainHand)) return;   // изменено

        if (!plugin.canSmoke(player)) {
            long remaining = plugin.getSmokeCooldownRemaining(player);
            player.sendMessage("§cПодождите ещё " + remaining + " сек.");
            event.setCancelled(true);
            return;
        }

        if (offHand == null || offHand.getType() != Material.FLINT_AND_STEEL) {
            player.sendMessage("§cВозьмите зажигалку во вторую руку.");
            event.setCancelled(true);
            return;
        }

        mainHand.setAmount(mainHand.getAmount() - 1);

        delayedEffectManager.schedule(player, "cigarette", smokeEffectManager, 13 * 20);

        Location loc = player.getLocation().add(0, 1.5, 0);
        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 5, 0.3, 0.5, 0.3, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.6f, 0.7f);

        player.sendMessage("§aВы закурили сигарету.");
        plugin.setSmokeCooldown(player);
        event.setCancelled(true);
    }
}