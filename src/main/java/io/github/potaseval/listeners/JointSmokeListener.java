package io.github.potaseval.listeners;

import io.github.potaseval.items.GashItems;
import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.MedicalItems;
import io.github.potaseval.items.SativaItems;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;

public class JointSmokeListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final GashItems gashItems;
    private final OverdoseListener overdoseListener;
    private final SmokeEffectManager smokeEffectManager;
    private final DelayedEffectManager delayedEffectManager;
    private final MedicalItems medicalItems;

    public JointSmokeListener(GreatWeeb plugin, OverdoseListener overdoseListener,
                              SmokeEffectManager smokeEffectManager,DelayedEffectManager delayedEffectManager,MedicalItems medicalItems) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.gashItems = plugin.getGashItems();
        this.overdoseListener = overdoseListener;
        this.smokeEffectManager = smokeEffectManager;
        this.delayedEffectManager = delayedEffectManager;
        this.medicalItems = medicalItems;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (gashItems.isGashCake(mainHand)) {
            eatGashCake(player, mainHand);
            event.setCancelled(true);
            return;
        }

        if (gashItems.isCannaBrownie(mainHand)) {
            eatCannaBrownie(player, mainHand);
            event.setCancelled(true);
            return;
        }
        if (gashItems.isTastyCookie(mainHand)) {
            eatTastyCookie(player, mainHand);
            event.setCancelled(true);
            return;
        }

        boolean isSativaJoint = sativaItems.isJoint(mainHand);
        boolean isIndicaJoint = indicaItems.isJoint(mainHand);
        boolean isMedicalJoint = medicalItems.isMedicalJoint(mainHand);

        if (!isSativaJoint && !isIndicaJoint && !isMedicalJoint) return; // Теперь пропускаем и медицинский

        if (offHand == null || offHand.getType() != Material.FLINT_AND_STEEL) return;
        if (isSativaJoint || isIndicaJoint) {
            for (Player nearby : player.getWorld().getPlayers()) {
                if (nearby.getLocation().distanceSquared(player.getLocation()) <= 40 * 40) {
                    nearby.sendMessage("§7Чувствуется запах травки..");
                }
            }
        }

        if (offHand.getItemMeta() instanceof Damageable damageable) {
            int currentDamage = damageable.getDamage();
            int maxDurability = offHand.getType().getMaxDurability();
            int newDamage = currentDamage + 1;
            if (newDamage > maxDurability) {
                offHand.setAmount(0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                damageable.setDamage(newDamage);
                offHand.setItemMeta((ItemMeta) damageable);
            }
        }

        Location loc = player.getLocation().add(0, 1.5, 0);
        // Для медицинского – меньше частиц
        int particleCount = isMedicalJoint ? 4 : 10;
        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, particleCount, 0.3, 0.5, 0.3, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.6f, 0.7f);

        if (isSativaJoint) {
            overdoseListener.registerSmoke(player, "sativa");
            delayedEffectManager.schedule(player, "sativa", smokeEffectManager, 20 * 20);
        } else if (isIndicaJoint) {
            overdoseListener.registerSmoke(player, "indica");
            delayedEffectManager.schedule(player, "indica", smokeEffectManager, 20 * 20);
        } else if (isMedicalJoint) {
            delayedEffectManager.schedule(player, "medical", smokeEffectManager, 10 * 20);
        }

        mainHand.setAmount(mainHand.getAmount() - 1);
        event.setCancelled(true);

        String strainName = isSativaJoint ? "Сативы" : (isIndicaJoint ? "Индики" : "с медицинской травкой");
        player.sendMessage("§aВы затянулись косяком " + strainName + "...");
    }
    private void eatCannaBrownie(Player player, ItemStack item) {
        overdoseListener.registerSmoke(player, "brownie");  // риск передозировки
        delayedEffectManager.schedule(player, "brownie", smokeEffectManager, 25 * 20);
        item.setAmount(item.getAmount() - 1);
        player.sendMessage("§6Вы съели канна-брауни... что-то будет...");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
    }
    private void eatTastyCookie(Player player, ItemStack item) {
        delayedEffectManager.schedule(player, "tasty_cookie", smokeEffectManager, 10 * 20);
        item.setAmount(item.getAmount() - 1);
        player.sendMessage("§aВы съели вкусное печенье. Приятного аппетита!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
    }
    private void eatGashCake(Player player, ItemStack cake) {
        overdoseListener.registerSmoke(player, "gash");
        delayedEffectManager.schedule(player, "cake", smokeEffectManager, 30 * 20);

        cake.setAmount(cake.getAmount() - 1);
        player.sendMessage("§6Вы съели кусок гашишного пирога...");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
    }
}