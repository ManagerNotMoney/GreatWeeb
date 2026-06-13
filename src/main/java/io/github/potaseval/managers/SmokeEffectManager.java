package io.github.potaseval.managers;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SmokeEffectManager {
    private final JavaPlugin plugin;
    private final Map<UUID, BukkitRunnable> smokeTasks = new HashMap<>();

    public static final Color GREEN_SMOKE = Color.fromRGB(0, 255, 0);
    public static final Color GREY_SMOKE = Color.fromRGB(105, 105, 105);

    public SmokeEffectManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void start(Player player) {
        start(player, GREEN_SMOKE);
    }
    public void start(Player player, Color color) {
        UUID uuid = player.getUniqueId();
        stop(player);

        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60; // 60 * 200 тиков = 10 минут

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    smokeTasks.remove(uuid);
                    return;
                }
                if (ticks >= maxTicks) {
                    cancel();
                    smokeTasks.remove(uuid);
                    return;
                }
                Location loc = player.getLocation().add(0, 2.2, 0);
                Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.5f);
                player.getWorld().spawnParticle(Particle.DUST, loc, 8, 0.5, 0.5, 0.5, 0, dustOptions);
                ticks++;
            }
        };

        task.runTaskTimer(plugin, 0L, 200L);
        smokeTasks.put(uuid, task);
    }

    public void stop(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable task = smokeTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
    public JavaPlugin getPlugin() {
        return plugin;
    }
    public void stopAll() {
        smokeTasks.values().forEach(BukkitRunnable::cancel);
        smokeTasks.clear();
    }
}
