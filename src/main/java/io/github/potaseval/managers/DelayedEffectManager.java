package io.github.potaseval.managers;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DelayedEffectManager {

    private final Map<UUID, BukkitTask> pendingTasks = new HashMap<>();

    public void schedule(Player player, String strain, SmokeEffectManager smokeManager, long delayTicks) {
        UUID uuid = player.getUniqueId();
        cancel(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 5 * 20, 0));

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                applyEffects(player, strain, smokeManager);
                pendingTasks.remove(uuid);
            }
        }.runTaskLater(smokeManager.getPlugin(), delayTicks);
        pendingTasks.put(uuid, task);
    }

    public void cancel(Player player) {
        BukkitTask task = pendingTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public void cancelAll() {
        pendingTasks.values().forEach(BukkitTask::cancel);
        pendingTasks.clear();
    }

    private void applyEffects(Player player, String strain, SmokeEffectManager smokeManager) {
        switch (strain) {
            case "sativa" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 7 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60 * 20, 1));
                smokeManager.start(player);
            }
            case "indica" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60 * 40, 0));
                smokeManager.start(player);
            }
            case "gash" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30 * 20, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 8 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100 * 20, 2));
                smokeManager.start(player);
            }
            case "spice" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120 * 20, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 50 * 20, 0));
                smokeManager.start(player);
            }
            case "cake" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60 * 20, 0));
            }
            case "medical" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 5 * 20, 0));
            }
            case "brownie" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 80 * 20, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 10 * 20, 0));
                smokeManager.start(player);
            }
            case "tasty_cookie" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 10 * 20, 0));
            }
        }
    }
}
